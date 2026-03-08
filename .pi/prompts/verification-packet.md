---
description: Build a raw verification packet before completion claims
---

Produce a `## Raw Evidence Packet` with raw outputs for:

1. `jj status`
2. `jj log -r 'dev|@|@-' --no-graph`
3. The relevant verification command(s) for this task (tests/build/checks)

If output is truncated, add a bounded follow-up command that proves pass/fail state.
No completion claim without this packet.
