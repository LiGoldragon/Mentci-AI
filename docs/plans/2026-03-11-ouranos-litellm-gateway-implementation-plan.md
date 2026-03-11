# Ouranos LiteLLM Gateway Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Build a user-space LiteLLM gateway in `crioZones.maisiliym.ouranos.hom.li.light` that exposes semantic aliases (`main`, `subagent`, `fast`) over one localhost OpenAI-compatible endpoint, routing first to Prometheus over tailnet and then to cloud fallbacks.

**Architecture:** Package a proxy-capable LiteLLM runtime with the required `proxy` extras, then wire it into the Ouranos home-user module as a user service plus declarative config files. Keep all routing policy in data/config, keep system-level CriomOS on `ouranos` untouched for now, and verify the home activation package plus local gateway behavior before rebinding `pi` clients.

**Tech Stack:** Nix derivations under `Components/nix/`, CriomOS Home Manager modules under `Components/CriomOS/nix/homeModule/`, LiteLLM proxy config (`model_list`, `router_settings`, `litellm_settings`), Home Manager `home.file` / `xdg.configFile` / `systemd.user.services`, curl-based OpenAI-compatible endpoint checks, and bounded JJ-driven commit checkpoints.

---

### Task 1: Package a proxy-capable LiteLLM runtime

**TDD scenario:** New feature — full TDD cycle where packaging verification is the red/green signal.

**Files:**
- Create: `Components/nix/litellm-proxy.nix`
- Modify: `Components/nix/default.nix`
- Modify: `Components/nix/common_packages.nix`

**Step 1: Add the failing package surface**
Create `Components/nix/litellm-proxy.nix` as a focused derivation or wrapper that produces a proxy-capable LiteLLM runtime, not just the base `python3Packages.litellm` package. Prefer a single-attrset Nix function and keep dependency decisions explicit.

**Step 2: Wire it into the package graph**
Expose the new package through `Components/nix/default.nix` and add it to `Components/nix/common_packages.nix` so the runtime is available to development and home-manager consumers.

**Step 3: Run the packaging verification to watch the initial failure (if the first attempt is incomplete)**
Run:
- `nix build .#litellm_proxy`
- `nix develop . --command bash -lc 'which litellm && litellm --help'`
Expected initial red state if incomplete: missing dependency or runtime import failure in proxy mode.

**Step 4: Complete the proxy extras packaging**
Add the missing dependencies required for `litellm[proxy]` / `litellm-proxy-extras` so proxy mode can actually start. Keep this logic in Nix/package space rather than shell scripts.

**Step 5: Verify green**
Run:
- `nix build .#litellm_proxy`
- `nix develop . --command bash -lc 'which litellm && which litellm-proxy'`
- `nix develop . --command bash -lc 'litellm --help >/dev/null'`
Expected: package builds, binaries exist, and CLI help succeeds.

**Step 6: Finalize via `jj-agent`**
Ask `jj-agent` to finalize the packaging intent, push the runtime target bookmark, and verify bookmark alignment. Use `jj-expert` only if `jj-agent` misbehaves.

---

### Task 2: Add declarative LiteLLM routing config to the Ouranos home module

**TDD scenario:** New feature — full TDD cycle using activation-package synthesis and config validation.

**Files:**
- Create: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`
- Modify: `Components/CriomOS/nix/homeModule/default.nix` (only if a new imported module file is introduced)
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`

**Step 1: Write the declarative routing config**
Create `litellm-router.yaml` containing:
- `model_list`
- `router_settings`
- `litellm_settings`
- semantic aliases (`main`, `subagent`, `fast`)
- explicit backend aliases (`prometheus-deepseek`, `prometheus-qwen`, `cloud-reasoning`, `cloud-coder`, `cloud-fast`)
- ordered fallback relations
Keep addresses, provider names, and alias mappings in data, not in shell snippets.

**Step 2: Write the failing home-module wiring**
Extend `Components/CriomOS/nix/homeModule/min/default.nix` so the config is delivered into the user home via `xdg.configFile` or `home.file`, and reference the new `litellm_proxy` package in `home.packages` or the appropriate package list.

**Step 3: Run the first synthesis check**
Run:
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`
Expected initial red state if wiring is incomplete: Home Manager evaluation failure, missing package attribute, or missing file path.

**Step 4: Fix synthesis issues minimally**
Adjust module imports, package references, and file paths until the home activation package synthesizes successfully.

**Step 5: Verify green**
Run:
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`
Expected: activation package builds cleanly.

**Step 6: Finalize via `jj-agent`**
Ask `jj-agent` to finalize the home-config intent and verify push alignment.

---

### Task 3: Add a user-space LiteLLM service for Ouranos

