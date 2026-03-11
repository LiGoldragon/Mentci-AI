# JJ Training and History Guardrails Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Diagnose why duplicate JJ change IDs, empty commits, and dangling-looking non-trunk commits keep appearing, then harden repository skills/agents/docs so Mentci agents operate with a much more accurate JJ mental model.

**Architecture:** Separate the work into four layers: (1) durable research artifact capturing real JJ mechanics plus repo-specific root causes, (2) core/skill policy updates that define the canonical workflow and anti-churn rules, (3) subagent prompt/command updates that teach the same model operationally, and (4) plan/example cleanup so older docs stop reinforcing hard-coded bookmark and empty-commit mistakes.

**Tech Stack:** Jujutsu workflow docs, Markdown prompt/skill files, bounded JJ inspection, web research evidence.

---

### Task 1: Persist JJ mechanics + repo root-cause research

**TDD scenario:** Trivial change — use judgment

**Files:**
- Create: `Research/medium/Subagent-Workflow/<solar>_report_jj_duplicate-change-ids_empty-commits_and_agent-training_gaps.md`
- Modify: `Research/medium/Subagent-Workflow/index.edn` (if present)

**Step 1: Draft the report**
- Explain JJ change ID vs commit ID.
- Document how divergent/duplicate visible change IDs arise legitimately.
- Document how empty working-copy commits arise normally in JJ.
- Add repo-specific diagnosis for Mentci: repeated `jj describe`/bookmark moves on clean trees, empty finalize commits, floating side bookmarks.

**Step 2: Verify evidence coverage**
Run bounded checks that confirm:
- external JJ sources are cited,
- local examples are tied to bounded JJ evidence already gathered,
- the new report path is indexed correctly.

**Step 3: Finalize via `jj-agent`**
Ask `jj-agent` to:
- establish bounded JJ state,
- finalize the current intent into a non-empty described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

### Task 2: Strengthen canonical JJ policy in Core + primary skills

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `Core/VersionControlProtocol.md`
- Modify: `.pi/skills/independent-developer/SKILL.md`
- Modify: `.pi/skills/subagent-driven-development/SKILL.md`
- Modify: `.pi/skills/finishing-a-development-branch/SKILL.md`

**Step 1: Add the missing JJ mental model**
- Teach explicitly that change IDs are stable change identities while commit IDs rewrite.
- Explain that duplicate visible change IDs usually mean divergence/history exposure, not repository corruption.
- Explain normal empty working-copy commits vs bad described empty commits.

**Step 2: Add anti-churn guardrails**
- Require `jj status` + `jj diff --summary` before `jj describe`/finalization.
- Forbid describing/committing a clean tree except when intentionally leaving anonymous `@` empty.
- Forbid moving bookmarks to empty commits.
- Clarify when to abandon/merge/rebase dangling side bookmarks.
- Reinforce `execute session-guard` / `execute root-guard` and research-artifact requirements.

**Step 3: Verify wording consistency**
Run bounded text checks ensuring the same key rules appear across the canonical docs without contradiction.

**Step 4: Finalize via `jj-agent`**
Ask `jj-agent` to finalize/push with bounded proof. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

### Task 3: Retrain JJ-facing agents and commands

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/agents/jj-expert.md`
- Modify: `.pi/agents/task.md`
- Modify: `.pi/agents/planner.md`
- Modify: `.pi/agents/reviewer.md`
- Modify: `.pi/agents/explore.md`
- Modify: `.pi/commands/jj-expert.md`
- Modify: `.pi/commands/jj-preflight.md`

**Step 1: Harden `jj-expert` most aggressively**
- Add deeper preflight requirements.
- Add explicit duplicate-change-ID interpretation guidance.
- Add empty/dangling commit diagnosis heuristics.
- Add rules for bookmark movement, `jj new`, `jj describe`, hidden rewrites, and content-preserving repairs.

**Step 2: Propagate a lighter JJ model to all other JJ-capable agents**
- Teach them what empty `@` means, when not to commit, and when to escalate to `jj-expert`.
- Ensure they stop treating duplicate visible change IDs as corruption.
- Ensure preflight uses the runtime bookmark, not hard-coded `dev`.

**Step 3: Fix helper command prompts**
- Remove hard-coded `dev` from `jj-preflight.md`.
- Ensure command prompts require raw bounded evidence and empty-commit checks.

**Step 4: Review**
Use a reviewer subagent to assess the JJ training edits for correctness and consistency.

**Step 5: Finalize via `jj-agent`**
Ask `jj-agent` to finalize/push with bounded proof. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

### Task 4: Correct old plan/examples that currently teach bad JJ habits

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `docs/plans/2026-03-05-rust-components-repo-split.md`
- Modify: `docs/plans/2026-03-07-jcodemunch-pi-extension-support-design.md`

**Step 1: Replace misleading hard-coded examples**
- Convert fixed `dev`/`main` push examples into runtime-bookmark-aware guidance.
- Clarify when `main` is valid only for explicit release flows.
- Replace `jj new main` style guidance with correct post-push handoff wording.

**Step 2: Add a note on empty working-copy semantics**
- Explain that `jj new` prepares the next prompt and should remain anonymous/empty.
- Explain that described empty commits are usually churn and should be avoided unless intentionally part of a repair path.

**Step 3: Verify no stale bad examples remain in touched plan files**
Run bounded text checks against these files.

**Step 4: Finalize via `jj-agent`**
Ask `jj-agent` to finalize/push with bounded proof. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

### Task 5: Final verification and report

**TDD scenario:** Verification phase

**Files:**
- Modify as needed from prior tasks only

**Step 1: Run bounded verification**
- Confirm working tree clean or intentionally prepared.
- Confirm updated files contain the new JJ rules.
- Run one final reviewer pass over the combined JJ-training surface.

**Step 2: Persist final report context**
- Summarize root causes, guards added, files updated, and any remaining unresolved tree cleanup recommendations.

**Step 3: Finalize via `jj-agent`**
Ask `jj-agent` to:
- finalize the final non-empty revision or session summary,
- push the runtime target bookmark,
- verify `<bookmark> == <bookmark>@origin`,
- leave exactly one empty working copy above the final revision.
Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.
