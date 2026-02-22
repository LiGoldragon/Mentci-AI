# Testing Context: root_guard

## Purpose
Verify that repository root entries conform to the top-level FS contract.

## Quick Checks
- `bb Components/scripts/root_guard/main.clj`

## Expected Result
- Exit 0 when only allowed top-level directories/files are present.
- Non-zero with explicit violations when root drift is introduced.
