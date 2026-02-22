# Testing Context: session_finalize

## Purpose
Finalize prompt sessions with a compliant `session:` commit message and safe bookmark/push behavior.

## Commands
- Help:
  - `bb Components/scripts/session_finalize/main.clj --help`
- Dry run without push:
  - `bb Components/scripts/session_finalize/main.clj --summary "x" --prompt "p" --context "c" --change "one" --no-push`

## Guarantees
- Refuses malformed session messages (missing required sections).
- Resolves target revision safely (`@` fallback to `@-` when working copy is empty/blank).
- Sets bookmark to finalized revision instead of empty working copy.
- Pushes and verifies remote bookmark hash unless `--no-push` is set.
