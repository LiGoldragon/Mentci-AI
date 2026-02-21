# Testing Context: Inputs Remount

## 1. Coverage State
- **Unit Tests:** `scripts/inputs_remount/test.clj` covers remount replacement and recursive write-bit removal.
- **Integration Tests:** Manual verification against real `.attrs.json` and `agent-inputs.edn`.

## 2. Verification Commands
```bash
bb scripts/inputs_remount/main.clj --help
bb scripts/inputs_remount/main.clj --attrs-path .attrs.json --whitelist-path agent-inputs.edn
bb scripts/inputs_remount/test.clj
```

## 3. Mocking Strategy
Future tests should stub filesystem operations (`delete`, `symlink`, `chmod`) and config resolution from `.attrs.json`.
