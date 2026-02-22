# Testing Context: answer_report

## Coverage State
- Script-level CLI execution validated manually.

## Verification Commands
- `bb Components/scripts/answer_report/main.clj --prompt "Q" --answer "A" --kind answer --change-scope modified-files --title smoke-test`
- `bb Components/scripts/answer_report/main.clj --prompt "Draft prompt" --answer "Draft answer" --kind draft --change-scope no-files --title draft-smoke-test`
- `bb Components/scripts/answer_report/main.clj --prompt "Question prompt" --answer "Question answer" --kind question --change-scope no-files --title question-smoke-test`

## Mocking Strategy
- No network mocks required.
- Chronography timestamp is sourced via local `cargo run --bin chronos`.
