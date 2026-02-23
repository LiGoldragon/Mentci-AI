# Strategy: Agent Authority and Path Alignment

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Process Hardening)

## 1. Objective
Establish `Core/AGENTS.md` as the single canonical agent authority file and remove repository-level reference drift that conflicts with this contract.

## 2. Scope
- Canonical authority references (`Core/AGENTS.md`).
- Source-of-truth references (`Core/HIGH_LEVEL_GOALS.md`, `Library/RestartContext.md`).
- Filesystem path normalization to `Sources/` (migrated casing).
- Strategy/queue/task cross-reference consistency.
- Non-destructive regression guardrails for reference drift.

## 3. Implementation Plan
1. **Authority Lock**
- Replace all references to `docs/architecture/AGENTS.md` with `Core/AGENTS.md`.
- Ensure root `AGENTS.md` points to `Core/AGENTS.md`.

2. **Reference Audit**
- Scan `Core/`, `Components/tasks/`, `Development/`, `Library/`, and root docs for:
- stale AGENTS path
- stale goals path (`docs/architecture/HIGH_LEVEL_GOALS.md`)
- stale restart-context path variants
- legacy `Inputs/` references that should now be `Sources/`
- Create an edit matrix with file, line, old path, new path.

3. **Path and SSOT Repairs**
- Update references to `Core/HIGH_LEVEL_GOALS.md`.
- Update references to `Library/RestartContext.md`.
- Normalize internal repository path references to `Sources/`.

4. **Strategy-System Alignment**
- Reconcile queue status with strategy file status and actual implementation state.
- Ensure strategy docs reference current script paths (for example `Components/scripts/<name>/main.clj`).
- Add missing active strategies to the queue where applicable.

5. **Goal Execution Integrity**
- Ensure task files point to the active canonical goals file.
- Ensure runbooks and checklists use current path reality (`Sources/`).

6. **Regression Guardrail**
- Add a checker script that flags:
- `docs/architecture/AGENTS.md` references
- `docs/architecture/HIGH_LEVEL_GOALS.md` references
- unintended lowercase `inputs/` references to repository paths
- Integrate checker into normal validation flow.

## 4. Strategy Doubts and Failure Modes
1. **Blind replacement risk**
- Problem: mechanical `inputs/` -> `Sources/` can modify historical text or intentional legacy examples.
- Mitigation: use allowlisted, context-aware replacements and manual review for ambiguous matches.

2. **Runtime assumption mismatch**
- Problem: some scripts may still resolve lowercase paths.
- Mitigation: audit executable references separately from documentation references; test critical scripts.

3. **Provenance distortion**
- Problem: rewriting old run records can blur historical accuracy.
- Mitigation: preserve historical wording when required and append correction notes instead of rewriting provenance sections.

4. **Scope creep**
- Problem: broad cleanup can turn into architectural refactoring.
- Mitigation: constrain this strategy to reference/path consistency and queue/task alignment only.

5. **Lint false positives**
- Problem: checker can fail on code blocks or intentional legacy mentions.
- Mitigation: support explicit ignore markers and fenced-block-aware scanning.

6. **Atomicity and commit-discipline conflict**
- Problem: multi-area cleanup encourages bundling.
- Mitigation: split work into atomic intent commits: authority refs, paths, queue/status, guardrail.

## 5. Acceptance Criteria
- No repository document claims canonical agent authority outside `Core/AGENTS.md`.
- Internal references to goals and restart context resolve to current canonical paths.
- Repository path references use `Sources/` where referring to the migrated input substrate.
- Strategy queue and strategy docs do not contradict each other on status/path facts.
- A guardrail check exists and catches legacy reference regressions.

*The Great Work continues.*
