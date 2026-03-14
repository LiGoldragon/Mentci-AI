# LiteLLM cancel propagation status, master state, and extension surface

## Intent
Answer the operator’s follow-up question precisely:
- is the earlier LiteLLM upstream-cancel problem fixed in a newer release,
- is it fixed on master,
- or can it be solved cleanly with LiteLLM extensions/plugins/hooks?

This note focuses on the Prometheus stack shape:
- Pi → LiteLLM → llama.cpp `llama-server`

## Short answer
### Yes, but only partially
Newer LiteLLM releases improved **stream/disconnect cleanup**.
They do **not** by themselves guarantee that an OpenAI-compatible upstream like `llama-server` will stop computing and free the slot immediately.

So the correct refined statement is:
- **LiteLLM has gotten better at closing upstream streaming connections when the client disconnects**
- but **that does not solve Prometheus slot release unless the upstream server actually treats that closure as cancellation**
- and current `llama-server` evidence says that reliable immediate cancellation is still not there

Therefore:
- for the Prometheus Qwen lane, newer LiteLLM is helpful but **not sufficient as the whole fix**

## What changed in LiteLLM
### Earlier state
Earlier work found:
- issue `#13774` tracked missing cancel-on-client-disconnect behavior
- PR `#14295` attempted an upstream-cancel-on-disconnect implementation
- PR `#14304` reverted that attempt

### Newer state
Later work landed a more conservative cleanup fix:
- PR `#21213` (merged Feb 2026)
- reflected in release notes around `v1.81.14` and carried into `v1.82.0`

The key claimed improvement:
- release upstream/provider streaming connections when clients disconnect mid-stream
- prevent connection-pool exhaustion / leaked open streams

This is real progress.

## Why that still does not fully solve Prometheus
Closing the upstream stream is not the same as aborting upstream model execution.

In the Prometheus stack, the real question is:
> when Pi hits `Esc`, does the active Qwen request in `llama-server` stop promptly and release the slot?

The available evidence still says:
- LiteLLM may now close or clean up its side of the provider stream more correctly
- but `llama-server` can still keep decoding after disconnect/cancel and only release the slot after the request naturally ends

So LiteLLM’s newer fix addresses:
- connection leaks
- stream cleanup
- pool starvation

But it does **not** prove:
- immediate provider-side compute cancellation
- prompt slot release on `llama-server`

For Prometheus, that distinction is the whole problem.

## Latest release and master: what to believe
### Best current reading
- A newer LiteLLM release train does include improved disconnect cleanup for streaming.
- Master is at least as good on that narrow dimension.
- There are still later/open reports of hanging requests or cancellation weirdness after those changes.

So the safe statement is:
- **"fixed" for stream cleanup:** substantially yes
- **"fixed" for end-to-end upstream cancellation against llama-server:** no reliable evidence

## Responses API cancel does not rescue llama-server today
LiteLLM exposes OpenAI Responses API cancellation:
- `POST /v1/responses/{id}/cancel`

But the docs explicitly say:
- not all providers support response cancellation
- unsupported providers raise errors

For `llama-server`, current upstream evidence says:
- it does **not** implement `/v1/responses`
- therefore it also does not provide `/v1/responses/{id}/cancel`

Implication:
- LiteLLM’s Responses cancel API cannot currently serve as the Prometheus fix when the upstream is `llama-server`

## Extension / plugin / middleware answer
### No clean documented extension path exists for this exact need
LiteLLM has extensibility surfaces such as:
- request/response hooks via custom loggers
- custom providers
- OpenAI-compatible upstream configuration

But the researched limitation is:
- those documented surfaces do **not** expose the proxy’s downstream disconnect lifecycle in a way that a provider plugin can use to cancel the upstream call
- they are mainly for payload mutation, auth, logging, or provider implementation details
- they are not a documented “on client disconnect, cancel upstream task” interface

### Minimal LiteLLM-side patch shape
The smallest plausible LiteLLM-side change remains roughly:
- watch FastAPI/Starlette request disconnect events
- cancel the async upstream task when `http.disconnect` arrives
- cleanly unwind with a 499-style path

