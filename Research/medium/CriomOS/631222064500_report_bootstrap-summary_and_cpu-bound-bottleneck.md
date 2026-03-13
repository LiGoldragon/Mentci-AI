# Session Report: Ouranos/Prometheus Mesh + Model Serving Bootstrap

## Executive Summary
This session established a stable, encrypted overlay mesh between `ouranos` and `prometheus` using Headscale and Tailscale, and performed the initial end-to-end wiring for Prometheus-based model serving via an Ouranos LiteLLM gateway.

## Current State
### 1. Overlay Mesh (Headscale + Tailscale)
- **Status:** **Functional and Stable.**
- **Implementation:** Headscale running on `ouranos` (via direct TLS bootstrap); `ouranos` and `prometheus` enrolled as Tailscale clients.
- **Connectivity:** Nodes communicate over stable overlay IPs (`100.64.0.2` / `100.64.0.1`).
- **DNS:** Unbound split-DNS is configured on both nodes to forward `*.tailnet.maisiliym.criome` requests to `100.100.100.100` (Tailscale MagicDNS listener).

### 2. Prometheus Model Serving
- **Status:** **Functional, but CPU-bound.**
- **Serving:** `prometheus-deepseek-70b` service is active and successfully loads the first shard (`DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf`).
- **Key Observation:** The system successfully loads the model, but inference is running on **CPU only** (`CPU_Mapped` tensors, no ROCM/GPU logs).
- **Blocker:** The current `pkgs.llama-cpp` nix derivation does not have ROCm/GPU offloading enabled for the Strix Halo/gfx1151 hardware.

### 3. Ouranos LiteLLM Gateway
- **Status:** **Working.**
- **Routing:** Successfully routed to the Prometheus overlay IP (`100.64.0.1:11436`).
- **Configuration:** LiteLLM is correctly detecting the Prometheus-hosted model groups (`prometheus-main-deepseek`, etc.) and routing requests.

## Known Blockers & Next Actions
1. **GPU Offloading (Highest Priority):** The model is serving on CPU (slow).
   - **Fix:** Update `Components/CriomOS/nix/homeModule/min/default.nix` to use `pkgs.llama-cpp-rocm` or a custom-built ROCm-enabled derivation, and verify `ROCM=1` in the `llama-server` start logs.
2. **Gateway Response Parsing:**
   - LiteLLM occasionally encounters `Expecting value: line 1 column 1` when communicating with the llama-server.
   - Investigate if this is an idle-timeout or protocol/stream-framing mismatch between `litellm` and the specific `llama.cpp` router-mode output.
3. **DNS Polish:**
   - While `dig` over the overlay works, system-wide `getent` resolution for the tailnet domain is still brittle. Finalize the split-DNS logic once the network stack is stabilized.

## VCS State
- **Parent Repo:** Pushed to `dev` (`30458bd7`).
- **Nested Repo:** Pushed to `ouranos-litellm-mvp` (`1572da28`).
- **Working Copies:** Both are clean, with fresh empty commits (`cc38872b` / `a1125736`) on top of the pushed heads.

---
*Persisted for post-compaction context recovery.*
