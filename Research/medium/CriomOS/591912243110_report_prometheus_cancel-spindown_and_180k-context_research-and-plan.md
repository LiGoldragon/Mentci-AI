# Prometheus cancel/spindown and 180k-context research and plan

## Intent
Capture the current upstream/runtime truth for two linked operator requirements:
1. hitting `Esc` in Pi should free the Prometheus reasoning slot quickly instead of letting the heavy `llama.cpp` request keep running for tens of seconds, and
2. the Qwen 3.5 reasoning lane should be raised from `32768` context to at least `180k`.

This report focuses on research and planning, not implementation.

## Current observed local truth
The current Prometheus reasoning lane now serves the requested Qwen 3.5 GGUF successfully, but cancellation/spindown is still incomplete.

Observed operator-facing failure mode:
- Pi reports `operation aborted` on `Esc`
- the user receives no final response in Pi
- the underlying `llama-server` request keeps running for tens of seconds before the slot finally clears
- this is better than the earlier fully runaway behavior, but still bad because the slot is occupied and the response is lost

Current configured context budget for the heavy reasoning lane:
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
  - `contextWindow = 32768`
  - `ctxSize = 32768`
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
  - `contextWindow = 32768`
- `config/pi/prometheus-agent-settings.json`
  - `contextWindow = 32768`
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
  - launches `llama-server` with `--ctx-size ${toString model.ctxSize}`

## Internal wiring summary
### What CriomOS currently does
`Components/CriomOS/nix/mkCriomOS/llm.nix` launches the reasoning lane approximately as:
- `llama-server`
- `--parallel 1`
- `--ctx-size <lock-derived ctxSize>`
- `--no-warmup`
- `--no-mmap`
- `--no-webui`

The same lock/catalog data also feeds LiteLLM routing and the home-managed Pi metadata.

### What the home-managed Pi path currently trusts
`Components/CriomOS/nix/homeModule/min/default.nix` reads `../../../data/config/pi/prometheus-model-catalog.json` and generates:
- `~/.pi/agent/models.json`
- `~/.pi/agent/settings.json`
- `~/.pi/settings.json`

So for the authoritative Ouranos home-managed Pi path, the server-side lock and the Pi-visible catalog must stay synchronized.

## External research summary

## 1. Pi abort alone is not enough
Prior local evidence already established that Pi uses a real `AbortController`-based abort path. So the remaining issue is not merely that Pi fails to signal cancellation.

## 2. LiteLLM is a real cancellation gap in the middle of the stack
Current upstream LiteLLM evidence indicates that a client disconnect/abort does not reliably cancel the upstream provider request.

Key findings:
- LiteLLM issue `#13774`: proxy keeps provider request alive when client disconnects; cancel-on-disconnect remains unresolved.
- PR `#14295` attempted this behavior and was reverted.
- LiteLLM issue `#17364`: breaking a stream iterator stops client delivery but the upstream request can still continue.
- LiteLLM issue `#9551`: long non-streaming requests often end at timeout, not true upstream cancellation.
- LiteLLM docs expose `/v1/responses/{id}/cancel`, but this only helps if the upstream provider itself supports cancellation.

Implication for Pi → LiteLLM → llama-server:
- Pi aborting the request does not guarantee that `llama-server` will stop computing.
- Therefore the current Prometheus path contains a structural cancel-propagation hole even before we reach llama.cpp.

## 3. llama.cpp / llama-server also lacks a reliable immediate cancel story
Recent upstream llama.cpp evidence indicates that client disconnect is not a dependable immediate-stop mechanism anymore.

Key findings:
- issue `#4911`: older/early guidance was effectively “close the connection”.
- issues `#11414` and `#11720`: newer builds show generation continuing even after cancel/disconnect; the slot only frees after the request naturally finishes.
- issue `#10509`: the proposed real fix is to wire request abortion into `ggml_abort_callback` / decode-time checks.
- server docs document `/slots` inspection and actions like save/restore/erase, but not a canonical HTTP request-cancel endpoint.
- server docs also document `--sleep-idle-seconds`, which helps idle unload/spindown after inactivity, but does not solve immediate abort of an in-flight decode.

Implication:
- Even if LiteLLM were bypassed, a plain HTTP disconnect to `llama-server` is not enough to guarantee immediate slot release.
- The durable fix is likely a repo-local runtime patch/fork of `llama.cpp` exposing per-request cancellation and cooperative decode abort.

## 4. 180k context is feasible for this model without YaRN
The chosen Qwen model family advertises a native `262144` token context window.

