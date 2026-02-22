# Strategy: core/ to Core/ Migration with Overlap Cleanup

## Objective
Move all core authority files from `core/` to `Core/` while preventing path drift and resolving overlapping or obsolete references discovered in the survey.

## Scope
- Migrate all files currently under `core/` into `Core/`.
- Resolve authoritative path overlap (especially version control and restart context references).
- Introduce migration guards and acceptance checks.

## Phase Plan

### Phase 0: Freeze and Inventory
1. Freeze structural moves while auditing references.
2. Export deterministic path inventory for `core/` and `core/programs/` references.
3. Classify references by type:
- canonical mandates
- historical report text
- superseded legacy docs

### Phase 1: Authority Overlap Cleanup (before rename)
1. Rewrite known stale references:
- `docs/architecture/VERSION_CONTROL.md` -> canonical current source (`core/VERSION_CONTROL.md`, later `Core/VERSION_CONTROL.md`).
- `docs/guides/RESTART_CONTEXT.md` -> `core/programs/RESTART_CONTEXT.md` (later `Library/RESTART_CONTEXT.md` if adopted).
2. Mark legacy docs as historical-only where needed to prevent accidental authority reintroduction.

### Phase 2: Compatibility Alias Contract
1. Add explicit alias table in FS/architecture docs:
- `core/*` aliases to `Core/*` during transition.
- If enabled in same cycle: `core/programs/*` aliases to `Library/*`.
2. Add guard mode that warns (then fails) on new lowercase `core/` references in active docs/scripts.

### Phase 3: Mechanical Rename
1. Move `core/` directory to `Core/`.
2. Rewrite repository references in one bounded changeset:
- `README.md`, `AGENTS.md`, `core/programs/*` equivalents, scripts, strategies, reports, workflows.
3. Keep explicit exceptions for historical report quotations where path preservation is intentional.

### Phase 4: Enforcement and Obsolescence Sweep
1. Promote warning guard to blocking guard for stale `core/` paths.
2. Run artifact sweep for superseded docs still carrying old authority path claims.
3. Update restart/program map and release milestones with migration completion stamp.

## Candidate Obsolescence Actions
- Reclassify `docs/architecture/JjAutomation.md` as historical note only, or update it to canonical path references.
- Remove/retire any document that still presents `docs/architecture/VERSION_CONTROL.md` as an active authority source.

## Risks
- High reference count means partial rewrite can silently break policy gates.
- Case-sensitive path renames may be inconsistent across tools/environments.
- Historical-report path normalization can accidentally alter archival meaning.

## Acceptance Criteria
- No active authoritative document points to `docs/architecture/VERSION_CONTROL.md`.
- No active authoritative document points to `docs/guides/RESTART_CONTEXT.md`.
- Core mandate references resolve to `Core/*` after migration.
- Guard scripts detect and block new stale `core/` references post-cutover.
