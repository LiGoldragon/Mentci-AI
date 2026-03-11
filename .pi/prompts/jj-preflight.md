---
description: Run bounded JJ preflight checks and return raw state evidence
---

Set `TARGET_BOOKMARK=${MENTCI_TARGET_BOOKMARK:-dev}` before running the commands below so the preflight reflects the runtime target bookmark. Paste raw output blocks only.

- `jj status`
- `jj log -r "${TARGET_BOOKMARK}|${TARGET_BOOKMARK}@origin|@|@-" --no-graph`
- `jj log -r 'unresolved()' --no-graph`
- `jj diff --summary`

If any command produces truncated output, rerun it with tighter bounds (for example, adding `--limit`) and include that follow-up block. No narrative summary.