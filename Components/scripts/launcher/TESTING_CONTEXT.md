# Testing Context: Jail Launcher

## 1. Coverage State
- **Unit Tests:** None.
- **Integration Tests:** Partial. Verified through manual `nix develop` entry.

## 2. Verification Commands
```bash
# Manual check of .attrs.json ingestion
bb Components/scripts/launcher/main.clj
```

## 3. Mocking Strategy
Future tests should mock `clojure.java.shell/sh` to avoid running real `rsync` or `ln` commands during unit verification.

*The Great Work continues.*
