# Version Control Protocol (JJ)

This document is the source of truth for Jujutsu workflows, commit discipline, and push cadence. **Jujutsu (jj) is the mandatory primary interface for all VCS operations in this repository; Git exists solely as the underlying storage backend.**

## 1. Core Rules
1. All active development targets the `dev` bookmark.
2. End-of-flow default is mandatory: every completed prompt session must end with a push to `dev` unless the user explicitly overrides the target.
3. Commit every intent: one atomic modification per commit, no bundling.
4. Atomic commit messages are minimal and must use only: `intent: <short description>`.
5. Full prompt/context attribution is reserved for final session synthesis per `Core/ContextualSessionProtocol.md`.
6. **Session completion gate:** a prompt is incomplete unless finalization follows the single-vs-multi sub-commit synthesis rules in `Core/ContextualSessionProtocol.md`.
6.1. Every `session:` commit description must include an explicit solar baseline line immediately after the title:
   - `<ZodiaUnicode>.<deg>.<min>.<sec> <Year>AM`
   - canonical shape: `spaceSeparated [dotSeparated [ZodiaUnicode deg min sec], concatenated [Year \"AM\"]]`
   - Gregorian-only date lines are not acceptable as the primary session timestamp.
7. Release default push target is `main`: when performing a release flow, push the release commit/tag to `main` unless explicitly overridden.
8. Aggressive auto-commit: any filesystem change must be committed immediately. Do not wait for explicit user prompts like "commit everything."
9. Per-prompt dirty-tree auto-commit: if the working copy is dirty at the start of a prompt, create a commit before making any new changes. After completing the prompt, create at least one new commit for the prompt's work.
10. **Hard pre-edit gate:** if the tree is dirty at prompt start, stop implementation, isolate pre-existing intent(s), and commit them before touching any additional files.

## 2. Preconditions
1. Prefer working in the dev shell so `MENTCI_*` variables and the jail workspace are active.
2. Use `mentci-jj` for status/log/commit to ensure consistent workspace targeting.
3. Run a pre-edit status check (`mentci-jj status` or `jj status`) before any file read/write intended to change code or docs.

If `MENTCI_*` variables are missing, use `jj` directly from the repository root and do not attempt jail shipping.

## 3. Atomic Change Loop
1. Make exactly one atomic change.
2. Verify status: `mentci-jj status`
3. Commit with minimal intent message only: `mentci-jj commit "intent: <short description>"`
4. Repeat until all intended changes are committed.
5. Parallelization is allowed: related atomic intents may be developed on parallel revisions.
6. Session synthesis:
   - If there is exactly one sub-commit for the prompt: prepend that commit description with the final `session:` message block (do not add a separate final commit).
   - If there are multiple sub-commits: duplicate the sub-commit branch, squash the duplicated branch into one final `session:` commit, and append the duplicated sub-branch change IDs in the final message.
6.1 Preferred automation for finalization:
   - Use `bb Components/scripts/session_finalize/main.clj` to synthesize a compliant `session:` message and safely target the finalized non-empty revision.
   - This prevents moving bookmarks onto empty working-copy commits.
7. Before declaring the prompt complete, run `bb Components/scripts/session_guard/main.clj`; non-zero exit means session synthesis is missing or malformed.
8. Run `bb Components/scripts/root_guard/main.clj`; non-zero exit means top-level FS contract drift.
9. Advance and push once:
   - `jj bookmark set dev -r @- --allow-backwards`
   - `jj git push --bookmark dev`
10. This push-to-`dev` step is the default end-of-flow requirement for every prompt-complete execution.
11. Verify push landed before declaring completion:
   - `jj log -r 'bookmarks(dev) | remote_bookmarks(dev@origin)' --no-graph`
   - completion is invalid until local `dev` and remote `dev@origin` point to the finalized session lineage.
12. Only after successful push verification, optionally create a fresh child working commit for the next prompt:
   - `jj new dev`
   - this new child is **next-session preparation**, not part of the completed session.
13. Do not abandon commits that are referenced by retained `session:` commit metadata (for example entries under `## Squashed Change IDs`), unless you also rewrite the referencing `session:` commit in the same rewrite sequence.
14. Every completed prompt must end with a finalized `session:` commit in the pushed `dev` lineage; leaving trailing `intent:` commits at prompt completion is a protocol violation.
14.1. Final `session:` commit message must include full context sections:
   - `## Original Prompt`
   - `## Agent Context`
   - `## Logical Changes`
   This is enforced by `bb Components/scripts/session_guard/main.clj`.
14.2. Session message timestamp format is mandatory:
   - commit/session text uses `<ZodiaUnicode>.<deg>.<min>.<sec> <Year>AM`
   - release/version tags use cycle offset where `5919 AM -> 0`, `5920 AM -> 1`, etc.
15. Every completed prompt must emit/update a research artifact in `Research/<priority>/<Subject>/` (new file or existing subject update); prompts are not complete without research coverage.
16. Session push invariant: prompt completion is invalid until the finalized `session:` commit is pushed (default `dev`) and verified.

## 4. Dirty Tree Intent Separation
When the working copy is dirty and multiple change-intents may be present:
1. Enumerate change groups by file and purpose (proposed intent list).
2. Get approval for the grouping before proceeding.
3. For each intent group:
   - Isolate the group so only that change remains.
   - Commit the isolated group: `mentci-jj commit "intent: <short description>"`
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
- **Reference safety:** Any purge/rewrite plan that uses `jj abandon` must first verify it will not invalidate references recorded in retained `session:` commit metadata.
- **Proactive Ignoring:** New binary formats, compressed archives (`.tgz`, `.zip`), and directory-local artifacts should be added to `.gitignore` as soon as they are identified.

## 8. Related References
- Conceptual model: `Library/architecture/JailCommitProtocol.md`
- Tooling overview: `Library/architecture/ToolStack.md`
- Release protocol: `Library/architecture/ReleaseProtocol.md`
