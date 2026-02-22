# Survey: Core/Library Cosmic Migration

## Scope
- Current `core/` and `core/programs/` layout.
- `.gitignore` relationship with top-level canonical roots.
- Rename blast radius from lowercase to capitalized root contracts.

## Current State
- `core/` exists; `Core/` does not exist.
- `core/programs/` contains:
  - `OBSOLESCENCE_PROTOCOL.md`
  - `RESTART_CONTEXT.md`
  - `STRATEGY_DEVELOPMENT.md`
  - `STRATEGY_QUEUE.md`
- `core/` contains 12 top-level canonical protocol/guideline files.

## Reference Density (rename impact)
- `core/` references across repository: `116`
- `core/programs/` references: `22`
- `strategies/` references: `78`
- `Reports/` references: `61`

## .gitignore Relationship
Current `.gitignore` includes:
- `/inputs/`
- `Inputs/`
- `inputs_backup/`
- `/workspace/`
- `/target`

Observations:
- There are no `.gitignore` rules for `core/`, `Core/`, `Library/`, or `Reports/`.
- Moving `core/` -> `Core/` and `core/programs/` -> `Library/` is unaffected by ignore rules directly.
- `Inputs/` and lowercase `inputs/` are both ignored; this aligns with substrate non-VCS policy but must remain intentional after root-type migration.

## Constraints
- Case-only renames can be brittle across mixed-case-sensitive tooling.
- Existing docs/scripts hardcode `core/` and `core/programs/` extensively.
- Some legacy text still references lowercase `inputs/`; guard scripts currently detect such drift.

