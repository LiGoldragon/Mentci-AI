# Testing Context: subject_unifier

## Purpose
Verify bidirectional subject coverage between `Reports/` entries and `strategies/` directories.

## Commands
- `bb scripts/subject_unifier/main.clj`
- `bb scripts/subject_unifier/main.clj --write`

## Expected Behavior
- Dry run prints inventory and missing counterpart counts.
- `--write` creates missing `Reports/topics/<subject>.md` and `strategies/<subject>/` scaffolds.
