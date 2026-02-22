# Testing Context: reference_guard

## Coverage State
- Script-level validation executed via direct CLI invocation.

## Verification Commands
- `bb scripts/reference_guard/main.clj`
- `bb scripts/validate_scripts/main.clj`

## Mocking Strategy
- No external service mocks required.
- The script performs read-only repository scans and returns non-zero on forbidden references.
