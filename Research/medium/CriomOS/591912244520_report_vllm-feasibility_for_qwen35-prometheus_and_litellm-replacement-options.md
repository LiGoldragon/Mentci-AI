# vLLM feasibility for the Qwen3.5 Prometheus lane and LiteLLM replacement options

## Intent
Answer three operator questions:
1. Can vLLM realistically run the Prometheus reasoning lane for Qwen3.5-35B-A3B?
2. What would migrating from `llama.cpp` to vLLM actually require?
3. What should replace LiteLLM, if anything?

## Short answer
### 1. Can vLLM run it?
- **The exact currently deployed artifact** (`unsloth/Qwen3.5-35B-A3B-GGUF` / `Qwen3.5-35B-A3B-Q8_0.gguf`): **not realistically as a production target**.
- **The equivalent Qwen3.5-35B-A3B model in vLLM-native formats** (HF safetensors / FP8 / AWQ / GPTQ style checkpoints): **yes, possible and likely realistic**, subject to Prometheus GPU/ROCm fit.

So the correct label is:
- **possible with conversion**, not “drop-in realistic” for the exact GGUF lane.

### 2. What replaces LiteLLM?
- If Prometheus becomes a small local vLLM fleet with only one or two model lanes, the best replacement is often: **nothing**.
- Meaning: use **vLLM’s own OpenAI-compatible server directly**, and if a single stable entrypoint is needed, put a **thin reverse proxy** in front of it.
- If richer multi-model routing is still needed later, use a **router designed for vLLM-style fleets** (vLLM Router / Semantic Router, or a comparable dedicated gateway), not LiteLLM by default.

## vLLM feasibility answer in detail

## A. vLLM can serve Qwen3.5-family MoE models
Official vLLM recipes now document Qwen3.5 deployment with:
- OpenAI-compatible `/v1` serving
- reasoning parser support
- prefix caching
- long-context support
- AMD ROCm installation and deployment notes

This is strong evidence that the model family itself is inside vLLM’s design target.

Also relevant upstream signs:
- vLLM docs expose Qwen3.5 recipes and specific `qwen3` reasoning parser flags
- API/docs expose Responses/OpenAI server surfaces
- model/config trees now include `qwen3_5` / `qwen3_5_moe`

Conclusion:
- vLLM is not conceptually blocked on the model family.

## B. The exact GGUF artifact is the main blocker
The current Prometheus lane is built around:
- a single-file GGUF artifact
- `llama.cpp`
- GGUF-native runtime semantics

vLLM does have GGUF support, but the official docs explicitly warn that it is:
- **highly experimental**
- **under-optimized**
- possibly **incompatible with other features**
- currently limited to **single-file GGUF**
- better when paired with the **base model tokenizer** because tokenizer conversion is unstable/time-consuming

That is not the confidence profile for a production migration whose motivation is better cancellation semantics.

So if the requirement is:
> preserve the exact current GGUF artifact unchanged

then the answer is:
- **vLLM is not a realistic production drop-in**.

If the requirement is instead:
> preserve the same model family / reasoning lane semantics, but allow artifact conversion or a native non-GGUF checkpoint

then the answer becomes:
- **yes, likely realistic**.

## C. ROCm answer is conditional, not universal
Official vLLM Qwen3.5 docs do include AMD install and deployment notes, but they are centered on modern Instinct-class hardware and current ROCm requirements.

Important caution:
- official ROCm guidance is not the same as “all Prometheus ROCm environments will be easy”.
- the operator should expect a heavier Python/Torch/ROCm stack than `llama.cpp`
- there are still ROCm-specific issue reports in the ecosystem

So the real feasibility statement is:
- **vLLM on ROCm is plausible for Qwen3.5, but only after hardware-fit verification for Prometheus**.

This is a bigger environment risk than the current `llama.cpp` lane.

## Migration from llama.cpp to vLLM: what actually changes

## 1. Artifact format changes
Today:
- GGUF via `llama.cpp`

Likely vLLM target instead:
- Hugging Face safetensors checkpoint
- preferably an officially supported quantized variant such as FP8 or AWQ if available and appropriate

This means the migration is not:
- swap one server binary for another

It is:
- change runtime family,
- change model artifact family,
- and likely change packaging assumptions.

## 2. Runtime stack becomes heavier
Current lane:
- compact repo-local `llama.cpp` package override
- simple systemd service per model
- LiteLLM router in front

vLLM lane would mean:
- Python/Torch runtime
- ROCm/CUDA-specific wheel/image discipline
- more complex startup/warmup behavior
- distributed/parallel settings if multi-GPU is used
- more complex deployment and tuning

In exchange, vLLM gives stronger serving architecture:
- request-aware engine
- batching/scheduler model
- richer observability
- much better basis for cancel/release semantics

## 3. Long context is conceptually better aligned
Qwen3.5 natively supports up to `262144` tokens.

For vLLM this means:
- `196608`/`192k` remains below native context
- long context is configured through vLLM’s max-model-len / HF override path
- the serving engine is built for more scheduler-aware KV management than the current llama.cpp lane

