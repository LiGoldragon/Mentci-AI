---
description: Build a raw verification packet before completion claims
---

Set `TARGET_BOOKMARK=${MENTCI_TARGET_BOOKMARK:-dev}` so the packet tracks the runtime target bookmark. Generate a `## Raw Evidence Packet` with raw command output blocks for:

Treat `origin` as the authoritative completion truth: a local commit does not count as real/completed until the target bookmark has been pushed and verified on `origin`. Direct Git workflow usage is heresy; Git is backend transport only.

1. `jj status`
2. `jj log -r "${TARGET_BOOKMARK}|${TARGET_BOOKMARK}@origin|@|@-" --no-graph`
3. `jj log -r 'unresolved()' --no-graph`
4. Verification command(s) passed as input (`$@`)

Rules:
- Keep revsets bounded.
- If `jj log -r 'unresolved()'` reports any bookmarks, capture that raw block and document how you resolved or will resolve them before claiming completion.
- If command output is truncated, include a bounded follow-up command proving pass/fail state.
- No summary-only output.