Key findings:
- Qwen/Qwen3.5-35B-A3B model card: native context `262,144`, extendable beyond that with YaRN.
- Qwen llama.cpp docs: long context is controlled by `--ctx-size`; YaRN/rope scaling only becomes relevant when stretching beyond the native context.

Implication:
- Raising Prometheus to `>=180k` does **not** require YaRN or rope scaling because `180k < 262144`.
- The required first step is simply to increase the configured `ctxSize`/`contextWindow` and ensure the machine can afford the KV-cache cost.

## 5. The real risk for 180k is memory/latency, not model architecture
The model can support 180k in principle, but the KV cache and runtime latency scale badly.

Operational implications from upstream/community evidence:
- KV cache grows roughly linearly with context length.
- For a large GGUF MoE model, `180k` context can require tens of GiB of additional memory pressure beyond the model weights themselves.
- Warmup and decode latency may worsen significantly.
- Large-context reasoning runs may also interact badly with known Qwen3.5 reasoning/template issues in llama.cpp, so the lane should be validated with careful prompts after any ctx-size jump.

## Why the current behavior happens
The current bad behavior is best explained as a stack composition problem:
1. Pi aborts locally and closes its side of the request.
2. LiteLLM does not reliably cancel the upstream provider request.
3. Even if the provider sees a disconnect, current `llama-server` may keep decoding until the request naturally ends.

So the present stack has **two** cancellation gaps:
- gateway gap: LiteLLM does not reliably propagate abort upstream
- runtime gap: llama.cpp does not reliably stop the in-flight decode immediately

That is why the slot stays occupied even though Pi already says `operation aborted`.

## Strategy options

### Strategy 1 — config-only / observational hardening
What it is:
- add slot observability and idle-sleep knobs
- raise context in config only
- keep the current Pi → LiteLLM → llama-server path unchanged

Pros:
- smallest change surface
- useful for observability and long-context enablement

Cons:
- does **not** solve immediate abort reliably
- at best it gives better post-failure behavior and metrics

Verdict:
- necessary as scaffolding, insufficient as the fix

### Strategy 2 — bypass LiteLLM for the heavy reasoning lane
What it is:
- point the heavy `main-reasoning` path directly at the reasoning `llama-server` endpoint (or add a dedicated direct path for it)
- keep LiteLLM for other models or compatibility needs

Pros:
- removes one broken cancellation hop
- simplifies diagnosis and may improve end-to-end latency

Cons:
- still does **not** guarantee immediate cancel, because llama.cpp itself is still weak here
- creates split routing semantics unless carefully designed

Verdict:
- useful as a simplifier and diagnostic tool, but not a complete solution

### Strategy 3 — repo-local runtime cancel support in llama.cpp, then wire Pi/gateway to use it (**recommended**)
What it is:
- keep the repo-local `llama.cpp` package approach already established for Qwen3.5 support
- add a repo-local cancellation patch so in-flight requests can set a per-request abort flag checked inside decode/prompt processing
- expose that capability via a request-aware cancel mechanism
- then teach Pi / gateway layer to call that cancel path on `Esc`

Pros:
- addresses the actual runtime cause
- preserves the shared loaded model instead of coarse process-killing as the main design
- can coexist with LiteLLM or a direct path

Cons:
- most engineering work
- requires careful validation on ROCm and with Qwen3.5 MoE

Verdict:
- best fit for the operator requirement of “leave the slot empty for a new request” after `Esc`

## Recommended plan

### Phase 0 — observability before mutation
Add explicit observability around the reasoning lane so cancel behavior is measurable, not anecdotal.

Target outcomes:
- request IDs correlated across Pi, LiteLLM, and llama-server logs where possible
- slot state visible through llama-server slot/metrics facilities if available in the pinned release
- heat-check procedure after every abort test

Why first:
- before changing cancellation semantics, we need proof of slot release timing and whether the server is still hot after abort

### Phase 1 — raise context to a rounded >=180k target
Recommended concrete target:
- `196608` tokens (`192k`)

Why this exact number:
- comfortably exceeds the user’s `180k` minimum
- remains below the model’s native `262144` context window
- avoids requiring YaRN/rope changes for the first pass
- is a cleaner binary-ish operational number than `180000`

Files to update:
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
  - `qwen3.5-35b-a3b.contextWindow`
  - `qwen3.5-35b-a3b.ctxSize`
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
  - `qwen3.5-35b-a3b.contextWindow`
- `config/pi/prometheus-agent-settings.json`
  - both canonical and alias entries for `qwen3.5-35b-a3b` / `main-reasoning`
