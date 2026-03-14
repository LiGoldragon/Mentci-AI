# Prometheus big-model + small-subagent-model hosting patterns and current-stack fit

## Intent
Answer whether Prometheus can host:
- one heavyweight main reasoning model that stays warm,
- one or more smaller fast instruction-following models for subagents,
- and whether Pi can route subagent requests to the smaller model while the main reasoning lane remains available.

Also answer whether the current stack is already sufficient or whether a different serving setup is needed.

## Short answer
Yes, **for the common and practical meaning of this pattern**, the current stack can do it.

Specifically:
- the current Prometheus/CriomOS runtime already supports **multiple concurrently exposed models**,
- Pi already supports **per-agent model overrides**,
- so a main reasoning agent can default to the heavyweight model while subagents explicitly target a smaller model.

However, an important distinction matters:
- **weights-hot / model-resident:** yes, the large model can remain loaded and addressable while smaller-model requests go elsewhere,
- **paused live server-side conversation/KV state preserved across model hops:** not strongly guaranteed by the current stack.

So the right answer is:
- **yes for “big model stays warm while subagents hit a smaller model”**,
- **no for “the runtime transparently suspends and resumes a persistent large-model in-flight context like a scheduler-managed engine.”**

## Current stack truth

## A. Runtime already supports multiple models concurrently
The current CriomOS runtime is already a **multi-service** design:
- `prometheus-model-lock.json` declares `servedModels`
- `mkCriomOS/llm.nix` maps each served model to its own `llama-server` service
- each model gets its own port
- LiteLLM sits in front as a single gateway on port `11434`
- Pi sees canonical IDs plus aliases via the catalog and generated home-module settings

This means the current architecture is already capable of:
- serving a large main model,
- serving a smaller side model,
- and routing different calls to each one by model name/alias.

This is not hypothetical; it already does this today with:
- `main-reasoning` → Qwen3.5-35B-A3B
- `main-sanity` → Llama 3.2 1B

## B. Pi can already select different models for subagents
Pi does not appear limited to a single global model for every subagent.

Current evidence indicates:
- Pi has a global default provider/model,
- but repository-defined agents/subagents can specify their own `model:` front matter,
- and Pi’s model resolution logic prioritizes those scoped/per-agent model requests before falling back to the global default.

So the practical pattern already available is:
- main interactive reasoning uses `main-reasoning`
- subagent definitions explicitly specify a smaller model alias
- those subagent calls then go to the smaller Prometheus lane

In other words:
- **the harness can do this, if the subagents are configured to ask for that model**.

## The key distinction: warm weights vs warm session state
This is the most important conceptual boundary.

## 1. What the current stack can keep hot
The current design can keep:
- the heavyweight model binary/process running,
- the model weights loaded in memory/VRAM (if capacity permits),
- the endpoint continuously available.

That is the practical meaning of “cached/still hot” in most local serving setups.

## 2. What the current stack does not strongly guarantee
The current stack is **not** a scheduler-first engine that gives a strong guarantee of preserving and resuming a paused server-side request/session state while other models run.

Why:
- each model is a separate `llama-server` process,
- the gateway surface is OpenAI-compatible and effectively request-based,
- `--parallel 1` is configured per model,
- current prior research already showed `llama.cpp` is comparatively weak on request lifecycle/cancel/release semantics.

So if the intended question is:
> can the large model keep its live in-flight reasoning slot/session state suspended while subagents go do work, then resume exactly where it left off?

then the current answer is:
- **not as a strong runtime guarantee**.

The safer statement is:
- Pi retains the orchestration state,
- the large model remains available and likely warm,
- but each model request is still basically routed independently.

## Is a different setup needed?

## For the simple “big planner + small subagent” pattern:
**No, not necessarily.**

If your goal is simply:
- keep one large reasoning lane up,
- add one or more fast small instruction lanes,
- and have subagents explicitly use the small lanes,

then the current architecture is already the right shape:
- add more `servedModels`
- add catalog aliases
- configure the relevant subagents to use the smaller alias explicitly

That is a normal, practical OSS pattern.

## For stronger scheduling/session semantics:
**Possibly yes.**

If you want:
- transparent request suspension/resumption,
- stronger multi-request scheduling,
- deeper server-managed lifecycle control,
- or many-model elastic routing on one host,

