# Prometheus Cancel/Spindown and 180K Context Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Make `Esc` in Pi free the Prometheus reasoning slot promptly and raise the Qwen3.5 reasoning lane to at least 180k context without losing repo-local Nix purity.

**Architecture:** Treat this as two linked tracks. First, raise the configured context budget for `main-reasoning` to a native-range value (`196608`) across the server lock, Pi-visible catalog, and generated home-managed Pi metadata. Second, add explicit request cancellation plumbing for the Prometheus reasoning lane instead of relying on client disconnect semantics through LiteLLM and current `llama-server` behavior.

**Tech Stack:** NixOS/CriomOS modules, repo-local `llama.cpp` override, LiteLLM gateway, Pi provider metadata, Qwen3.5-35B-A3B GGUF.

---

### Task 1: Capture baseline abort and heat-check evidence

**TDD scenario:** Modifying tested code — gather runtime evidence first.

**Files:**
- Read: `Research/medium/CriomOS/591912243110_report_prometheus_cancel-spindown_and_180k-context_research-and-plan.md`
- Read: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Read: `Components/CriomOS/data/config/pi/prometheus-model-lock.json`

**Step 1: Run a bounded abort reproduction**

Run the current authoritative Pi path with a deliberately long prompt, press `Esc`, and record timestamps for:
- request start
- Pi abort message
- last observed server activity
- slot release / service idle time

**Step 2: Run the heat check immediately after abort**

Capture:
- active `prometheus-llama-reasoning` state
- whether the model is still decoding / hot
- whether a second request can start immediately

**Step 3: Persist the evidence in Research**

Update the active research artifact or add a sibling note with the measured timing.

**Step 4: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.

### Task 2: Raise the reasoning lane context to 196608

**TDD scenario:** Trivial change — use judgment, but verify with deployment/runtime tests.

**Files:**
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: `config/pi/prometheus-agent-settings.json`
- Read/verify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Read/verify: `Components/CriomOS/nix/mkCriomOS/llm.nix`

**Step 1: Update the lock**

Set the Qwen reasoning entry to:
- `contextWindow: 196608`
- `ctxSize: 196608`

**Step 2: Update the Pi-visible catalog**

Set the matching reasoning model metadata to:
- `contextWindow: 196608`

**Step 3: Update non-home Pi settings metadata**

Set both canonical and alias entries (`qwen3.5-35b-a3b`, `main-reasoning`) to:
- `contextWindow: 196608`

**Step 4: Verify no hidden cap remains**

Confirm:
- `mkCriomOS/llm.nix` still passes `--ctx-size ${toString model.ctxSize}`
- `homeModule/min/default.nix` still derives Pi model metadata from the updated catalog without another reasoning-lane clamp

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to finalize and push this atomic config intent.

### Task 3: Add reasoning-lane observability and slot state visibility

**TDD scenario:** Modifying runtime config — verify behavior with deployment/runtime checks.

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Possibly modify: router/service config surfaces that log or expose request metadata
- Update: relevant Research artifact with observed slot behavior

**Step 1: Enable the smallest useful llama-server observability knobs**

Aim for:
- slot visibility (`/slots`) if supported by the chosen release/flags
- request/slot correlation in logs where feasible
- idle/spindown visibility

**Step 2: Consider enabling `--sleep-idle-seconds`**

Only as an idle-unload aid, not as the core abort fix.
Pick a conservative value if used.

**Step 3: Redeploy and verify observability works**

Confirm that after one request you can identify:
- active slot/request
- transition to idle
- whether the slot remains occupied after client abort

**Step 4: Finalize via `jj-agent`**

Commit only the observability/runtime-config change.

### Task 4: Decide the cancellation architecture boundary

**TDD scenario:** New feature — design checkpoint before implementation.

**Files:**
- Read: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Read: Pi provider/runtime wiring files that initiate Prometheus requests
- Update: a design/research note if the boundary decision changes the architecture

**Step 1: Compare two shapes**

