# Development Program: Section-by-Section Repository Cleanup

## Objective
Execute a deterministic full-repository sweep to remove dead links, abandoned ideas, and obsolete artifacts while preserving valid historical research.

## Sweep Order (Section by Section)
1. `Core/`
2. `Library/`
3. `Components/`
4. `Development/high/`
5. `Development/medium/`
6. `Development/low/`
7. `Research/high/`
8. `Research/medium/`
9. `Research/low/`
10. Root files (`AGENTS.md`, `README.md`, `flake.nix`, `.gitignore`, etc.)

## Per-Section Procedure
1. Inventory links and references.
- Run:
  - `rg -n "Reports/|Strategies/|README\.md|TODO|FIXME|WIP|deprecated|obsolete|legacy|abandon|superseded" <section>`

2. Classify findings.
- `retain`: still authoritative/operational.
- `integrate`: valid but misplaced.
- `archive`: keep only as historical research context.
- `remove`: dead, conflicting, or stale.

3. Apply cleanup.
- Update dead paths to canonical paths.
- Remove stale references to superseded structures.
- Move historical-but-obsolete implementation notes into `Research/` artifacts.

4. Validate section.
- For script changes: `bb Components/scripts/validate_scripts/main.clj`
- For cross-tree consistency: `bb Components/scripts/subject_unifier/main.clj`
- For policy references: `bb Components/scripts/reference_guard/main.clj`

5. Emit section checkpoint research artifact.
- One artifact per section sweep under `Research/high/Repo-Intent-Realignment/`.

## Done Criteria (Per Section)
1. No dead internal path links in the section.
2. No references to removed/superseded structures unless explicitly marked historical.
3. Obsolete implementation guidance removed from active development docs.
4. Validation checks pass for the affected surface.

## Global Finalization Criteria
1. Zero `Reports/` or `Strategies/` references outside historical quoted context.
2. Topic documentation resolves via `Research/<priority>/<Subject>/index.edn`.
3. `Development/` contains only active execution guidance.
4. `Research/` holds historical and prompt-answer traceability.
5. Guard scripts pass except known pre-existing root drift (`outputs`, `result`) unless separately remediated.

## First Execution Batch
1. `Development/high/`
2. `Research/high/`
3. `Core/`

Rationale: high-priority tracks and authority files produce the largest cleanup leverage first.