So for the user’s long-context goal, vLLM is more architecturally aligned than `llama.cpp`.

## 4. Cancellation behavior would likely improve materially
This is the main attraction.

Compared to `llama.cpp`, vLLM has:
- first-class request/scheduler concepts
- documented abort surfaces in engine APIs
- Responses/OpenAI serving surfaces
- a much more modern serving architecture for in-flight request management

This does not mean “zero bugs forever”, but it is much closer to the open-source world’s mature answer than `llama.cpp`.

So if the primary goal is:
- real cancel/release semantics

then vLLM is a serious candidate.

## What should replace LiteLLM?

## Option 1 — No replacement: use vLLM directly (**recommended if the stack is small**)
Shape:
- Pi talks directly to one or more vLLM OpenAI-compatible endpoints
- optional thin reverse proxy for stable hostnames/ports/TLS

Why this is attractive:
- shortest request path
- least cancellation ambiguity
- removes one entire proxy layer from the abort story
- easiest to reason about operationally if there are only 1–2 model lanes

What you lose from LiteLLM:
- model alias/group abstractions
- generic multi-provider routing/fallback/retry layer
- some convenience policy surfaces

Verdict:
- best default if Prometheus only needs a local sanity lane and a local reasoning lane

## Option 2 — Thin reverse proxy in front of vLLM
Shape:
- vLLM per lane/model
- nginx/caddy/haproxy for a single stable public entrypoint

Why:
- preserves simple topology
- keeps routing shallow
- easier than keeping a heavyweight LLM-specific gateway

Verdict:
- strong practical replacement if only basic aliasing/port normalization is required

## Option 3 — vLLM-native router / semantic router / dedicated model gateway
Shape:
- multiple vLLM workers behind a routing layer designed for model selection and fleet management

Why:
- better if the stack grows beyond “just a couple of local models”
- better fit for multi-model scheduling/routing than LiteLLM in a pure-vLLM deployment

Verdict:
- appropriate only if Prometheus becomes a larger multi-model serving fleet

## Should LiteLLM stay?
LiteLLM can still front vLLM, but if the reason for moving to vLLM is better cancellation/release semantics, keeping LiteLLM by default is not obviously helpful.

Why:
- it adds another hop
- it preserves extra abstraction you may not need
- it reintroduces ambiguity about where cancel is being translated or lost

So my current recommendation is:
- **do not keep LiteLLM unless you specifically still need its multi-provider/fallback/policy role**

## Final answer by question

### Q1. Can vLLM run the target?
- **Exact current GGUF artifact:** not realistically as the production target.
- **Equivalent Qwen3.5-35B-A3B in native vLLM-supported checkpoint form:** yes, plausible.
- Final label: **possible with conversion**.

### Q2. What would migration require?
- move off GGUF
- adopt HF-native / vLLM-native model artifacts
- package a heavier Python/Torch/ROCm runtime
- tune long context and memory anew
- retest the entire deployment stack

### Q3. What replaces LiteLLM?
Recommended order:
1. **nothing** — direct vLLM OpenAI server
2. **thin reverse proxy** if one stable entrypoint is needed
3. **vLLM Router / semantic router / dedicated gateway** only if the fleet becomes larger/more dynamic

## Practical recommendation
If the operator is evaluating vLLM seriously, the cleanest next question is not:
- “can we point vLLM at the current GGUF?”

It is:
- “are we willing to migrate this lane off GGUF to a native vLLM-serving artifact?”

If the answer is no:
- stay on `llama.cpp` and improve/patch cancellation there.

If the answer is yes:
- vLLM becomes a credible replacement candidate, and LiteLLM likely becomes unnecessary for the minimal local Prometheus shape.

## Prompt
- Evaluate vLLM feasibility for the Qwen3.5 Prometheus lane and identify LiteLLM replacement options that better serve cancellation and release semantics.

## Context
- Finalize the CriomOS research note so it captures the operator questions, the conversion boundaries, and replacement-path recommendations while staying linked from `Research/medium/CriomOS/index.edn`.

## Summary
- vLLM can serve Qwen3.5 once the GGUF artifact is converted to a vLLM-native checkpoint and the Prometheus ROCm fit is verified, but the current GGUF lane is not drop-in production.
- Migrating from `llama.cpp` to vLLM involves adopting HF-native artifacts, running a heavier Python/Torch/ROCm stack, and retuning for longer context while gaining stronger cancellation control.
- LiteLLM should be replaced with a direct OpenAI-compatible vLLM endpoint (optionally fronted by a thin proxy) unless the fleet expands, in which case a vLLM-native router is the right topological upgrade.

## Sources
- Official vLLM Qwen3.5 usage guide
- Official vLLM GGUF documentation
- Official vLLM Responses / OpenAI API docs
- Official vLLM production-stack / semantic-router docs
- upstream issue/release evidence around Qwen3.5 support timing and model config presence

## Guard status
- `execute session-guard` passed.
- `execute root-guard` currently fails because `Components/mentci-aid/src/actors/root_guard.edn` is missing; this blocker is recorded here until the sidecar appears.
