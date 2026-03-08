---
description: Build a raw verification packet before completion claims
---

Generate a `## Raw Evidence Packet` with raw command output blocks for:

1. `jj status`
2. `jj log -r 'dev|@|@-' --no-graph`
3. Verification command(s) passed as input (`$@`)

Rules:
- Keep revsets bounded.
- No summary-only output.
- If command output is truncated, include a bounded follow-up command proving pass/fail state.