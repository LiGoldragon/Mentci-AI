# Prometheus runtime root-cause reweighting, LiteLLM version findings, and light-model sanity strategy

## Intent
Preserve the updated root-cause picture for the Prometheus LLM runtime lane after incorporating the later routing cleanup handoff and the new upstream research on Pi cancellation, LiteLLM disconnect handling, and llama.cpp runtime behavior.

This report extends:
- `Research/medium/CriomOS/591912244827_report_prometheus-ouranos-pi-home-routing_cleanup_push-state_and_remaining-llm-runtime-blocker.md`

## Operator instruction updates captured here
The following manual instructions from the operator now materially govern this lane:
- Cancel propagation is only part of the story; the runtime itself appears wrong because llama can stay hot even after a trivial prompt like `ping`.
- Start with the runtime-first stack strategy rather than prematurely narrowing on cancel handling alone.
- Build the most likely sane stack first, then test it.
- Install and use a light local model on Prometheus first to establish whether the stack itself is sane.
- After any runtime test on Prometheus, explicitly verify whether llama is still hot / consuming high CPU when it should be idle.
- If Prometheus remains hot after the test, force-stop the serving stack so the machine returns to calm.
- Prefer preserving shared service continuity in the intended design; do not make coarse process-kill the target solution.
- Exhaust canonical upstream solutions first; if missing, prefer a principled extension/fork over superstition.
- The old `programming: hh50gb7f` footer/prompt requirement should be removed entirely and not reintroduced.
- Subagents should withdraw rather than over-analyze when instructions are unclear.

## Updated root-cause picture
### What is now strongly evidenced
1. **Pi already performs real request aborts.**
   Escape/cancel in the TUI flows through `AbortController.abort()` and into provider fetch/reader cancellation.
2. **The repo pin is already past LiteLLM's important streaming disconnect cleanup fix.**
   The repository pins LiteLLM `1.82.1`, which is newer than the documented streaming cleanup release `1.81.14`.
3. **LiteLLM still does not provide a general automatic non-stream cancel-on-disconnect story.**
   Streaming cleanup improved, but general upstream cancel behavior remains provider-specific.
4. **llama.cpp / llama-server still lacks a reliable canonical per-request non-process-level cancel path.**
   This remains the strongest explanation for runaway server compute after the upstream HTTP path is gone.
5. **The observed symptom is broader than cancellation.**
   The operator explicitly notes that the runtime can remain hot even after a trivial prompt, meaning the stack may be unstable even when no explicit cancel event occurs.

### Consequence
The lane should no longer be framed as only:
- “make Escape propagate farther”.

It must now be framed as:
- “establish a known-good runtime stack with a light model,
- determine whether the runaway condition is stack-wide or model-specific,
- then fix remaining cancellation/spindown gaps on the correct layer.”

## Key local evidence
### Pi cancel path
Local repo evidence shows:
- interactive Escape triggers `this.agent.abort()`
- `Agent.abort()` calls `AbortController.abort()`
- the signal is passed into provider streaming calls
- provider code uses `fetch(..., signal)` and `reader.cancel()` on abort

Interpretation:
- Pi is not merely cancelling local UI state.
- It is causing a real HTTP/stream abort.

### LiteLLM pin in repo
The current packaged Prometheus lane uses:
- `litellm = 1.82.1`
- `litellm_proxy_extras = 0.4.53`
- `litellm_enterprise = 0.1.33.post2`

Primary file:
- `Components/nix/litellm-proxy.nix`

Interpretation:
- The Prometheus lane is already newer than the documented LiteLLM streaming disconnect cleanup release.
- Therefore a plain “upgrade LiteLLM for streaming disconnect cleanup” is unlikely to be the decisive fix.

### Runtime wiring still hard-wires a heavy model
Current Prometheus OS service wiring still points to a single heavy GGUF:
- `DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf`
- service `prometheus-llama-backup`
- direct llama port `11436`
- LiteLLM canonical port `11434`

Primary file:
- `Components/CriomOS/nix/mkCriomOS/llm.nix`

Interpretation:
- The current deployed stack does not yet contain a light-model sanity lane.
- That means the current system cannot distinguish:
  - heavy-model-specific pathology,
  - llama.cpp runtime pathology,
  - or broader stack wiring pathology.

