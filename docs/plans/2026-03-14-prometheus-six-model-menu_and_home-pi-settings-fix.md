# Prometheus six-model menu and home Pi settings fix Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Make Pi on Ouranos operational against Prometheus with a declarative CriomOS home configuration, and add a declarative six-model Prometheus menu with one properly configured 70B/72B-class reasoning model.

**Architecture:** Extend the existing Prometheus model catalog from a single-model shape to a six-model declarative menu while keeping the current CriomOS JSON-driven structure. Fix the home Pi override by making CriomOS home generation own `~/.pi/settings.json` in addition to `~/.pi/agent/settings.json`. Keep the runtime implementation honest: at minimum, deploy a real larger-model lane for the selected reasoning winner, and ensure the Pi-visible menu is produced from declarative data rather than ad-hoc host state.

**Tech Stack:** Nix, CriomOS home module, Home Manager generation, llama.cpp, LiteLLM, JSON model catalog/lock metadata, JJ.

---

### Task 1: Fix home Pi settings ownership in CriomOS

**TDD scenario:** Modifying tested code — run existing tests/builds before and after

**Files:**
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Test: bounded Nix build of the exact home attr that emits Pi config

**Step 1: Add declarative ownership for `~/.pi/settings.json`**
- Reuse the same `piAgentSettingsJson` already used for `~/.pi/agent/settings.json`.
- Emit `".pi/settings.json".text = piAgentSettingsJson;` for Ouranos home generations.

**Step 2: Verify the exact home attr still builds**
- Run the exact home build attr after the change.

**Step 3: Finalize this intent**
- Commit only after verification evidence exists.

### Task 2: Extend Prometheus model catalog to a six-model menu

**TDD scenario:** Modifying tested code — run existing builds before and after; add data-first changes before runtime code

**Files:**
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-lock.json` (or evolve lock semantics if required)
- Modify: `config/pi/prometheus-agent-settings.json`
- Test: bounded JSON/Nix evaluation via exact attrs and generated files

**Step 1: Expand catalog from 1 → 6 declared models**
- Keep the current catalog shape (`models`, `aliasTargets`, `enabledAliases`, `defaultModel`, `provider`, `serviceEndpoints`).
- Add six exact model entries with sane `contextWindow`, `maxTokens`, and `reasoning` metadata.

**Step 2: Set the larger-model winner**
- Configure the selected 70B-class reasoning model as a real runtime lane, not only metadata.

**Step 3: Mirror the menu into top-level Pi agent settings**
- Update `config/pi/prometheus-agent-settings.json` so repo/devshell and generated home settings can expose the same menu.

### Task 3: Make the Prometheus router/runtime consume the expanded catalog honestly

**TDD scenario:** Modifying tested code — verify exact runtime/build outputs before and after

**Files:**
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Modify: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml` or replace its role with generated content
- Modify: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Test: exact Prometheus OS attr + deployManifest attr

**Step 1: Remove single-model assumptions where needed**
- Ensure router metadata and generated Pi config reflect the expanded catalog.

**Step 2: Keep runtime claims honest**
- If only one or two underlying models are truly served, do not falsely imply that all six are simultaneously distinct unless the runtime actually supports it.
- The selected 70B-class reasoning model must be real and operational.

**Step 3: Verify exact Prometheus attrs build**
- Build:
  - `.#crioZones.maisiliym.prometheus.os`
  - `.#crioZones.maisiliym.prometheus.deployManifest`

### Task 4: Deploy and verify from Ouranos

**TDD scenario:** Modifying tested code — evidence-driven runtime verification

**Files:**
- No source changes beyond earlier tasks
- Test runtime from `/home/li` with repo overrides cleared

**Step 1: Deploy Prometheus through manifest flow**
- Use `execute deploy-manifest` from inside `Components/CriomOS`.

**Step 2: Activate the correct Ouranos home profile**
- Rebuild/switch the intended home profile so `~/.pi/settings.json` is declaratively owned by CriomOS.

**Step 3: Verify generated files**
- Confirm both:
  - `~/.pi/agent/settings.json`
  - `~/.pi/settings.json`
  match intended Prometheus menu/defaults.

**Step 4: Verify real Pi usability**
- From `/home/li` with repo overrides cleared, run:
  - `pi --provider prometheus --model <winner-or-sanity> --thinking off --no-session --no-tools -p 'Reply with exactly pong.'`
- Confirm quick response, not timeout.

**Step 5: Verify the larger model**
- Run one bounded direct HTTP probe and one Pi probe against the selected 70B-class reasoning model.
- Check post-test heat/runaway state on Prometheus.

### Task 5: Optional follow-up cleanup — make LSP/startup extensions opt-in

**TDD scenario:** Modifying tested code — run exact packaged Pi checks before and after

**Files:**
- Modify: `Components/nix/pi-with-extensions.nix`
- Modify: `Components/nix/pi_with_extensions_check.nix`
- Possibly modify: `Components/nix/default.nix`

**Step 1: Remove `lsp-pi` from always-loaded startup path**
- Keep it optional / subagent-only.

**Step 2: Update checks**
- Remove assertions that require `lsp-pi` to appear in default `bin/pi` startup wiring.

**Step 3: Verify exact packaged Pi check attr**
- Run the exact top-level check attr for Pi wrapper validation.
