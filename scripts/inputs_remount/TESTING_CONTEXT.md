# Testing Context: Inputs Remount

## 1. Coverage State
- **Unit Tests:** None.
- **Integration Tests:** Manual verification only.

## 2. Verification Commands
```bash
bb scripts/inputs_remount/main.clj --help
bb scripts/inputs_remount/main.clj --attrs-path .attrs.json --whitelist-path agent-inputs.edn
```

## 3. Mocking Strategy
Future tests should stub filesystem operations (`delete`, `symlink`, `chmod`) and config resolution from `.attrs.json`.
