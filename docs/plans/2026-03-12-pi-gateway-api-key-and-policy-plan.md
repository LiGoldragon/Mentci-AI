I'm using the writing-plans skill to create the implementation plan.
# Pi Gateway API Key & Policy Update Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Revert the local authless Pi modifications, wire the Ouranos LiteLLM gateway through a non-empty API key, and encode the "fork upstream rather than patch" rule in the high-authority docs/skills.

**Architecture:** Step 1 restores the Pi derivations and extension checks to the pushed baseline so we operate on an untouched package/runtime. Step 2 updates the CriomOS home-module generator and the local `.pi/agent/models.json` to annotate the gateway provider with a stable API key, keeping the generated JSON in sync with the runtime state. Step 3 touches the high-authority guidance files to record the policy that behavioral changes in upstream-like dependencies should be satisfied by forks instead of in-place patches.

**Tech Stack:** Nix expressions under `Components/nix/` and `Components/CriomOS/nix/homeModule/min/`, generated JSON under `.pi/agent/`, and Markdown/skill documentation files within `Core/` and `.pi/skills/`.

---

### Task 1: Revert the local Pi authless patches

**TDD scenario:** Trivial change — no code execution needed, only metadata verification.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Components/nix/pi.nix`
- Modify: `/home/li/git/Mentci-AI--dev/Components/nix/pi-dev.nix`
- Modify: `/home/li/git/Mentci-AI--dev/Components/nix/pi_with_extensions_check.nix`

**Step 1:** Capture the current diffs with `jj diff Components/nix/pi.nix Components/nix/pi-dev.nix Components/nix/pi_with_extensions_check.nix` to confirm the authless patches we need to drop.

**Step 2:** Run `jj revert -- Components/nix/pi.nix Components/nix/pi-dev.nix Components/nix/pi_with_extensions_check.nix` to restore the pushed versions.

**Step 3:** Validate the clean state with `jj status` and `jj diff --summary` for the reverted files, confirming no authless logic remains in the working copy.

**Step 4:** (Optional verification) Re-run `jj diff --stat` to ensure only the intended paths changed.

---

### Task 2: Provide an API key for the ouranos-lite-gateway provider

**TDD scenario:** Modifying configuration that is already versioned (generator + runtime file) — update the generator, mirror the runtime file, then validate outputs and JSON shape.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix:216-260`
- Modify: `/home/li/git/Mentci-AI--dev/.pi/agent/models.json`

**Step 1:** Introduce a constant `piAgentGatewayApiKey` near the existing `piAgentGatewayProvider` definition, and augment the provider entry in `piAgentModels.providers[ouranos-lite-gateway]` with `apiKey = piAgentGatewayApiKey;`. Keep the rest of the JSON shape identical.

**Step 2:** Mirror the generated JSON under `.pi/agent/models.json` by adding the same `apiKey` key with the stable string value so the live runtime already satisfies the new requirement.

**Step 3:** Validate the JSON shape with `jq` (or `python -m json.tool`) on `.pi/agent/models.json` and ensure the generator still produces the same structure by evaluating `nix eval` if feasible or simply re-reading `Components/CriomOS/nix/homeModule/min/default.nix` for consistency.

**Step 4:** (Optional) Run `carve`/`cat` view to confirm the provider definition includes the new field and no other siblings were accidentally mutated.

---

### Task 3: Document the fork-instead-of-patch policy

**TDD scenario:** Documentation change — no runtime tests, but include policy verification and cross-reference mention.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Core/ARCHITECTURAL_GUIDELINES.md`
- Modify: `/home/li/git/Mentci-AI--dev/Core/AGENTS.md`
- Modify: `/home/li/git/Mentci-AI--dev/.pi/skills/independent-developer/SKILL.md`

**Step 1:** In `Core/ARCHITECTURAL_GUIDELINES.md`, add a paragraph near the dependency/tooling mandates stating that upstream-like dependencies requiring behavioral changes must be forked and not patched in place.

**Step 2:** In `Core/AGENTS.md`, extend the relevant structural or tooling section (e.g., the `Resolving Version Bugs & Tooling Issues` bullet list) with the same forking mandate, making it explicit that in-place patches on upstream packages violate policy.

**Step 3:** In `.pi/skills/independent-developer/SKILL.md`, add a short rule under the tooling/resolution section reminding independent developers that behavioral changes to upstream dependencies require a forked copy, not ad-hoc patches.

**Step 4:** Double-check the formatting (ordered lists, bullet markers) and confirm all files mention the policy in consistent language.

**Step 5:** Run `rg -n "fork" Core/ARCHITECTURAL_GUIDELINES.md Core/AGENTS.md .pi/skills/independent-developer/SKILL.md` or similar to ensure the new string is present and not duplicated elsewhere.

---

**Plan complete and saved to `docs/plans/2026-03-12-pi-gateway-api-key-and-policy-plan.md`.**

**Execution choice:** Proceed in this session using `/skill:subagent-driven-development` to keep the work self-contained while honoring the implementation plan.