## Upstream evidence summary
### LiteLLM
Strongest current conclusion:
- LiteLLM improved streaming disconnect cleanup and upstream connection closure in recent versions.
- That helps avoid stream/socket leakage.
- It does **not** prove that backend compute will halt for every provider.
- Non-stream general cancel-on-disconnect is still not a stable universal feature.

### llama.cpp / llama-server
Strongest current conclusion:
- There is still no authoritative reliable per-request non-process-level cancel API that would let the stack safely stop compute for an in-flight request while preserving the shared process.
- This matches observed Prometheus behavior: the upstream request can effectively disappear while compute continues.

## Revised engineering direction
### Recommended runtime-first strategy
1. **Add a local lightweight sanity model on Prometheus first.**
2. **Expose it cleanly through the same LiteLLM path used by Pi.**
3. **Test direct llama endpoint and LiteLLM endpoint with tiny prompts.**
4. **Observe whether idle spindown works correctly on the light model.**
5. **Only then reintroduce or compare against the heavy DeepSeek 70B model.**

### Why this is the best next move
It distinguishes three cases:
1. **Light model is healthy, heavy model is pathological**
   - then the issue is at least partly model-specific or resource-pressure-specific.
2. **Both light and heavy models stay hot**
   - then the issue is stack-wide, probably llama.cpp runtime / service config / ROCm interaction.
3. **Light model is healthy direct but not via LiteLLM**
   - then the gateway layer still matters.

This is the cleanest discriminator available from current evidence.

## Recommended first sanity model
Primary recommendation:
- **Llama-3.2-1B-Instruct GGUF**

Why:
- small enough for quick startup and safe smoke testing,
- still Llama-family, making comparison with the DeepSeek-Llama distill lane more meaningful,
- widely available as GGUF,
- suitable for simple prompt-following and quick `pong`-style checks.

Reasonable quant candidates:
- `Q4_K_M` as first default
- `Q8_0` as fallback if quant-specific loader/runtime weirdness appears

Secondary candidates if needed:
- Gemma-3 1B Instruct GGUF
- Qwen3 4B Instruct GGUF

## Likely file mutation surfaces
### Required for light-model introduction
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
  - current single heavy model path / single llama-server service
- `Components/CriomOS/nix/homeModule/min/default.nix`
  - model list and generated router/user metadata
- `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`
  - concrete router file currently single-model
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
  - Pi-visible canonical model metadata
- `config/pi/prometheus-agent-settings.json`
  - top-level Pi-visible config used in repo/devshell contexts

### Likely structural change
The current stack effectively assumes one llama-server service serving one model. A clean light-model lane likely requires:
- either a second llama-server service on a second port,
- or a temporary replacement of the heavy model during diagnosis.

Current evidence favors:
- **temporarily adding a second local sanity service** if resources permit,
- otherwise **temporarily replacing the heavy service during diagnosis** to reduce ambiguity and risk.

## Runtime safety rule for this lane
After every Prometheus test:
1. check whether llama/LiteLLM is still hot,
2. if it remains hot when it should be idle, force-stop it,
3. record that evidence before continuing.

This must remain part of the operator workflow until the runaway condition is resolved.

## Most likely successful stack shape now
### Phase 1: sanity stack
- local small GGUF on Prometheus
- dedicated llama-server instance
- LiteLLM routing entry for that model
- Pi can target it explicitly for smoke tests
- goal: prove stack health independent of 70B load

### Phase 2: heavy comparison
- reintroduce the current DeepSeek 70B lane
- compare startup, prompt completion, and idle spindown
- determine whether the runaway is:
  - model-specific,
  - service-config-specific,
  - or stack-wide

### Phase 3: canonical cancellation / spindown fix
If the light model is healthy but the heavy model is not:
- tune or patch the heavy runtime path.

If both models are unhealthy:
- the likely long-term target becomes llama.cpp / llama-server cooperative cancellation and/or runtime stability fixes,
- possibly via a principled fork wired through CriomOS.

## Compact handoff truth
If this session is compacted again, the most important updated truth is:

> Pi already appears to cancel correctly, and the repo already pins a LiteLLM newer than the main streaming disconnect cleanup fix. The remaining blocker is broader runtime instability in the Prometheus llama lane. The correct next move is to establish a light local sanity model on Prometheus, prove whether the stack itself is healthy, and only then compare the heavy DeepSeek 70B path. After every runtime test, verify the node is not still hot; if it is, force-stop the stack and record the evidence.
