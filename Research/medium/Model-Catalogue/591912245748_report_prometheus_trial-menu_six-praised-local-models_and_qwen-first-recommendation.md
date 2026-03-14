# Prometheus trial menu: six praised local models and Qwen-first recommendation

## Intent
Capture a bounded external-research menu of six highly regarded local/self-hostable open models spanning size categories, with a recommendation for the first larger-model trial after the current sanity lane.

## Research basis
This menu was synthesized through the project web-search lane using current open-model roundups, community local-inference discussions, and deployment-oriented model summaries.

The term "praised" here means:
- repeatedly recommended in current open-model discussions,
- commonly used in local inference stacks,
- and plausible to source in GGUF or related local-serving formats.

## Recommended six-model trial menu

### 1. Tiny
**Ministral 3**
- Approx size: ~3B
- Why praised: very strong efficiency and useful reasoning for a tiny local model
- Likely local format: GGUF/community quants
- Caution: limited depth on harder coding/reasoning tasks

### 2. Small
**Mixtral / Mixtral-8x22B class**
- Approx size: ~22B active MoE class
- Why praised: strong quality-per-parameter and good instruction performance
- Likely local format: GGUF/GPTQ/AWQ community releases
- Caution: MoE/runtime behavior can complicate serving and tuning

### 3. Medium
**Qwen 2.5 / Qwen coder-family ~30B class**
- Approx size: ~30B
- Why praised: strong coding, multilingual, and instruction-following reputation
- Likely local format: GGUF/GPTQ/AWQ broadly available
- Caution: some variants differ a lot in reasoning profile; exact variant choice matters

### 4. Upper-medium
**DeepSeek-R1 distilled ~32B class**
- Approx size: ~32B
- Why praised: especially strong chain-of-thought / reasoning reputation in open-model discussions
- Likely local format: GGUF and other community quants
- Caution: rapidly evolving family; exact distillate and quant quality need verification

### 5. Large
**Llama 3 70B class**
- Approx size: ~70B
- Why praised: consistently strong broad open-model baseline with extensive tooling support
- Likely local format: GGUF and multiple quant formats
- Caution: serious VRAM/offload demands

### 6. XLarge
**Qwen 72B class**
- Approx size: ~72B
- Why praised: strong reputation for coding, multilingual quality, and broad usefulness as an open general-purpose large model
- Likely local format: GGUF/GPTQ/AWQ community variants
- Caution: large hardware demands and quant choice matters a lot

## Best first larger-model candidate after `main-sanity`
**Qwen 2.5 72B instruct-family**

Why this is the recommended first larger-model trial:
- strongly praised in current local-model discussions
- broad ecosystem support
- plausible local-serving path compared with more exotic alternatives
- good fit for the operator’s stated desire to "try qwen"

## Practical operator interpretation
If the goal is to get to a meaningful larger-model trial quickly, the most sensible sequence is:
1. keep the current sanity lane as the known-good baseline
2. activate one larger Qwen lane next
3. only then expand into a wider six-model menu if the serving/runtime path stays healthy

## Important caution
This menu is a bounded external-research shortlist, not a deployment claim.

Before implementation, each exact candidate should still be checked for:
- precise upstream artifact choice
- GGUF/community quant availability
- license/usage fit
- realistic Prometheus runtime fit under the current ROCm/offload lane

## Recommendation for the next implementation lane
The best next larger-model experiment is:
- **one Qwen large-model lane first**

Only if that works cleanly should the system grow into a broader six-model trial menu.
