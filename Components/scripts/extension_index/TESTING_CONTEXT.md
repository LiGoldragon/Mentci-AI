# Testing Context: extension_index

## Purpose
Resolve optional extension index files from standard Core-defined locations.

## Commands
- EDN output:
  - `bb Components/scripts/extension_index/main.clj`
- Plain output:
  - `bb Components/scripts/extension_index/main.clj --plain`

## Contract
- Reads `Core/ExtensionIndexLocations.edn`.
- Missing optional index files are non-fatal.
- Output preserves location order and lists only existing files under `:found`.
