# Prometheus Qwen ROCm offload failure: runtime evidence and fix lane

## Intent
Persist the critical debugging conclusion before session compaction:
- the current Prometheus Qwen reasoning lane is not merely timing out due to a short Pi timeout,
- the live `llama-server` runtime is failing to initialize ROCm GPU offload,
- therefore the heavy Qwen lane is effectively CPU-bound and far slower than intended.

## Short answer
The current live Prometheus Qwen lane is **not using GPU offload correctly**.

Key evidence from live service logs:
- `ggml_cuda_init: failed to initialize ROCm: no ROCm-capable device is detected`
- `warning: no usable GPU found, --gpu-layers option will be ignored`
- `load_tensors: CPU model buffer size = 35183.10 MiB`
- `llama_kv_cache: CPU KV buffer size = 3840.00 MiB`

Operational implication:
- the runtime intends to use GPU offload (`--n-gpu-layers 99`),
- but the host runtime cannot see/use a ROCm-capable device,
- so the Qwen lane runs on CPU and can take minutes even for tiny requests once large prompt/session state is involved.

## Current live runtime truth
### Service flags still intend GPU usage
From `Components/CriomOS/nix/mkCriomOS/llm.nix` and the live process command line, the Qwen reasoning lane runs with:
- `--n-gpu-layers 99`
- `--parallel 1`
- `--ctx-size 196608`
- `--alias prometheus-main-reasoning`

So the service is configured to try GPU offload.

### Live runtime refuses GPU offload
Prometheus-side logs from `prometheus-llama-reasoning.service` show the GPU path is failing at runtime.

Most important lines:
- `ggml_cuda_init: failed to initialize ROCm: no ROCm-capable device is detected`
- `warning: no usable GPU found, --gpu-layers option will be ignored`
- `warning: one possible reason is that llama.cpp was compiled without GPU support`
- `load_tensors: CPU model buffer size = 35183.10 MiB`
- `llama_kv_cache: CPU KV buffer size = 3840.00 MiB`
- `sched_reserve: CPU compute buffer size = 616.02 MiB`

This is strong evidence of CPU fallback.

## Why latency is so bad
The lane is not only CPU-bound; it is also currently processing/reprocessing very large prompt state.

Observed runtime behavior from the service logs:
- repeated prompt-processing batches of `2048` tokens
- task sizes around `61733` tokens
- checkpoint creation and restoration
- forced full prompt re-processing due to lack of cache reuse

Combined with CPU fallback, this explains why:
- `GET /v1/models` returns quickly,
- but actual Qwen completions can block for much longer,
- and the upstream process keeps burning CPU after the client has given up.

## What this means operationally
The near-term problem is **not** simply a Pi timeout knob.

There are now three separate problems in play:
1. **ROCm/device visibility failure** on Prometheus (current first-order blocker)
2. **very large prompt/session processing cost** on the Qwen lane
3. **weak upstream cancellation / slot-release semantics** after client aborts

Problem (1) must be fixed first, because it distorts all latency debugging.

## Likely root-cause classes
The live evidence is consistent with one or more of:
- ROCm runtime/device access broken on Prometheus
- missing/inaccessible `/dev/kfd` or render nodes for the service context
- host driver/runtime drift since earlier successful deployment
- llama.cpp binary/backend mismatch on the live node
- service user/environment not seeing the GPU correctly

## Recommended debugging/fix order
1. **Fix ROCm/device visibility first**
   - prove whether the GPU is visible on the host at all
   - inspect `/dev/kfd`, render nodes, device groups, and ROCm tools
   - inspect service environment and host driver/runtime state
2. **Re-validate llama.cpp runtime offload**
   - confirm startup logs show real GPU offload lines, not CPU fallback
3. **Only then re-measure Qwen latency**
   - short prompt first-token latency
   - longer request latency
4. **Only after GPU is working revisit cancellation behavior**
   - otherwise CPU-bound prompt processing overwhelms the signal

## Artifact relationship
This note is the evidence companion to:
- `docs/plans/2026-03-15-prometheus-qwen-rocm-offload-and-latency-repair-plan.md`

## Sources / evidence anchors
- live Prometheus service logs for `prometheus-llama-reasoning.service`
- live process command line for PID `84479`
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
- `Research/medium/CriomOS/591912250210_report_prometheus-qwen_timeout_investigation_pi-timeout-surface_vs_upstream-runtime-behavior.md`
