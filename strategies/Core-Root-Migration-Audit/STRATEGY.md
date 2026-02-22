# Strategy: Core Root Migration Audit

## Status
Completed baseline migration audit and path-cutover review.

## Objective
Track and harden the completed lowercase-to-capitalized root migration for core authority files.

## Completed Outcomes
- Canonical authority root moved from `core/` to `Core/`.
- Program modules moved from `core/programs/` to `Library/`.
- Repository references were rewritten to new canonical paths.

## Remaining Hardening Tasks
1. Fix stale path references in legacy architecture docs that still mention superseded `docs/architecture/*` authority routes.
2. Add explicit guard checks for stale lowercase root references (`core/`, `core/programs/`).
3. Tag historical report quotations that intentionally preserve old paths to avoid accidental rewrites.
4. Keep `scripts/program_version/main.clj` compatible with `Core/` as canonical hash target.

## Acceptance Criteria
- Active policy/docs/scripts reference `Core/` and `Library/` only.
- Guard scripts fail on newly introduced stale lowercase root paths.
- Legacy quoted historical paths are explicitly marked as archival context.
