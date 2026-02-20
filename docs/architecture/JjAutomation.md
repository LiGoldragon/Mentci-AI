# Jujutsu Automation Workflow

This document defines the operational JJ workflow for agents, with explicit commands and failure handling. It is the primary reference for automating commits and pushes.

## 1. Preconditions
1. Enter the dev shell so `MENTCI_*` variables are set and the jail workspace is active.
2. Use `mentci-jj` for status/log/commit to ensure consistent workspace targeting.

If the environment variables are missing, use `jj` directly from the repository root and do not attempt jail shipping.

## 2. Atomic Change Loop (Commit-Every-Intent)
1. Make exactly one atomic change.
2. Verify status: `mentci-jj status`
3. Commit: `mentci-jj commit "intent: <short message>"`
4. Push immediately: `jj git push --bookmark dev`

If `jj git push` fails due to missing remote configuration, stop and ask for instructions.

## 3. Jailed Shipping (Workspace -> dev)
When operating in the jailed workspace, always use `mentci-commit` to advance the target bookmark:

```
mentci-commit "intent: <short message>"
```

This uses `MENTCI_WORKSPACE`, `MENTCI_REPO_ROOT`, and `MENTCI_COMMIT_TARGET` to move the bookmark safely.

## 4. Handling Unexpected Changes
If the working copy shows changes you did not make:
1. Stop and ask the user how to proceed.
2. Do not include unrelated changes in a commit without explicit approval.

## 5. Dirty Tree Intent Separation
When the working copy is dirty and multiple change-intents may be present:
1. Enumerate change groups by file and purpose (proposed intent list).
2. Get approval for the grouping before proceeding.
3. For each intent group:
   - If the group is not already isolated, split the change by reverting or moving files so only that group remains.
   - Commit the isolated group: `mentci-jj commit "intent: <message>"`
   - Push immediately: `jj git push --bookmark dev`
4. Repeat until all change-intents are committed or explicitly deferred.

If splitting cannot be done safely, stop and request direction.

## 6. History Inspection
Use `mentci-jj log` to prefer `--no-signing` and avoid GPG failures.

## 7. Related References
- Conceptual model: `docs/architecture/JAIL_COMMIT_PROTOCOL.md`
- Tooling overview: `docs/architecture/TOOLS.md`
