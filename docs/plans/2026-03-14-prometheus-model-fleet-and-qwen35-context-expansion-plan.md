# Prometheus Model Fleet and Qwen3.5 Context Expansion Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Expand the Prometheus llama.cpp fleet, raise the Qwen3.5 reasoning lane context substantially, add a curated starter kit of heavyweight and fast local models, prefetch all model hashes before builds, perform the heavy Nix builds on Prometheus as user `li`, and expose the final validated fleet through both Prometheus routing and Pi-visible model selection.

**Architecture:** Treat the fleet as data-driven infrastructure. `prometheus-model-lock.json` remains the runtime source of truth for model artifacts, ports, aliases, and `ctxSize`; `prometheus-model-catalog.json` and `homeModule/min/default.nix` remain the Pi-visible source of truth; `mkCriomOS/llm.nix` remains the service generator. The work should proceed in phases: manifest/catalog design, prefetch/hashing, build-on-Prometheus validation, phased runtime activation, then Pi menu synchronization.

**Tech Stack:** Nix/fetchurl, repo-local CriomOS modules, llama.cpp runtime, GGUF model artifacts from Hugging Face, LiteLLM router (unless later redesigned), home-managed Pi settings generation, SSH to Prometheus as user `li`.

---

### Task 1: Freeze the target starter-kit manifest before touching Nix data

**TDD scenario:** Trivial change — planning/data design first, then validate with fetch/build gates.

**Files:**
- Read: `Research/medium/CriomOS/591912245010_report_prometheus-model-fleet_starter-kit_selection_and_prefetch-build-planning.md`
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: `config/pi/prometheus-agent-settings.json`

**Step 1: Lock the starter-kit model list**

Use this initial manifest shape:
- **Keep / upgrade core lanes**
  - `qwen3.5-35b-a3b` as `main-reasoning`
  - `llama-3.2-1b-instruct` as `main-sanity`
- **Add heavyweight comparison lanes**
  - newer `DeepSeek-R1-Distill-Llama-70B` GGUF lane
  - `Llama-3.3-70B-Instruct` GGUF lane
  - `Qwen2.5-72B-Instruct` GGUF lane
- **Add medium lanes**
  - `Qwen2.5-Coder-14B-Instruct`
  - one 30–32B-class Qwen or comparable model chosen at implementation time based on best reputable GGUF support
- **Add small/fast lanes**
  - `Qwen2.5-7B-Instruct`
  - one Phi-class fast model if storage/ops budget allows

**Step 2: Decide a stable alias/descriptor scheme**

For each model define:
- `modelId`
- `canonicalId`
- `alias`
- `primaryAlias`
- `serviceSuffix`
- `descriptor`
- `reasoning`
- `contextWindow`
- `maxTokens`
- `ctxSize`
- `port`

**Step 3: Reserve the port map in advance**

Document a port range for all new lanes instead of letting assignments grow ad hoc.

**Step 4: Keep Pi menu honesty**

Do not expose any model in Pi selection until its artifact URL, hash, build, and deployment path are all validated.

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.

### Task 2: Raise the Qwen3.5 context budget first

**TDD scenario:** Modifying tested config — validate with deployment/runtime checks before expanding the full fleet.

**Files:**
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: `config/pi/prometheus-agent-settings.json`
- Read/verify: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Read/verify: `Components/CriomOS/nix/homeModule/min/default.nix`

**Step 1: Choose the raised context target**

Recommended planning target:
- `196608` tokens (`192k`)

Reason:
- clearly above the operator’s `180k` requirement
- still below Qwen3.5’s native `262144` context
- avoids immediate YaRN/rope-scaling work in the first pass

**Step 2: Update all three visible data surfaces consistently**

Set Qwen3.5’s heavy reasoning lane to:
- `contextWindow: 196608`
- `ctxSize: 196608`

Synchronize this across:
- lock
- catalog
- `config/pi/prometheus-agent-settings.json`

**Step 3: Verify no hidden cap remains**

Confirm the service generator still reads `ctxSize` directly and that Pi metadata is still catalog-derived.

**Step 4: Treat this as an atomic early rollout**

Validate the context bump on its own before adding the broader fleet.

**Step 5: Finalize via `jj-agent`**

Commit/push only the Qwen context intent if it is isolated.

### Task 3: Add a prefetch-first artifact acquisition workflow

**TDD scenario:** New workflow feature — verify hashes before any large builds.

**Files:**
- Modify or create: a repo-local prefetch helper document/script location inside `Components/CriomOS` or adjacent docs if needed
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- Update: related Research note documenting URLs and hashes

**Step 1: Standardize the prefetch command shape**

Use a Nix prefetcher instead of build-fail hash capture.

Preferred command family:
- `nix-prefetch-url <direct-resolve-url>`
- or equivalent `nix-prefetch builtins.fetchurl --url ...`

**Step 2: Require final direct URLs first**

For each candidate model:
- choose the exact Hugging Face `resolve/main/...gguf` URL
- prefetch it
- record the resulting hash
- only then write the lock entry

**Step 3: Record a reproducible artifact manifest**

For every model persist:
- artifact URL
- publisher
- quantization
- filename
- resulting SHA256

**Step 4: Refuse failure-driven hash discovery**

Explicitly ban the old workflow of “write fake hash, build, catch failure, paste hash.”

**Step 5: Finalize via `jj-agent`**

Commit/push the prefetch workflow artifact separately if it becomes its own intent.

