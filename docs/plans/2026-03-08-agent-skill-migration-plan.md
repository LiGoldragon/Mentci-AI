# Agent Skill Reinforcement & Migration Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Reinforce subagent/orchestrator reliability by migrating high-friction operational rules into `.pi/agents/*` and `.pi/skills/*`, with recency-weighted policy resolution and lightweight command templates.

**Architecture:** Keep Core/Library as authority references, but operationalize the most failure-prone behavior directly in agent/skill docs. Add reusable command templates for JJ preflight, verification evidence packets, and ad-hoc script disclosure so common workflows are repeatable and low-noise.

**Tech Stack:** Markdown policy files, JJ workflow, Pi command/prompt templates.

---

### Task 1: Recency-weighted policy resolution (agents + orchestrator)

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/agents/task.md`
- Modify: `.pi/agents/explore.md`
- Modify: `.pi/agents/planner.md`
- Modify: `.pi/agents/reviewer.md`
- Modify: `.pi/skills/independent-developer/SKILL.md`

**Step 1: Add a `Recency-Weighted Policy Resolution` section**
- Define precedence and tie-breaking:
  1. User instruction
  2. System/developer harness rules
  3. Core authority docs (`Core/*`)
  4. Skill/agent role docs
  5. Legacy/older guidance
- Add recency tie-break requirement for same-layer conflicts using bounded JJ evidence.

**Step 2: Add bounded-command guidance**
- Require bounded revsets/path-scoped checks when gathering recency evidence.

**Step 3: Verify section presence**
Run:
- `rg -n 'Recency-Weighted Policy Resolution' .pi/agents .pi/skills/independent-developer/SKILL.md`

**Step 4: Commit**
- Commit with `intent: migrate recency-weighted policy resolution into agents and orchestrator skill`

### Task 2: Harden subagent failure and evidence loop

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/skills/subagent-driven-development/SKILL.md`
- Modify: `.pi/skills/verification-before-completion/SKILL.md`

**Step 1: Add explicit fallback decision tree**
- Include required behavior when task tool returns `Unknown agent ... Available: none`.
- Require minimal JJ preflight before retry.

**Step 2: Add raw evidence packet format**
- Require raw blocks for `jj status`, relevant `jj log`, and verification command output before completion claims.

**Step 3: Verify insertion**
Run:
- `rg -n 'Unknown agent|Available: none|Raw Evidence Packet' .pi/skills/subagent-driven-development/SKILL.md .pi/skills/verification-before-completion/SKILL.md`

**Step 4: Commit**
- Commit with `intent: enforce subagent fallback and raw evidence packet workflow`

### Task 3: Add ad-hoc script disclosure protocol

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/agents/task.md`
- Modify: `.pi/agents/reviewer.md`
- Modify: `.pi/skills/independent-developer/SKILL.md`

**Step 1: Add `Ad-Hoc Script Disclosure` section**
- Require explicit statement when ad-hoc scripts were used.
- Require short rationale and intended long-term replacement path.

**Step 2: Add reviewer checks**
- Reviewer must flag missing disclosure when script-like one-offs are used.

**Step 3: Verify insertion**
Run:
- `rg -n 'Ad-Hoc Script Disclosure' .pi/agents/task.md .pi/agents/reviewer.md .pi/skills/independent-developer/SKILL.md`

**Step 4: Commit**
- Commit with `intent: codify ad-hoc script disclosure and review checks`

### Task 4: Create lightweight command templates for common operations

**TDD scenario:** New feature — full TDD cycle not required (documentation/templating)

**Files:**
- Create: `.pi/commands/jj-preflight.md`
- Create: `.pi/prompts/jj-preflight.md`
- Create: `.pi/commands/verification-packet.md`
- Create: `.pi/prompts/verification-packet.md`
- Create: `.pi/commands/adhoc-disclosure.md`
- Create: `.pi/prompts/adhoc-disclosure.md`

**Step 1: Add JJ preflight template**
- Includes bounded standard commands for state checks.

**Step 2: Add verification packet template**
- Enforces raw outputs for claims.

**Step 3: Add ad-hoc disclosure template**
- Enforces declaration and follow-up artifact requirements.

**Step 4: Verify files exist and are discoverable**
Run:
- `ls -1 .pi/commands .pi/prompts | rg 'jj-preflight|verification-packet|adhoc-disclosure'`

**Step 5: Commit**
- Commit with `intent: add reusable prompt/command templates for jj preflight and verification disclosure`

### Task 5: Introduce orchestrator execution loop skill alignment

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/skills/subagent-driven-development/SKILL.md`
- Modify: `.pi/skills/writing-plans/SKILL.md`

**Step 1: Encode required phase loop**
- Brainstorm → Plan → Implement → Test → Review → Re-implement with Review.

**Step 2: Add stop conditions**
- No advancement with missing evidence or unresolved review findings.

**Step 3: Verify insertion**
Run:
- `rg -n 'Brainstorm|Re-implement|Review Loop|Stop conditions' .pi/skills/subagent-driven-development/SKILL.md .pi/skills/writing-plans/SKILL.md`

**Step 4: Commit**
- Commit with `intent: align orchestrator skills to explicit iterative execution loop`

### Task 6: Final verification + review loop

**TDD scenario:** Trivial change — use judgment

**Files:**
- Verify all modified/created files above

**Step 1: Run repo checks for this scope**
- `jj diff --summary`
- `rg -n 'Recency-Weighted Policy Resolution|Raw Evidence Packet|Ad-Hoc Script Disclosure' .pi/agents .pi/skills .pi/commands .pi/prompts`

**Step 2: Dispatch reviewer subagent**
- Reviewer checks for policy conflicts, ambiguity, and missing anti-regression constraints.

**Step 3: Apply reviewer-required fixes**
- Re-run scope checks.

**Step 4: Finalize session hygiene**
- Push `dev`, verify `dev == dev@origin`, and keep exactly one empty `@` handoff commit.
