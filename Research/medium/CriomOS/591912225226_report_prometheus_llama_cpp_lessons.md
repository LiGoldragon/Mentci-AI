# Prometheus `llama.cpp` lessons for gfx1151

## Solar time
- `591912225226`

## Summary
- Verified that the safest local inference path on `prometheus` now comes through `llama.cpp`, keeping the hardware stable while still delivering DeepSeek reasoning.
- Recorded the router/service wiring, the chosen quantized target, and the known blocker so future work can pick up the validation and rollback story immediately.

## gfx1151 profile
- `gfx1151` is the RDNA 3.5 integrated GPU core used by the high-end Strix Halo (a.k.a. Strix Point Halo) APUs such as AMD’s Ryzen AI MAX+ 395 / Radeon 8060S stack, offering 40 compute units and the 192 KB per-SIMD VGPR file that RDNA3+ products enjoy while retaining the broader `gfx11` ISA name.
- The silicon is exciting for local inference but, as of early 2026, ROCm still treats Strix Halo as a work-in-progress entry (see the TinyComputers upgrade note and WCCFTech roundup), so driver maturity remains uncertain.

## Why `llama.cpp` on this hardware
- GPU acceleration is not yet a stable bet for `gfx1151`; even ROCm 7.2 only exposes a generic `gfx11` profile, so relying on a CPU/quantized stack avoids rebuilding drivers for every firmware drop.
- DeepSeek-R1-Distill-Llama-70B-Q8_0 stays inside roughly 70 GB on disk → ≈55–60 % of the 128 GiB RAM target, which means the heavy lifting happens in RAM/LLM quantization rather than GPU kernels.
- `llama.cpp` exposes an OpenAI-compatible `llama-server`, which lets both LiteLLM and `pi` keep their existing alias (`main`) without touching the broader Ouranos service graph.

## Model + service/router shape
- Model: `DeepSeek-R1-Distill-Llama-70B-Q8_0 (GGUF)` stored under `$HOME/.local/share/prometheus-llama/models/` and exposed through the `prometheus-deepseek` alias defined in `~/.config/prometheus-llama/models.ini` (`load-on-startup = false`).
- Service: user-level `systemd` unit `prometheus-llama-server` runs `llama.cpp`’s `llama-server` bound to `127.0.0.1:11436/v1` so the model is hot but lazy-loaded when the first request arrives.
- Router: the LiteLLM router now points the `main` alias at `http://127.0.0.1:11436/v1` with the fixed `sk-no-key-required` API key, leaving the `prometheus-qwen` alias and cloud targets untouched while routing both LiteLLM and `pi` traffic through this local server.

## Validation blocker
- The Nix evaluation path under `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/pkdjz/` still expects a `buildNvimPlogin` entry, but the only file in that directory is `bildNvimPlogin`, so `nix eval` or `nix develop` cannot import the helper needed to revalidate the new service wiring.
- Until the `buildNvimPlogin` artifact is restored (possibly by renaming the existing file or providing a real derivation), I cannot confirm the home-manager module that wires `prometheus-llama-server` and the LiteLLM flag builds without the missing path error.

## Rollback story
- `prometheusUseOllamaFallback` toggles whether the old `ollama` tunnel is fed back into LiteLLM; setting it to `true` adds the existing port 11434 service block back to `systemd.user.services` and keeps the previous OpenAI tunnel/Router pairing alive.
- After flipping the flag, a `home-manager switch` reloads both the tunnel unit and the LiteLLM router so that `http://127.0.0.1:11434/v1` with the original alias/credentials is again the default surface, while the new `prometheus-llama-server` can remain online for testing or be stopped safely.

## Sources
- Internal (stack verification): `Research/high/prometheus-llama-stack.md`
- External cosmetic context: https://wccftech.com/amd-begins-driver-support-for-rdna-3-5-gfx1151-gpus-for-high-end-strix-halo-apus/ and https://tinycomputers.io/posts/upgrading-rocm-7-0-to-7-2-on-amd-strix-halo-gfx1151/