**TDD scenario:** New feature — full TDD cycle using user-service generation and local gateway startup.

**Files:**
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Reuse: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`

**Step 1: Add the failing service declaration**
Declare a `systemd.user.services` LiteLLM service in the Ouranos home module that:
- binds to localhost only
- points at the declarative config file
- uses the packaged proxy-capable LiteLLM binary
- stays scoped to the intended home-user surface
Guard it so this behavior remains specific to the intended node/user profile and does not become a broad unwanted default.

**Step 2: Verify service synthesis exists**
Run:
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`
Expected red state if incomplete: malformed user service declaration or missing config path/package.

**Step 3: Implement the minimal fixes**
Fix only what is needed for a valid user service plus config linkage.

**Step 4: Verify green at synthesis level**
Run:
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`
Expected: home package builds cleanly with the service included.

**Step 5: Verify runtime shape locally**
Using a bounded activation/runtime environment on the target machine or equivalent dev environment, run:
- user service start/status command for the LiteLLM gateway
- `curl http://127.0.0.1:<gateway-port>/v1/models`
Expected: the service starts and exposes the alias-visible model surface.

**Step 6: Finalize via `jj-agent`**
Ask `jj-agent` to finalize the service-wiring intent and verify push alignment.

---

### Task 4: Rebind local client defaults to the gateway

**TDD scenario:** Modifying existing tested/user-known behavior — verify the current surface, then update, then verify again.

**Files:**
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Possibly create or write through Home Manager to user files corresponding to:
  - `~/.pi/agent/models.json`
  - `~/.pi/agent/settings.json`

**Step 1: Confirm the current client-binding pattern**
Mirror the already-documented Prometheus-side `pi` config approach, but adapt it so Ouranos clients point at the local LiteLLM endpoint instead of directly at one provider.

**Step 2: Add the minimal rebinding config**
Write the Home Manager-managed client config so:
- the local endpoint is the stable API surface
- semantic aliases are preserved
- provider/model defaults do not hardcode backend details into day-to-day clients

**Step 3: Run the activation-package verification**
Run:
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`
Expected red state if incomplete: malformed file generation, JSON shape issue, or conflicting config paths.

**Step 4: Fix minimally and verify green**
Run:
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`
- one bounded `pi`/client probe against the local gateway once the service is running
Expected: local tools can talk to the gateway surface without provider-specific rewiring.

**Step 5: Finalize via `jj-agent`**
Ask `jj-agent` to finalize the client-binding intent and verify push alignment.

---

### Task 5: End-to-end verification, review, and report

**TDD scenario:** Verification and review checkpoint — no new feature code unless a review/test failure demands it.

**Files:**
- Modify or create a report under `Research/medium/CriomOS/` if new evidence or blockers appear
- Optionally update `docs/plans/2026-03-11-ouranos-litellm-tailnet-routing-design.md` only if the validated implementation details materially refine the design

**Step 1: Run the full bounded verification packet**
Run and capture evidence for:
- `nix build .#litellm_proxy`
- `nix develop . --command bash -lc 'which litellm && which litellm-proxy'`
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`
- user-space gateway startup/status on the target environment
- `curl http://127.0.0.1:<gateway-port>/v1/models`
- one bounded client probe through `pi` or equivalent local tool

**Step 2: Request spec review**
Dispatch a reviewer subagent to check whether the MVP matches the approved architecture and constraints:
- user-space only
- Ouranos home surface only
- semantic aliases present
- Prometheus-primary routing plus cloud fallback represented
- no premature system-level `ouranos` CriomOS mutation

**Step 3: Request code quality review**
Dispatch a reviewer subagent to check for Nix packaging quality, home-module hygiene, config clarity, and runtime-closure correctness.

**Step 4: If review issues exist, re-implement and re-verify**
Fix only validated review findings, then re-run the affected verification commands.

**Step 5: Persist the implementation outcome**
Write or update a Research artifact capturing:
- what was implemented
- what remains blocked (if anything)
- exact verification evidence
- whether `ouranos` system-level CriomOS changes are still deferred

**Step 6: Finalize via `jj-agent`**
Ask `jj-agent` to finalize the verification/report intent, push the bookmark, and leave the clean handoff state.

---

## Review/Execution Notes
- Execute this plan using `/skill:subagent-driven-development` in this session.
- Do not perform non-trivial shell/package probing in the main context; use subagents for implementation, testing, and reviews.
- Keep `ouranos` system-level CriomOS untouched for now. The user must be asked first before any later host-level integration.
- Prefer declarative config and Home Manager file generation over ad-hoc shell scripts.
- If external packaging/docs evidence changes, persist the new findings under `Research/medium/CriomOS/` before claiming architectural superiority.