- confirm `Components/CriomOS/nix/homeModule/min/default.nix` continues deriving Pi metadata from the catalog without extra caps

Validation focus:
- Prometheus service starts with the larger `--ctx-size`
- warm-state minimal `pong` probe still passes
- a long-context synthetic prompt fits without OOM
- post-test heat check confirms the service returns to idle after normal completion

### Phase 2 — reduce the gateway cancellation gap
Do the smallest architecture change that prevents Pi abort from dying locally while leaving the heavy upstream request untracked.

Recommended shape:
- introduce request correlation for Prometheus reasoning calls
- evaluate whether the heavy reasoning lane should temporarily bypass LiteLLM during abort-fix work, because LiteLLM does not reliably propagate cancel-on-disconnect
- if keeping LiteLLM in path, add an explicit cancel side-channel rather than relying on disconnect semantics alone

Success criterion:
- an `Esc` in Pi triggers an explicit cancel attempt against the active Prometheus reasoning request, not just a local reader abort

### Phase 3 — add repo-local llama.cpp cooperative cancellation (**core fix**)
Implement the real cancellation fix in the repo-local `llama.cpp` package already introduced for Qwen3.5 support.

Design target:
- maintain a request/task-level cancel flag inside server state
- expose a cancel mechanism keyed by request/task/slot identity
- check the abort flag inside prompt processing / token decode using the engine’s abort callback or equivalent cooperative-stop hook
- ensure cancellation releases the slot promptly and returns the server to idle without killing the whole process

Acceptance target for this phase:
- while `main-reasoning` is generating, pressing `Esc` in Pi causes the active server task to stop quickly and the slot becomes available for a new request within a small bounded time window (for example a few seconds, not tens of seconds)

### Phase 4 — decide user-facing `Esc` semantics in Pi
Once runtime cancellation is real, decide what Pi should keep on screen when the user aborts.

Two viable behaviors:
1. **Hard cancel + preserve partial output**
   - stop upstream generation immediately
   - keep whatever partial assistant text has already arrived
2. **Hard cancel + explicit discard**
   - stop upstream generation immediately
   - discard partial output intentionally

Recommendation:
- preserve partial output if technically straightforward, because the operator explicitly called out the current “lose the slot and lose the response” behavior as bad

### Phase 5 — verify with real abort and long-context probes
Authoritative checks should cover both dimensions:
- abort test: start a prompt long enough to generate for a while, press `Esc`, verify prompt slot clears quickly
- immediate reuse test: send a new probe right after abort; it should start promptly
- long-context test: verify `>=180k` input budget without OOM or pathological latency regression
- normal-response test: ensure ordinary `pong`/short prompts still return correctly

## Concrete recommended next mutation order
1. Add observability + post-test heat-check hooks.
2. Raise `main-reasoning` context to `196608` and redeploy.
3. Test memory/latency and confirm normal idle behavior.
4. Introduce explicit request-tracking / cancel plumbing for Prometheus reasoning calls.
5. Patch repo-local `llama.cpp` for cooperative cancellation.
6. Wire Pi `Esc` to the explicit cancel path.
7. Decide whether to preserve partial output on abort.

## Important design guardrails
- Do not rely on plain HTTP disconnect as the final design.
- Do not rely on LiteLLM automatic upstream cancel-on-disconnect; upstream evidence says this is not dependable today.
- Do not make “kill the whole reasoning service on every abort” the primary design, because it destroys warm-state usefulness; keep it only as emergency safety fallback.
- Do not add YaRN/rope scaling for the first 180k pass; it is unnecessary below the model’s native context budget.
- Keep all Nix/package work repo-local to the active repository surfaces.

## Practical recommendation
The best plan is a two-track lane:
- **Track A:** raise the reasoning lane to `192k` (`196608`) now, because that is a clean configuration change within the model’s native context support.
- **Track B:** implement a real cancel path by patching the repo-local `llama.cpp` runtime and wiring Pi/gateway to use an explicit cancel, because neither LiteLLM nor current llama-server disconnect handling is sufficient.

That pairing directly addresses both operator goals without pretending that config-only changes will solve the slot-release problem.

## Sources
- llama.cpp server README (`/slots`, slot actions, `--sleep-idle-seconds`)
- llama.cpp issues: `#4911`, `#10509`, `#11414`, `#11720`
- LiteLLM docs: response cancel API, timeout docs
- LiteLLM issues: `#9551`, `#13774`, `#17364`
- Qwen/Qwen3.5-35B-A3B model card (native `262144` context)
- Qwen llama.cpp local-run docs (`--ctx-size`, YaRN/rope controls)