### Task 4: Plan the heavyweight build lane on Prometheus as user `li`

**TDD scenario:** Operational planning.

**Files:**
- Read: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Read: deployment-related CriomOS docs/runbooks already present
- Update: Research/runbook note if needed

**Step 1: Build where downloads are fastest**

Perform heavy Nix builds on Prometheus itself because:
- LAN wiring improves artifact acquisition
- the machine owns the runtime hardware constraints

**Step 2: Use SSH to the normal user**

Plan all Nix builds via:
- SSH to `li@prometheus...`
- user-level `nix build`

Do **not** treat root as the normal build user.

**Step 3: Separate build from activation**

The plan should distinguish:
- user-level build/evaluation/fetch stages
- any later privileged activation step

**Step 4: Keep service ownership consistent**

Preserve the existing `User = "li"` runtime model in generated systemd units.

**Step 5: Finalize via `jj-agent`**

Commit/push any runbook clarification separately if needed.

### Task 5: Expand the runtime lock manifest in phases

**TDD scenario:** Modifying tested runtime config — add one cohort at a time and verify.

**Files:**
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- Modify: `Components/CriomOS/nix/mkCriomOS/llm.nix`

**Step 1: Add the first heavyweight cohort only**

First cohort:
- Qwen3.5 context-upgraded reasoning lane
- updated DeepSeek-R1-Distill-Llama-70B lane
- Llama 3.3 70B lane

**Step 2: Add the second heavyweight cohort**

Second cohort:
- Qwen2.5-72B lane
- 30–32B-class medium-large comparison lane

**Step 3: Add the small/fast cohort**

Third cohort:
- Qwen2.5-7B
- optional Phi-class small lane
- preserve Llama 3.2 1B sanity lane

**Step 4: Keep `mkCriomOS/llm.nix` manifest-driven**

Do not hard-code special cases unless unavoidable.
Let new `servedModels` entries drive service creation, router data, and firewall rules.

**Step 5: Finalize via `jj-agent`**

Commit/push each cohort separately if the changes are staged incrementally.

### Task 6: Expand the Pi-visible catalog and selection menu only after runtime truth exists

**TDD scenario:** Modifying tested config — verify generated Pi settings after each cohort.

**Files:**
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: `config/pi/prometheus-agent-settings.json`
- Read/verify: `Components/CriomOS/nix/homeModule/min/default.nix`

**Step 1: Extend `models`, `aliasTargets`, and `enabledAliases`**

Ensure every deployed runtime lane has a Pi-visible canonical model and alias mapping.

**Step 2: Curate `declaredModelMenu` intentionally**

Group models by practical use-case, not just by size.
Example menu buckets:
- sanity / tiny
- fast general
- medium coding
- heavyweight chat
- heavyweight reasoning

**Step 3: Keep descriptors operator-friendly**

Each model descriptor should immediately convey:
- family
- size/quant
- intended role

**Step 4: Verify home-managed Pi generation still matches the catalog**

Confirm `homeModule/min/default.nix` still regenerates `.pi` settings correctly from the updated catalog.

**Step 5: Finalize via `jj-agent`**

Commit/push the Pi-menu/catalog intent separately if isolated.

### Task 7: Validate each cohort on Prometheus before widening the menu

**TDD scenario:** Verification task.

**Files:**
- Update: Research evidence note(s)

**Step 1: Validate artifact fetch/build success as user `li`**

For each model cohort confirm:
- prefetch succeeded
- lock hash is correct
- user-level build succeeds on Prometheus

**Step 2: Validate service startup**

Confirm each model actually starts through the generated runtime path.

**Step 3: Validate minimal prompt health**

For each model run an intentionally tiny prompt to confirm:
- correct alias
- service responsiveness
- no obviously runaway idle behavior

**Step 4: Validate long-context lane separately**

For Qwen3.5 verify:
- advertised/accepted context reflects the raised target
- a long prompt near the new budget is accepted without immediate failure

**Step 5: Finalize via `jj-agent`**

Commit/push only the verification/reporting intent if it becomes a separate artifact.

### Task 8: Revisit defaults after the fleet is real

**TDD scenario:** Operational policy task.

**Files:**
- Modify if needed: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify if needed: `config/pi/prometheus-agent-settings.json`

**Step 1: Keep `main-reasoning` stable initially**

Do not change the default away from Qwen3.5 until the broader fleet has been proven.

**Step 2: Decide which heavy comparison lane deserves a stable alias**

Only after real runtime tests should one of the new heavy lanes get a stable first-class alias.

**Step 3: Keep one tiny sanity lane always available**

Ensure a fast tiny model remains present for diagnostics and smoke tests.

**Step 4: Finalize via `jj-agent`**

Commit/push any default-model policy change separately from raw fleet expansion.

### Task 9: Maintain a storage and port budget ledger

**TDD scenario:** Operational hardening.

**Files:**
- Update: Research/runbook artifact documenting ports, sizes, quantizations, and disk budget

**Step 1: Record per-model disk and runtime expectations**

For each model note:
- quantization
- approximate artifact size
- port
- use-case

**Step 2: Record alias and menu mapping**

This avoids later confusion between runtime services and Pi-visible names.

**Step 3: Keep this ledger authoritative for future additions**

No future model should be added ad hoc without extending the ledger and prefetch manifest.

**Step 4: Finalize via `jj-agent`**

Commit/push the ledger artifact if added.
