---
description: Build a raw verification packet before completion claims
---

Set `TARGET_BOOKMARK=${MENTCI_TARGET_BOOKMARK:-dev}` so every verification packet is bound to the runtime target bookmark. Produce a `## Raw Evidence Packet` with raw outputs for:

Treat `origin` as the authoritative completion truth: a local commit does not count as real/completed until the target bookmark has been pushed and verified on `origin`. We only use Git through JJ; Git may be backend transport, but JJ is the workflow authority.

1. `jj status`
2. `jj log -r "${TARGET_BOOKMARK}|${TARGET_BOOKMARK}@origin|@|@-" --no-graph`
3. `jj log -r 'unresolved()' --no-graph`
4. The relevant verification command(s) for this task (tests/build/checks)

If `jj log -r 'unresolved()'` reports unresolved bookmarks, include their raw output block and explain how you addressed them before completion. If any output is truncated, add a bounded follow-up command (e.g., add `--limit 20`) to prove pass or fail. No completion claim is allowed without this packet.