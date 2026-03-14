# Prometheus model-fleet starter kit selection and prefetch/build planning

## Intent
Preserve the planning inputs for a Prometheus model-fleet expansion lane that must:
- raise the Qwen3.5 reasoning context substantially,
- add a broader starter kit of local llama.cpp-served GGUF models,
- use Nix prefetching to acquire hashes before writing locks,
- perform Nix builds on Prometheus as user `li` rather than via root,
- expose the expanded fleet both in Prometheus routing and in Pi-visible model selection.

## Current local wiring truth
The authoritative configuration/control surfaces are:
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- `config/pi/prometheus-agent-settings.json`
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
- `Components/CriomOS/nix/homeModule/min/default.nix`

Key structural facts:
- `prometheus-model-lock.json` is the runtime truth for served models, `ctxSize`, ports, artifact URLs, and hashes.
- `mkCriomOS/llm.nix` reads that lock and emits the actual `llama-server` systemd services plus the LiteLLM router config.
- `prometheus-model-catalog.json` is the Pi-visible catalog/menu truth.
- `homeModule/min/default.nix` derives `~/.pi/agent/models.json`, `~/.pi/agent/settings.json`, and `~/.pi/settings.json` from the catalog.
- Current fleet is still only:
  - `main-sanity` (Llama 3.2 1B)
  - `main-reasoning` (Qwen3.5-35B-A3B)
- Qwen3.5 currently sits at `32768` context in both runtime and Pi-visible metadata.

## Operator requirements captured here
The requested plan must cover:
1. raise Qwen3.5 context,
2. expand the available model fleet,
3. retry DeepSeek-R1 distilled 70B, preferably a newer/better variant,
4. use a Nix prefetcher for hashes rather than failure-driven hash discovery,
5. do builds on Prometheus over SSH as user `li`, not root,
6. create a broad starter kit including both 70–100 GB VRAM-class models and small fast models,
7. expose the new models through llama.cpp selection and Pi selection,
8. remain planning-only in this lane.

## Recommended starter kit
### Heavy / flagship lanes (roughly 70–100 GB VRAM class and adjacent high-value lanes)
These are the strongest candidates to plan for first, balancing popularity, capability, and likely GGUF availability.

1. **Qwen3.5-35B-A3B**
- keep as the main reasoning lane
- raise context significantly (recommended planning target: `196608`)
- preserve `main-reasoning`

2. **DeepSeek-R1-Distill-Llama-70B**
- retry this lane using the newer `R1-0528`-era distill family if the GGUF publisher/version is available and reputable
- use as a dedicated reasoning comparison lane, not the default at first

3. **Llama 3.3 70B Instruct**
- broad community adoption
- strong chat/general-purpose baseline
- useful as a non-DeepSeek, non-Qwen heavyweight control lane

4. **Qwen2.5-72B-Instruct**
- large general/reasoning baseline with broad reputation and long-context friendliness
- complements Qwen3.5 MoE by providing a dense-style heavyweight lane

### Medium / capable but cheaper lanes
5. **Qwen2.5-Coder-14B-Instruct**
- keep or refresh as the medium coding/reasoning lane already hinted by the catalog
- useful for cheaper development tasks

6. **Qwen3 30B-A3B or Qwen3 32B-class instruct/reasoning model**
- medium-large option for better throughput than the 70B-class lanes
- should be selected based on the best currently reputable GGUF availability during implementation

### Small / fast starter-kit lanes
7. **Llama 3.2 1B Instruct**
- keep as the main sanity lane

8. **Qwen2.5 7B Instruct**
- very good quick-turn general lane
- small enough for fast validation

9. **Phi-3 Mini / Phi-class small instruct model**
- ultra-fast fallback/sanity/chat lane
- especially useful for testing prompts and stack health quickly

10. **Mixtral 8x7B or a similarly well-supported small/medium MoE**
- useful if the operator wants a fast but capable comparison lane
- include only if storage/ops budget permits after the first 8–9 models are planned

## Recommended artifact and quant strategy
### Prefer reputable GGUF publishers
Use Hugging Face-hosted GGUFs from reputable sources such as:
- official/first-party if available
- `unsloth`
- `bartowski`
- `ggml-org`
- other proven maintainers only with care

### Quantization planning defaults
For large 70B-class lanes, start planning around:
- `Q4_K_M` as the default “fits + quality” starting point
- evaluate `Q5_K_M` only if VRAM/headroom and latency permit
- avoid planning around `Q8_0` for multiple heavy lanes unless there is explicit capacity justification

For small/fast lanes:
- `Q4_K_M` is usually the correct default

## Prefetch policy
The plan should not rely on Nix build failure to reveal hashes.

Preferred prefetch shapes:
- `nix-prefetch-url <resolved-huggingface-resolve-url>`
- or equivalent `nix-prefetch` / `builtins.fetchurl` workflow

Workflow requirements:
1. collect final direct artifact URLs,
2. prefetch them up front,
3. write the resulting hashes into `prometheus-model-lock.json`,
4. only then perform the real Nix builds.

This should be recorded in the plan as mandatory operator discipline.

## Build/deploy policy
All heavy Nix builds for this lane should be planned to occur on Prometheus itself:
- via SSH
- as user `li`
- using normal user build rights
- avoiding root for builds unless a later deploy step truly requires privileged activation

This matches the current runtime service ownership model in `mkCriomOS/llm.nix`, where services already run under `User = "li"`.

## Main planning risks
1. **Context growth risk**
- raising Qwen3.5 to ~192k will materially increase KV/cache pressure and latency
- the plan must require bounded long-context validation before advertising success

2. **Fleet growth risk**
- many heavy GGUFs increase disk usage, download time, build/eval time, and service/port pressure
- the plan should phase the rollout rather than enabling every model at once without validation

3. **Menu honesty risk**
- Pi-visible catalog entries must not advertise models that have not yet been fetched, built, and deployed successfully
- catalog/menu work must track actual runtime availability

4. **Port and alias sprawl**
- every added model needs a consistent `serviceSuffix`, `primaryAlias`, `alias`, and port assignment
- the plan must reserve and document the port map rather than growing it ad hoc

## Practical recommendation
The best plan structure is:
1. raise Qwen3.5 context first,
2. establish a model manifest and port/alias scheme,
3. prefetch hashes for the full starter kit,
4. build on Prometheus as `li`,
5. deploy in phases,
6. only then expand Pi menus and defaults to match reality.

## Compact truth
> The Prometheus model-fleet expansion should be treated as a manifest-driven Nix/data evolution, not as ad hoc service edits. The runtime truth lives in the lock, the Pi-visible truth lives in the catalog/home-module, hashes must be acquired with `nix-prefetch-url` before builds, and heavy builds should happen over SSH on Prometheus as user `li`. The recommended starter kit centers on Qwen3.5, DeepSeek-R1-Distill-Llama-70B (newer distill generation), Llama 3.3 70B, Qwen2.5 72B, plus medium and small fast lanes like Qwen2.5 Coder 14B, Qwen2.5 7B, and Llama 3.2 1B.
