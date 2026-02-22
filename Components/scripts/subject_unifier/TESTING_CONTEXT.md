# Testing Context: subject_unifier

## Purpose
Verify bidirectional subject coverage between `Research/` entries and `Development/` directories.

## Commands
- `bb Components/scripts/subject_unifier/main.clj`
- `bb Components/scripts/subject_unifier/main.clj --write`

## Expected Behavior
- Dry run prints inventory and missing counterpart counts.
- `--write` migrates legacy flat research artifacts into `Research/high/<Subject>/`, creates/updates topic `index.edn` files, and creates missing `Development/high/<Subject>/` scaffolds.
