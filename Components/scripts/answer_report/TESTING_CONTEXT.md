# Testing Context: answer_report

## Coverage State
- Script-level CLI execution validated manually.

## Verification Commands
- `bb Components/scripts/answer_report/main.clj --prompt "Q" --answer "A" --kind answer --change-scope modified-files --title smoke-test`
- `bb Components/scripts/answer_report/main.clj --prompt "Draft prompt" --answer "Draft answer" --kind draft --change-scope no-files --title draft-smoke-test`
- `bb Components/scripts/answer_report/main.clj --prompt "Question prompt" --answer "Question answer" --kind question --change-scope no-files --title question-smoke-test`
- `bb Components/scripts/answer_report/main.clj --prompt "Q" --answer "A" --title smoke-test --chronos-raw "12.1.2.3 | 5919 AM"`

## Mocking Strategy
- No network mocks required.
- Chronography timestamp is resolved in this order:
  1. `chronos` binary on `PATH`
  2. `cargo run --manifest-path Components/Cargo.toml --bin chronos`
- Deterministic tests can bypass runtime invocation with `--chronos-raw`.
