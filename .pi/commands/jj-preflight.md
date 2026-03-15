---
description: Run bounded JJ preflight checks and return raw state evidence
---

Run and return raw outputs for the following commands:

Treat `origin` as the authoritative completion truth: a local commit does not count as real/completed until the target bookmark has been pushed and verified on `origin`. We only use Git through JJ; Git may be backend transport, but JJ is the workflow authority.

1. `jj status`
2. If `MENTCI_TARGET_BOOKMARK` is set: `jj log -r "$MENTCI_TARGET_BOOKMARK|$MENTCI_TARGET_BOOKMARK@origin|@|@-" --no-graph -n 20`.
   If it is unset: `jj log -r '@|@-' --no-graph -n 10` and note that the runtime bookmark is unresolved before taking further action.
3. `jj diff --summary`

Do not summarize. Output raw command blocks only.
