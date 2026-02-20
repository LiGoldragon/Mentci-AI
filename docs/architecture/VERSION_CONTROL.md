# Version Control Protocol (JJ)

This document is the source of truth for Jujutsu workflows, commit discipline, and push cadence.

## 1. Core Rules
1. All active development targets the `dev` bookmark.
2. Commit every intent: one atomic modification per commit, no bundling.
3. Push once after all intended commits are ready, unless explicitly instructed otherwise.
4. Per-prompt dirty-tree auto-commit: if the working copy is dirty at the start of a prompt, create a commit before making any new changes. After completing the prompt, create at least one new commit for the prompt's work.

## 2. Preconditions
1. Prefer working in the dev shell so `MENTCI_*` variables and the jail workspace are active.
2. Use `mentci-jj` for status/log/commit to ensure consistent workspace targeting.

If `MENTCI_*` variables are missing, use `jj` directly from the repository root and do not attempt jail shipping.

## 3. Atomic Change Loop
1. Make exactly one atomic change.
2. Verify status: `mentci-jj status`
3. Commit: `mentci-jj commit "intent: <short message>"`
4. Repeat until all intended changes are committed.
5. Advance and push once:
   - `jj bookmark set dev -r @`
   - `jj git push --bookmark dev`

## 4. Dirty Tree Intent Separation
When the working copy is dirty and multiple change-intents may be present:
1. Enumerate change groups by file and purpose (proposed intent list).
2. Get approval for the grouping before proceeding.
3. For each intent group:
   - Isolate the group so only that change remains.
   - Commit the isolated group: `mentci-jj commit "intent: <message>"`
4. After the final intent commit is ready, advance and push once:
   - `jj bookmark set dev -r @`
   - `jj git push --bookmark dev`

If splitting cannot be done safely, stop and request direction.

Empty working changes:
- Do not abandon or close empty JJ working changes by default; they may belong to another agent or worktree.

## 5. Jailed Shipping (Workspace -> dev)
When operating in the jailed workspace, always use `mentci-commit` to advance the target bookmark:

```
mentci-commit "intent: <short message>"
```

This uses `MENTCI_WORKSPACE`, `MENTCI_REPO_ROOT`, and `MENTCI_COMMIT_TARGET` to move the bookmark safely.

## 6. History Inspection
Use `mentci-jj log` to prefer `--no-signing` and avoid GPG failures.

## 7. Related References
- Conceptual model: `docs/architecture/JAIL_COMMIT_PROTOCOL.md`
- Tooling overview: `docs/architecture/TOOLS.md`
