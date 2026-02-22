# Testing Context: inputs_mounter

## Purpose
Mount flake-derived input sources into `Inputs/` from admin mode (outside jail).

## Quick Checks
- `bb Components/scripts/inputs_mounter/main.clj`
- `bb Components/scripts/inputs_mounter/main.clj --write`
- `bb Components/scripts/inputs_mounter/main.clj --mode ro --replace --write`

## Expected Result
- Dry run prints planned mount actions.
- `--write` creates `Inputs/<name>` entries for whitelisted inputs.
- `--mode ro` creates symlinks to source paths (read-only semantics from source store paths).
