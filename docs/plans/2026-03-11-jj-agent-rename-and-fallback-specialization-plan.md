# JJ Agent Rename and Fallback Specialization Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Rename the primary JJ/VCS subagent lane from `agentic-jj-expert` to `jj-agent`, and reposition `jj-expert` as a stronger fallback deep-debug / rescue specialist.

**Architecture:** Keep one primary operational lane (`jj-agent`) and one fallback diagnostic lane (`jj-expert`). Update active runtime surfaces, high-authority skills/protocol docs, and active plans so they all teach the new escalation order consistently.

**Tech Stack:** Pi agent markdown surfaces, Mentci core protocol docs, active plan artifacts.

---

### Task 1: Introduce `jj-agent` as the primary agent surface

**TDD scenario:** Trivial documentation/runtime-surface change — use judgment

**Files:**
- Create: `.pi/agents/jj-agent.md`
- Modify: `.pi/agents/agentic-jj-expert.md`

**Step 1:** Copy the current primary JJ lane semantics into `.pi/agents/jj-agent.md`.

**Step 2:** Update metadata and body text so `jj-agent` is clearly the default JJ/VCS execution lane.

**Step 3:** Leave a compatibility stub in `.pi/agents/agentic-jj-expert.md` that redirects to `jj-agent` semantics during migration.

### Task 2: Re-specialize `jj-expert`

**TDD scenario:** Trivial documentation/runtime-surface change — use judgment

**Files:**
- Modify: `.pi/agents/jj-expert.md`

**Step 1:** Reword the agent as fallback/rescue-only, not the default lane.

**Step 2:** Strengthen its mastery language around difficult JJ diagnosis, history repair, and contradiction handling.

**Step 3:** Upgrade the configured model from `openai-codex/gpt-5.1-codex-mini` to `openai-codex/gpt-5.4`.

### Task 3: Update command and policy surfaces

**TDD scenario:** Trivial documentation/runtime-surface change — use judgment

**Files:**
- Modify: `.pi/commands/jj-expert.md`
- Modify: `Core/VersionControlProtocol.md`
- Modify relevant `.pi/skills/*.md`

**Step 1:** Change all active operational guidance from primary `agentic-jj-expert` to primary `jj-agent`.

**Step 2:** Preserve explicit fallback wording pointing to `jj-expert` only when `jj-agent` is unavailable or misbehaving.

**Step 3:** Avoid suffix-collision mistakes by using exact token replacements, not partial substitutions.

### Task 4: Update active plans/examples and verify

**TDD scenario:** Trivial documentation/runtime-surface change — use judgment

**Files:**
- Modify: `docs/plans/2026-03-11-jj-cleanup-training-and-sane-jj-plan.md`
- Modify: `docs/plans/2026-03-11-jj-training-and-history-guardrails.md`
- Modify other active `docs/plans/2026-03-11-*` references as needed

**Step 1:** Update active plans to describe `jj-agent` as primary and `jj-expert` as fallback/debug-only.

**Step 2:** Verify no active instruction surface still teaches `agentic-jj-expert` as primary.

**Step 3:** Dispatch a reviewer subagent to review the migration wording and role split.

**Step 4: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if `jj-agent` is unavailable or misbehaving.
