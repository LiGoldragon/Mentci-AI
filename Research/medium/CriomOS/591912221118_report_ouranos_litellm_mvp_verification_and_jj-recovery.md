# Report: Ouranos LiteLLM MVP Verification and JJ Recovery

## Prompt
The user asked for strong anti-Git guardrails, correction of the mistaken Git-centric handling in `Components/CriomOS`, and completion of the in-progress LiteLLM-over-Prometheus/Ouranos MVP.

## Context
The work spans two JJ repos:
- parent repo: `/home/li/git/Mentci-AI--dev`
- nested JJ repo: `/home/li/git/Mentci-AI--dev/Components/CriomOS`

A prior mistake treated `Components/CriomOS` as if its visible `.git` boundary justified direct Git finalization. JJ later imported that backend movement, so the safe recovery path was to reassert JJ authority in the nested repo, continue on the JJ-visible `ouranos-litellm-mvp` line, and harden repo-wide guardrails so this does not recur.

## Summary
### Parent repo work completed
- Added a proxy-capable LiteLLM package surface:
  - `Components/nix/litellm-proxy.nix`
  - `Components/nix/default.nix`
  - `Components/nix/common_packages.nix`
  - `flake.nix`
- Verified:
  - `nix build .#litellm_proxy`
  - `which litellm && which litellm-proxy`
  - `litellm --help`
- Hardened anti-direct-Git JJ policy surfaces:
  - `Core/VersionControlProtocol.md`
  - `.pi/skills/independent-developer/SKILL.md`
  - `.pi/skills/sema-programmer/SKILL.md`
  - `.pi/skills/finishing-a-development-branch/SKILL.md`
  - `.pi/skills/subagent-driven-development/SKILL.md`
  - `.pi/agents/jj-agent.md`
  - `.pi/agents/jj-expert.md`
  - `.pi/commands/jj-agent.md`
  - `.pi/commands/jj-expert.md`

### Nested CriomOS JJ repo work completed
JJ-only current active line:
- bookmark: `ouranos-litellm-mvp`
- finalized revision: `8405307e`
- empty working copy above it: `b6aad27f`

Implemented in `Components/CriomOS/nix/homeModule/min/`:
- declarative LiteLLM router config:
  - `litellm-router.yaml`
- user-space Home Manager service:
  - `systemd.user.services.litellm-gateway`
- Home Manager-managed `pi` client defaults:
  - `.pi/agent/models.json`
  - `.pi/agent/settings.json`

### Runtime blocker discovered and fixed
Original router config failed because:
1. bare model IDs (`deepseek-r1:latest`, `qwen2.5-coder:7b`) lacked provider metadata for LiteLLM
2. fallback entries used an invalid dictionary shape

Fix applied in router YAML:
- Prometheus routes now use Ollama-specific provider metadata:
  - `custom_llm_provider: ollama`
  - `model: ollama_chat/deepseek-r1:latest`
  - `model: ollama_chat/qwen2.5-coder:7b`
- fallback entries now use LiteLLM-compliant single-key dictionary form

## Verification Evidence
### Parent repo packaging
Verified previously during Task 1:
- `nix build .#litellm_proxy`
- `nix develop . --command bash -lc 'which litellm && which litellm-proxy'`
- `nix develop . --command bash -lc 'litellm --help >/dev/null && echo HELP_OK'`

### Nested CriomOS synthesis
Verified during Task 2/3/4:
- `nix build github:criome/CriomOS/develop#crioZones.maisiliym.ouranos.hom.li.light --no-link --refresh`

### LiteLLM local runtime verification
Bounded local verification against the router config succeeded after the fix:
- built package: `nix build .#litellm_proxy`
- started CLI:
  - `./result/bin/litellm --config /home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/litellm-router.yaml --host 127.0.0.1 --port 11435`
- queried models:
  - `curl http://127.0.0.1:11435/v1/models`
- result: HTTP 200 with configured aliases including:
  - `main`
  - `subagent`
  - `fast`
  - `prometheus-deepseek`
  - `prometheus-qwen`

### Current JJ state
Parent repo:
- working copy contains only the anti-Git guardrail changes listed above
- parent target line before finalization of those guardrails:
  - `dev = 6c5f933d Package LiteLLM proxy`

Nested CriomOS repo:
- `jj status` clean
- `@ = b6aad27f` empty
- `@- = 8405307e ouranos-litellm-mvp | intent: complete ouranos LiteLLM gateway MVP wiring`

## Recovery Outcome
The Git-centric detour was corrected by returning to JJ authority in `Components/CriomOS`. The imported backend commits were not treated as authoritative Git state; instead, the accepted work was continued and sealed on the JJ-visible `ouranos-litellm-mvp` line. The repo-wide policy surfaces now explicitly forbid using direct Git commit/branch workflows in nested JJ repos just because a `.git` directory is visible.

## Remaining Gaps
1. A true live `pi` probe through the Home Manager-installed local gateway on the intended machine still remains desirable for end-to-end operator confirmation.
2. The nested CriomOS line is finalized locally under `ouranos-litellm-mvp`, but integration/push policy for that repo still depends on the desired target bookmark strategy for CriomOS work.
3. The parent repo still needs finalization/push for the anti-Git guardrails and this report.

## Recommendation
- Finalize and push the parent repo guardrail/report work on `dev`.
- Keep `Components/CriomOS` on `ouranos-litellm-mvp` as the preserved JJ line for the MVP until an explicit CriomOS integration target is chosen.
- Continue using JJ-only handling for `Components/CriomOS`; no further direct Git workflow there.