Option A:
- keep Pi → LiteLLM → llama-server
- add explicit cancel side-channel

Option B:
- route `main-reasoning` directly to the heavy reasoning `llama-server`
- keep LiteLLM only where it adds value

**Step 2: Pick the minimal architecture that supports explicit cancel**

Do **not** rely on disconnect semantics alone.

**Step 3: Persist the decision with rationale**

Document the chosen boundary and why.

**Step 4: Finalize via `jj-agent`**

Commit the architecture decision artifact if it changed.

### Task 5: Patch repo-local llama.cpp for cooperative request cancellation

**TDD scenario:** New feature — full TDD-ish runtime verification cycle.

**Files:**
- Modify: `Components/CriomOS/nix/llama-cpp-prometheus.nix` (if patch application/wiring is needed)
- Create/modify: repo-local patch file under `Components/CriomOS/nix/` or adjacent package assets
- Modify: runtime server invocation/config if a new cancel/slot feature flag or endpoint is introduced

**Step 1: Add a request/task abort flag in server state**

The patch should allow a request to be marked cancelled by request/task/slot identity.

**Step 2: Wire the abort flag into decode/prompt processing**

Use the engine’s cooperative abort hook (`ggml_abort_callback` or equivalent) so in-flight work can stop promptly.

**Step 3: Expose an explicit cancel path**

Provide a request-aware cancel mechanism that the upper layers can call.

**Step 4: Build the package locally**

Verify the repo-local package still builds and the server starts.

**Step 5: Finalize via `jj-agent`**

Commit only the runtime patch/package wiring.

### Task 6: Wire Pi/gateway abort to the explicit cancel path

**TDD scenario:** New feature — run existing abort behavior first, then implement minimal plumbing.

**Files:**
- Modify: the Pi/provider or gateway integration surface that currently only aborts locally
- Modify: any Prometheus routing config required to reach the new cancel mechanism
- Update: relevant Research artifact with the new flow

**Step 1: Reproduce current abort behavior one more time**

Confirm baseline before changing client/gateway behavior.

**Step 2: Add explicit cancel on `Esc`**

When the active provider/model is Prometheus reasoning, `Esc` should:
- abort local streaming/reader state
- send an explicit cancel against the active reasoning request

**Step 3: Preserve partial output if straightforward**

If partial assistant text has already arrived, prefer keeping it visible rather than dropping it.

**Step 4: Verify immediate slot reuse**

After `Esc`, a new small request should start promptly.

**Step 5: Finalize via `jj-agent`**

Commit only the client/gateway cancel plumbing.

### Task 7: Deploy and validate end-to-end

**TDD scenario:** Verification task.

**Files:**
- No new files required, but update Research with measured evidence

**Step 1: Deploy the updated CriomOS runtime**

Rebuild and deploy Prometheus using the established CriomOS flow.

**Step 2: Run abort acceptance checks**

Verify:
- Pi `Esc` stops the active reasoning request quickly
- the slot frees within a small bounded time window
- a second request can begin immediately after abort

**Step 3: Run long-context acceptance checks**

Verify:
- the reasoning lane advertises/accepts `196608`
- a long prompt near the new budget fits without OOM
- the service still returns to idle after completion

**Step 4: Run normal short-prompt checks**

Re-run the exact warm-state `pong` probe and compare latency to the previous ~20.89s reference.

**Step 5: Finalize via `jj-agent`**

Commit/push any final verification or documentation changes.

### Task 8: Emergency fallback and operator guardrails

**TDD scenario:** Operational hardening.

**Files:**
- Update: Research or operator runbook artifact if needed

**Step 1: Define the emergency fallback**

If cancellation fails and the reasoning lane stays hot:
- capture evidence
- forcibly stop/restart the serving stack
- document the stop reason

**Step 2: Keep this as fallback, not primary design**

Do not regress to “kill the whole service on every abort” as the normal path.

**Step 3: Finalize via `jj-agent`**

Commit only the runbook/guardrail change if it was added.
