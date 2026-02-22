# Testing Context: subject_unifier

## Purpose
Verify bidirectional subject coverage between `Reports/` entries and `strategies/` directories.

## Commands
- `bb scripts/subject_unifier/main.clj`
- `bb scripts/subject_unifier/main.clj --write`

## Expected Behavior
- Dry run prints inventory and missing counterpart counts.
- `--write` migrates legacy flat reports into `Reports/<Subject>/`, creates/updates topic `README.md` files, and creates missing `strategies/<Subject>/` scaffolds.