That is conceptually close to the reverted earlier approach and related later work.

### But even that still depends on llama-server
Even with a perfect LiteLLM disconnect watcher:
- if `llama-server` does not cooperatively stop decode promptly,
- the slot can still remain occupied

So a LiteLLM extension/patch alone is not enough to promise the operator result.

## What this means for strategy choice
### Strategy 1 revisited
Original Strategy 1 was config/observability hardening only.

With this new LiteLLM research, Strategy 1 becomes slightly stronger if expanded to include:
- upgrade/confirm latest LiteLLM disconnect-cleanup behavior
- add observability
- possibly simplify routing

But Strategy 1 **still does not guarantee** the target behavior:
- `Esc` frees the reasoning slot promptly

So if the operator wants only the safest immediate next move, a revised Strategy 1 is valid for:
- confirming the latest LiteLLM behavior in the current stack
- reducing one source of leakiness
- gathering sharper evidence

It is **not** a credible final fix by itself.

### Strong conclusion for Prometheus
For a Pi → LiteLLM → `llama-server` stack:
1. newer LiteLLM is worth having
2. LiteLLM master/recent releases improved disconnect cleanup
3. LiteLLM plugins/extensions do not currently provide a clean no-fork cancel-propagation solution
4. the blocking hard part remains `llama-server` request cancellation / slot release

## Operational recommendation
If the operator is leaning Strategy 1, the best refined version is:
- **Strategy 1a:** update/confirm the newest LiteLLM disconnect-cleanup behavior and instrument the stack
- **then immediately test whether Prometheus slot release materially improves**
- **if not, escalate straight to runtime-level llama.cpp cancellation work**

That is a sensible low-risk next step because it may remove one layer of bad behavior without large architecture changes.

But it should be treated as:
- a diagnostic and partial-hardening pass,
- not the final answer.

## Compact answer for future handoff
> Newer LiteLLM releases and master have improved stream/disconnect cleanup, but that is not the same as reliable upstream compute cancellation. For a Prometheus stack backed by llama.cpp `llama-server`, there is still no evidence that LiteLLM alone—whether newer release or documented extension/plugin surface—can guarantee immediate slot release on `Esc`. LiteLLM’s Responses cancel API does not help because `llama-server` does not implement the Responses API. Therefore, a LiteLLM upgrade is worthwhile as a partial hardening step, but the likely final fix still lives in runtime-level cancellation within `llama-server` (or an architecture change that avoids depending on disconnect semantics).

## Sources
- LiteLLM release notes around `v1.81.14`
- LiteLLM PRs/issues: `#13774`, `#14295`, `#14304`, `#21213`, `#19749`
- LiteLLM Responses API docs
- llama.cpp issue `#14702` (`/v1/responses` missing)

## Prompt
Provide the operator with a precise follow-up on whether LiteLLM cancel propagation is fixed in the latest releases/master and whether extensions/plugins can address the remaining Prometheus slot-leak risk.

## Context
The focus remains the Prometheus stack (Pi → LiteLLM → llama.cpp `llama-server`) where user disconnects still leave slots occupied. The earlier LiteLLM cancel-on-disconnect work was reverted, and the new release notes now describe cleanup improvements rather than immediate upstream cancellation.

## Summary
- Newer LiteLLM releases (v1.81.14+, v1.82.0 master) improve stream cleanup but do not guarantee upstream compute cancellation when Pi disconnects.
- LiteLLM master matches the release behavior on this narrow dimension, and extensions/plugins do not currently expose a disconnect-to-cancel hook that would let `llama-server` stop decoding early.
- The operator should treat a LiteLLM upgrade as a hardening/diagnostic step, but the final slot-release fix still depends on runtime-level `llama-server` cancellation or a broader architectural change.

## Guard status
- `execute root-guard` currently fails because `Components/mentci-aid/src/actors/root_guard.edn` is missing; the guard cannot pass until that sidecar config materializes.
