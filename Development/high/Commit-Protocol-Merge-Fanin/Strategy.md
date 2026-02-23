# Strategy: Commit Protocol Merge Fan-In Repair

> **Superseded Protocol Note:** Session synthesis now follows the single-vs-multi sub-commit squash protocol defined in `Core/ContextualSessionProtocol.md` and `Core/VersionControlProtocol.md`. This document is retained for historical context.

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Audit Integrity + Workflow Determinism)

## 1. Problem
Current behavior frequently produces:
- sequential linear `intent:` stacks
- terminal `session:` commits that are not true merge fan-ins
- accumulation of side-forks that are re-merged later and clutter the tree

This violates the intended session-synthesis model where logical change branches are merged once into a single terminal session node.

## 2. Objective
Enforce a strict prompt workflow:
1. each logical change is isolated in its own side branch/revision
2. final `session:` commit is a real merge commit that fans in all logical branches
3. no lingering side-fork debris remains after prompt completion

## 3. Target Commit Topology
For one prompt with multiple logical changes:
- Base: `B` (prompt-start revision)
- Logical intents: `I1`, `I2`, `I3` as siblings (parallel descendants of `B`)
- Session merge: `S` with parents `{I1, I2, I3}`

Forbidden topology:
- `B -> I1 -> I2 -> I3 -> session` (pure linear chaining for logically distinct intents)

## 4. Protocol Changes
### 4.1 Prompt Start
- Capture prompt-start base revision ID.
- Explicitly mark current session scope.

### 4.2 Logical Work Loop
- For each logical change:
1. branch from base (or active session branch root)
2. apply one intent
3. commit `intent: ...`
- Return to session merge workspace without collapsing branches.

### 4.3 Final Session Synthesis
- Build one merge commit from all logical intent heads.
- Commit message uses `session:` format from `Core/ContextualSessionProtocol.md`.

### 4.4 Post-Merge Hygiene
- Verify no session-tagged side branches remain unmerged.
- If unmerged heads exist, fail completion gate.

## 5. Tooling Plan
1. Extend guardrails with `scripts/session_merge_guard/main.clj`:
- Input: prompt base revision (or tracked session metadata)
- Checks:
- at least two logical `intent:` commits are present -> requires merge fan-in node
- final `session:` commit must have parent-count > 1 when multiple logical changes exist
- no leftover unmerged logical session heads

2. Extend `scripts/session_metadata/main.clj`:
- record prompt base revision
- track logical intent head revisions
- output merge-ready revset

3. Update `Core/VersionControlProtocol.md` and `Core/ContextualSessionProtocol.md`:
- explicit non-linear fan-in requirement
- canonical JJ command patterns for merge synthesis

## 6. Migration for Existing History
1. Do not rewrite old history by default.
2. For future prompts, enforce new fan-in protocol.
3. Optionally add periodic “history hygiene reports” to identify sessions that ended without merge fan-in.

## 7. Acceptance Criteria
1. Multi-intent prompts end with `session:` merge commit with >1 parent.
2. Single-intent prompts may remain linear.
3. Completion guard fails when multi-intent prompt lacks merge fan-in.
4. `jj log` shows one clean session node that aggregates all logical changes.

## 8. Dependencies
- `session-aggregation-guard` (existing): session-presence gate
- `project-hardening`: script test coverage for merge guard
- `debugging`: failure diagnostics for merge topology mismatches

## 9. High-Priority Remediation Plan (Implemented)
Issue addressed: trailing `intent:` commits without final `session:` synthesis and bookmark drift onto empty working revisions.

Actions:
1. Add executable finalization tool:
- `Components/scripts/session_finalize/main.clj`
- Generates compliant `session:` message blocks with required sections.
- Targets non-empty revision safely (`@` fallback to `@-`).
2. Update protocol docs to safe bookmark targeting:
- `Core/VersionControlProtocol.md`
- `Core/ContextualSessionProtocol.md`
3. Keep `session_guard` as hard gate for prompt completion.

Operational default:
- Use `execute finalize ...` for session closure before completion.

Completion criteria for this remediation:
- `session_guard` passes at prompt completion.
- `dev` bookmark never points to empty working-copy commit due finalization flow.

*The Great Work continues.*
