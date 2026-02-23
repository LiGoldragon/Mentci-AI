# Roadmap: Repo Intent Realignment

## Phase 1: Baseline (Inventory)
1. Generate mismatches against Core authority (naming, paths, tool entrypoints).
2. Tag artifacts as `retain/integrate/archive/remove`.
3. Publish a tracked inventory report.

## Phase 2: Hard Conflicts
1. Resolve FS contract drift (`Sources` canonical, remove stale alias usage in active paths).
2. Resolve jail contract drift (single canonical jail launch path).
3. Resolve script/runtime duplication for overlapping tools.

## Phase 3: Artifact Prune
1. Remove dead scripts and compatibility wrappers.
2. Archive superseded strategy fragments into report artifacts.
3. Keep only operational docs in active strategy files.

## Phase 4: Verification + Lock
1. Run full guard sweep and focused runtime checks.
2. Update queue/index files and restart context snapshot.
3. Freeze stabilized contracts in Core and remove temporary compatibility notes.

## Phase 5: Section Sweep Program
1. Execute `Development/high/Repo-Intent-Realignment/SECTION_SWEEP_PROGRAM.md`.
2. Sweep repository sections in deterministic order with per-section research checkpoints.
3. Complete global finalization criteria and close remaining drift classes.
