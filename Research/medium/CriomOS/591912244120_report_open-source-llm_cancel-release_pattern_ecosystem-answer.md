# Open-source LLM cancel/release pattern: ecosystem answer

## Intent
Answer the operator’s question at the ecosystem level:
- what is the open-source world’s answer to cancelling in-flight LLM work,
- releasing the occupied request slot quickly,
- and not losing the already-produced partial response.

This note is not Prometheus-specific implementation guidance. It is the cross-project architectural answer drawn from major open-source serving stacks and gateway patterns.

## Short answer
The open-source world answer is **not**:
- “just close the client socket”
- “just kill the whole model process”
- “just wait for the generation to finish naturally”

The mature answer is:
1. **treat generation as a request with an identity** (`request_id`, `response_id`, slot/task handle)
2. **support explicit cancellation** of that request, not just implicit disconnect
3. **perform cooperative cancellation inside the serving scheduler/runtime**, so the request is removed from batching/decoding and its KV/scheduler state is reclaimed quickly
4. **stream partial output to the client as it is produced**, so cancel stops future tokens but already-emitted tokens remain useful
5. **keep server-side timeouts/watchdogs as fallback**, not as the primary user-facing cancel mechanism

In one sentence:
> The OSS consensus is request-scoped explicit abort + scheduler/runtime cooperative cancellation + streaming partial-output preservation, with disconnect and timeouts as secondary signals/fallbacks.

## The major ecosystem pattern

## 1. Mature engines do cancellation at the request/scheduler layer
Across higher-performance serving engines such as vLLM, TGI, and SGLang, the better pattern is not “kill the model” but “abort the request inside the engine.”

That means:
- the request has an ID
- the scheduler knows the request is active
- abort removes it from the active queue/batch
- the engine reclaims its decode/KV/batch capacity as fast as the runtime allows

This is the real "slot release" story in the stronger OSS stacks.

### vLLM pattern
vLLM documents explicit abort support in its engine APIs and scheduler internals.
Its scheduler docs explicitly describe finished/aborted requests being removed from internal queues and their cached state being cleared in subsequent scheduling steps.

Important implication:
- cancellation is treated as a **first-class scheduler event**, not just a broken TCP stream.

Recent vLLM issues also show operators wanting:
- partial rollout return on abort
- confirmation that all unfinished requests are actually drained before sleeping/restarting the server

That tells us the mature community focus is:
- explicit abort,
- partial result preservation,
- and deterministic request-drain visibility.

### TGI pattern
TGI historically treated closing the streaming connection as cancellation for streamed generation.
It also uses continuous batching and overload controls.

Important implication:
- TGI sits closer to “stream close cancels” than some other stacks,
- but the real advantage still comes from the engine/scheduler being designed for dynamic in-flight request management.

### SGLang pattern
SGLang exposes timeout/watchdog controls and has active work/issues around aborting queued/running requests.
It is also a scheduler-driven runtime, not a static one-shot process model.

Important implication:
- the community expectation is again request-level lifecycle control,
- but there are still real bugs/edge cases when disconnect/timeout handling does not fully stop decode.

## 2. Gateway/UI layers are not the real cancel authority
Open-source proxies and UIs typically do one or both of these:
- close the downstream stream on client abort
- propagate an abort/cancel signal to the upstream request

But gateways are not the true source of slot release unless the backend runtime cooperates.

This is why client disconnect alone is widely understood to be insufficient.

### Typical gateway behavior
- frontend uses `AbortController` or closes SSE/WebSocket
- proxy tears down its side of the stream
- best-case proxy also cancels the upstream async task or forwards an explicit cancel command

### Why this is not enough by itself
If the inference engine keeps decoding after disconnect:
- the slot is still occupied
- the GPU/CPU is still hot
- the client got an abort, but the backend did not truly stop

So the ecosystem answer is:
- **gateway abort is necessary but not sufficient**

## 3. Partial output preservation is normal, not exotic
In modern streaming-oriented serving, partial output already delivered to the client is normally kept.
Cancel means:
- stop producing more tokens
- do not erase what was already streamed

This is the practical user-facing answer to:
- “don’t lose both the slot and the answer”

More advanced vLLM discussions explicitly ask for abort to return the partial rollout rather than discarding it.
That is exactly aligned with the operator concern here.

So in ecosystem terms, the desirable UX is usually:
- **preserve partial text, free the slot, stop future decode**

## 4. Timeouts/watchdogs are fallback safety, not the primary design
Across projects, operators still use:
- request timeouts
- watchdog timeouts
- client-side stream cancellation
- external gateways/load balancers

But these are regarded as fallback safeguards when full cooperative cancel is imperfect.
They are not the gold-standard interactive cancel model.

