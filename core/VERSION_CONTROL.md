# Version Control Protocol (JJ)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

This document is the source of truth for Jujutsu workflows, commit discipline, and push cadence.

## 1. Core Rules
1. All active development targets the `dev` bookmark.
2. Commit every intent: one atomic modification per commit, no bundling.
3. **Automatic Push:** Every atomic commit to `dev` **must** be immediately followed by a `jj git push --bookmark dev` to ensure synchronization across agents and environments.
4. Push once to `main` only after all intended session commits are ready and verified.
5. Aggressive auto-commit: any filesystem change must be committed immediately. Do not wait for explicit user prompts like "commit everything."
5. Per-prompt dirty-tree auto-commit: if the working copy is dirty at the start of a prompt, create a commit before making any new changes. After completing the prompt, create at least one new commit for the prompt's work.

## 2. Preconditions
1. Prefer working in the dev shell so `MENTCI_*` variables and the jail workspace are active.
2. Use `mentci-jj` for status/log/commit to ensure consistent workspace targeting.

If `MENTCI_*` variables are missing, use `jj` directly from the repository root and do not attempt jail shipping.

## 3. Atomic Change Loop
1. Make exactly one atomic change.
2. Verify status: `mentci-jj status`
3. Commit using the **Contextualized Session Protocol** if an active session is in progress (see `docs/architecture/CONTEXTUAL_SESSION_PROTOCOL.md`).
4. Repeat until all intended changes are committed.
5. Synthesis: At the end of a session, synthesize all atomic commits into a single Contextualized Prompt commit as per the protocol.
6. Advance and push once:
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

## 7. Large Files and Binary Artifacts
**Committing large binary files (> 1MiB) or build artifacts is strictly forbidden.**

- **Safety Gate:** The Mentci Engine (`jj`) is configured to refuse snapshots of files exceeding size limits. Agents must **not** attempt to bypass this by increasing `snapshot.max-new-file-size` unless directed by the Top Admin.
- **VCS Integrity:** If a binary file accidentally enters the history, it must be immediately purged using `jj abandon` or history rewriting before pushing to `main`.
- **Proactive Ignoring:** New binary formats, compressed archives (`.tgz`, `.zip`), and directory-local artifacts should be added to `.gitignore` as soon as they are identified.

## 8. Related References
- Conceptual model: `docs/architecture/JAIL_COMMIT_PROTOCOL.md`
- Tooling overview: `docs/architecture/TOOLS.md`
- Release protocol: `docs/architecture/RELEASE_PROTOCOL.md`
