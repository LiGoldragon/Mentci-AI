# Testing Context: Sources Remount

## 1. Coverage State
- **Unit Tests:** `Components/scripts/sources_remount/test.clj` covers remount replacement and recursive write-bit removal.
- **Integration Tests:** Manual verification against real `.attrs.json` and `Core/agent-sources.edn`.

## 2. Verification Commands
```bash
bb Components/scripts/sources_remount/main.clj --help
bb Components/scripts/sources_remount/main.clj --attrs-path .attrs.json --whitelist-path Core/agent-sources.edn
bb Components/scripts/sources_remount/test.clj
```

## 3. Mocking Strategy
Future tests should stub filesystem operations (`delete`, `symlink`, `chmod`) and config resolution from `.attrs.json`.