then the current `llama.cpp` lane is not the strongest fit.

That is where people more often move toward:
- vLLM-style scheduler runtimes,
- dedicated multi-model routers,
- or swap/proxy layers such as llama-swap / newer llama.cpp router-mode patterns.

## How people do this in practice

## Common OSS pattern: supervisor/planner + workers on separate endpoints
The most common open-source pattern is:
- a large planner/supervisor model on a heavyweight endpoint,
- smaller worker/subagent/tool models on separate endpoints,
- explicit routing based on agent role.

This is usually done with:
- separate per-model services,
- a router/gateway in front,
- and per-agent configuration selecting which model to use.

This closely matches the current Prometheus architecture already.

## Common runtime pattern on a single box
On a single machine, people usually choose one of these:

### Pattern 1: Separate model servers per model
- one process per model
- stable ports/aliases
- router/gateway on top
- easiest to reason about

This is effectively what Prometheus already does.

### Pattern 2: Hot-swap / model-management layer
- one public endpoint
- load/evict models on demand
- keep heavyweight model pinned or long-TTL
- swap in smaller ones as needed

Examples in the ecosystem:
- llama-swap
- newer llama.cpp router/model-management mode
- Ollama-style keep-alive patterns

This is more flexible but also more operationally complex.

### Pattern 3: Scheduler-first runtime
- request-aware engine
- stronger lifecycle control
- often better for many concurrent requests or complex multi-model routing

This is more like the vLLM/TGI/SGLang family of answers.

## What matters most on Prometheus

## 1. VRAM budget decides whether both models can really stay hot
The largest practical constraint is not Pi or LiteLLM; it is hardware budget.

If both models are separate `llama-server` processes and both attempt GPU residency, then:
- both must fit alongside their KV/cache/headroom,
- otherwise one will partially offload, thrash, fail, or degrade sharply.

So “possible” depends on:
- the chosen quantizations,
- context sizes,
- `--n-gpu-layers`,
- and the total VRAM headroom.

## 2. The current 1B sanity lane proves the shape, not the ideal worker quality
Prometheus already has a small side lane (`main-sanity`), which proves the architectural pattern works.

But that 1B model may be too weak for serious subagent work.

So the practical next step is not architectural reinvention; it is:
- add a better fast instruction-following small model,
- give it a stable alias such as `subagent-fast` or similar,
- and point selected Pi subagents at it explicitly.

## 3. Automatic routing is not the same as capability
The current stack appears capable of this pattern, but not automatically semantically routing subagents to the best model by itself.

In practice you should assume:
- **explicit per-agent model selection**,
- not magical dynamic routing,
- unless you deliberately add a higher-level router/policy layer.

## Recommendation
For the current Prometheus lane, the best next practical answer is:
1. **Stay with the current architecture** for now.
2. Add one genuinely useful small/fast instruction model as a new dedicated lane.
3. Expose it via a clear alias in the Prometheus catalog.
4. Configure selected Pi subagents explicitly to use that alias.
5. Measure whether VRAM headroom really keeps both the big reasoning lane and the small worker lane warm enough in practice.

Only consider a different runtime setup if you later need:
- many more models,
- stronger scheduler semantics,
- or transparent model hot-swap/eviction behavior.

## Bottom line
- **Possible with current setup?** Yes.
- **Already structurally supported?** Yes.
- **Automatic/subagent-aware by default?** Not fully; configure subagents explicitly.
- **Keeps the big model warm?** Yes, if VRAM/memory budget allows.
- **Keeps a paused live big-model reasoning state with strong runtime guarantees?** Not really.
- **Need a different setup right now?** Probably not for the first “big planner + small fast worker” version.

## Sources
### Local repo evidence
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
- `Components/CriomOS/nix/homeModule/min/default.nix`
- `config/pi/prometheus-agent-settings.json`
- `.pi/agents/explore.md`
- `Research/high/Debugging/591912201622_report_subagent-empty-output_investigation_model-selection-and-event-drain.md`
- `Research/medium/Model-Catalogue/591912201737_report_current-model-allowlist_recommendations_and_google-catalogue-cleanup.md`

### External sources
- LangChain multi-agent documentation
- Comet multi-agent systems article
- Hugging Face llama.cpp model-management/router-mode article
- llama-swap README
- SitePoint memory-management article on running multiple local models
