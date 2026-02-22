# Testing Context: Inputs Remount

## 1. Coverage State
- **Unit Tests:** `Components/scripts/inputs_remount/test.clj` covers remount replacement and recursive write-bit removal.
- **Integration Tests:** Manual verification against real `.attrs.json` and `Core/agent-inputs.edn`.

## 2. Verification Commands
```bash
bb Components/scripts/inputs_remount/main.clj --help
bb Components/scripts/inputs_remount/main.clj --attrs-path .attrs.json --whitelist-path Core/agent-inputs.edn
bb Components/scripts/inputs_remount/test.clj
```

## 3. Mocking Strategy
Future tests should stub filesystem operations (`delete`, `symlink`, `chmod`) and config resolution from `.attrs.json`.
