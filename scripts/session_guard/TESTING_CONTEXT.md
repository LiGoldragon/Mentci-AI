# Testing Context: session_guard

## Coverage State
- Script-level logic validated by running the command directly.

## Verification Commands
- `bb scripts/session_guard/main.clj`

## Mocking Strategy
- No external service mocks.
- Script reads local `jj` history and exits non-zero when trailing `intent:` commits are not terminated by a `session:` commit.