The reason is simple:
- timeouts are coarse
- they act late
- they often leave poor UX
- they may still require drain time before the engine is truly idle

## 5. Anti-patterns the OSS world keeps rediscovering
### Anti-pattern A: relying on plain disconnect alone
This often fails when the runtime does not stop compute immediately.

### Anti-pattern B: killing the whole model process for every cancel
This frees resources, but destroys warm-state latency and turns a request-lifecycle problem into a process-lifecycle problem.
Useful as emergency stop, poor as the main UX.

### Anti-pattern C: non-streaming when you care about cancellation UX
If you buffer everything until the end, you naturally lose partial output.
Streaming is the normal prerequisite for “keep what we already got.”

### Anti-pattern D: static/rigid slot models without cooperative eviction
If a serving stack cannot evict or mark requests dead during scheduling/decode, abort behavior will always feel broken.

## The ecosystem hierarchy of answers
From strongest to weakest:

### Best answer
**Explicit request cancel + runtime/scheduler abort + partial-output preservation**

Typical features:
- request/response IDs
- abort endpoint or internal abort API
- scheduler removes request from queue/batch
- engine reclaims KV/cache pages
- stream already-sent tokens remain visible

### Acceptable interim answer
**Disconnect-aware streaming + cooperative runtime checks + timeouts/watchdog**

This is workable when explicit cancel APIs are immature but the engine does check disconnect/cancel frequently enough.

### Weak answer
**Client abort only**

## Sources
- vLLM scheduler and cancellation documentation
- TGI streaming/cancel behavior notes and overload controls
- SGLang timeout/abort research threads and community discussions
- General open-source serving-layer architecture knowledge across major runtimes

## Prompt
Provide the operator with the ecosystem-level answer to: how do OSS serving stacks cancel in-flight LLM work, release the occupied slot quickly, and keep the partial response already sent to the client?

## Context
This research note compares behaviors across the primary OSS serving engines (vLLM, TGI, SGLang) and gateway/UI layers to articulate the mature pattern for request cancellation, slot release, and partial-output preservation. It is intended for cross-project reasoning rather than a Prometheus-specific implementation.

## Summary
- Mature OSS engines treat cancellation as a scheduler/runtime event tied to request IDs, so request aborts drop the work from queues and reclaim resources quickly.
- Gateways or UIs can tear down streams, but the slot only frees reliably when the backend runtime cooperatively cancels in-flight work and stops decoding.
- Partial output that already reached the client is typically preserved while canceling future tokens, and timeouts/watchdogs are fallback safety nets rather than the primary cancel mechanism.

This often stops the UI but not the backend workload.

### Worst answer
**Process kill / restart as normal cancel mechanism**

Acceptable only as emergency recovery.

## What this means in practical architecture terms
If an operator wants a correct modern cancel/release design, the stack usually needs:
1. **streaming transport**
2. **request identity**
3. **cancel signal path** from UI/proxy to backend
4. **backend runtime support** to honor cancel during scheduling/decode
5. **observability** to confirm the request really drained and the slot really freed

If any one of these is missing, cancellation becomes unreliable.

## The specific lesson from comparing projects
### Stronger/more modern serving stacks
vLLM / TGI / SGLang communities think in terms of:
- schedulers,
- active request sets,
- abort queues,
- unfinished-request counters,
- partial rollouts,
- timeouts as guardrails.

### Weaker/rougher stacks
Stacks that mostly expose a raw model process behind an OpenAI-like HTTP API but do not have rich request lifecycle management tend to struggle with:
- immediate cancel,
- quick slot release,
- and preserving partial outputs cleanly.

That gap explains why some systems feel “interactive” under abort and others feel “stuck” even though the client says cancelled.

## Ecosystem answer in one operational sentence
The open-source world answer is:
> do not model cancel as a socket accident; model it as an explicit request-lifecycle transition that the scheduler/runtime understands and can enforce quickly.

## Practical recommendation template
If designing or evaluating an OSS-serving stack, ask these five questions:
1. Can each request be named/tracked (`request_id` / `response_id`)?
2. Is there an explicit cancel path, not just disconnect semantics?
3. Does the runtime/scheduler actually remove aborted requests and reclaim capacity quickly?
4. Are partial streamed tokens preserved for the client?
5. Is there a metric/log proving the request truly drained?

If the answer to any of those is no, the cancel UX is probably incomplete.

## Sources
- vLLM docs/API: abort support and scheduler internals
- vLLM issues: `#10087`, `#20798`, `#22197`, `#27901`
- Hugging Face TGI docs and issue `#1040`
- SGLang docs/discussions/issues: `#6855`, `#13575`, `#15686`
- LiteLLM issue `#13774` and Responses API docs (for gateway-layer contrast)
