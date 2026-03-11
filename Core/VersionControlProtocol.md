# Version Control Protocol (JJ)

This document is the source of truth for Jujutsu workflows, commit discipline, and push cadence. **Jujutsu (jj) is the mandatory primary interface for all VCS operations in this repository; Git exists solely as the underlying storage backend.**

## 1. Core Rules (BOOMING MANDATE)
1. **TARGET BOOKMARK:** All active development targets the runtime bookmark from `MENTCI_TARGET_BOOKMARK` (default fallback may be `dev` when unresolved).
2. **END-OF-FLOW PUSH:** Every completed prompt session **MUST** end with a push to the runtime target bookmark on the `origin` remote.
3. **COMMIT EVERY INTENT:** One atomic modification per commit. No bundling.
4. **NO DIRTY TREES:** Finishing a turn with uncommitted changes is a protocol violation.
5. **ATOMIC MESSAGES + CONTEXT TRAILER:** Use `intent: <short description>` for intermediate commits, and include commit-context sections for every commit (see Rule 5.1).
5.1. **MANDATORY COMMIT CONTEXT (EVERY COMMIT):** Every commit message (including `intent:` commits) must persist three sections:
   - `## Prompt`
   - `## Context`
   - `## Summary`
   These sections may be concise, but they are mandatory for auditability.
6. **SESSION SYNTHESIS:** Full prompt/context attribution is reserved for final session synthesis per `Core/ContextualSessionProtocol.md`.
7. **PUSH VERIFICATION:** Always verify that the push was successful and the bookmark is visible on the remote.
6.1. Every `session:` commit description must include an explicit solar baseline line immediately after the title:
   - `<ZodiaUnicode>.<deg>.<min>.<sec> <Year>AM`
   - canonical shape: `spaceSeparated [dotSeparated [ZodiaUnicode deg min sec], concatenated [Year \"AM\"]]`
   - Gregorian-only date lines are not acceptable as the primary session timestamp.
7. Release default push target is `main`: when performing a release flow, push the release commit/tag to `main` unless explicitly overridden.
7.1. **MANDATORY SIGNED RELEASE TAGS:** Every release tag must be cryptographically signed (GPG or SSH signing).
   - Unsigned release tags are protocol-invalid.
   - Verify with: `git tag -v <tag>` (or equivalent verification command) before declaring release completion.
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
3. Commit using `intent:` title plus mandatory context sections (`## Prompt`, `## Context`, `## Summary`).
4. Repeat until all intended changes are committed.
5. Parallelization is allowed: related atomic intents may be developed on parallel revisions.
6. Session synthesis:
   - If there is exactly one sub-commit for the prompt: prepend that commit description with the final `session:` message block (do not add a separate final commit).
   - If there are multiple sub-commits: duplicate the sub-commit branch, squash the duplicated branch into one final `session:` commit, and append the duplicated sub-branch change IDs in the final message.
   - **Synthetic Context Recovery:** If original prompt, context, or logical changes are unrecoverable (e.g., during history rewrite), synthesize them based on the diff and mark them explicitly as `[SYNTHETIC]`.
6.1 Preferred automation for finalization:
   - Create a structured file at `.mentci/session.json` containing the following schema: `{"summary": "title...", "prompt": "Original prompt...", "context": "Agent context...", "model": "gemini-3-flash...", "changes": ["change 1", "change 2"]}`
   - Use `execute finalize` to read this file, synthesize a compliant `session:` message, and safely target the finalized non-empty revision.
   - This prevents moving bookmarks onto empty working-copy commits and guarantees data under subtitles is not missing.
6.2 Preferred automation for every intent commit:
   - Store commit metadata in `.mentci/commit-context.json` with keys `prompt`, `context`, and `summary`.
   - Generate commit messages from this metadata so every commit persists Prompt/Context/Summary without omission.

**Clean-Tree Preflight & Bookmark Safety:** Before finalizing any intent (including `session:` commits), run `jj status` and `jj diff --summary` so you can verify that tangible changes exist. Treat the working copy (`@`) as a deliberately anonymous, empty edit node; only describe it once there is actual content to capture. Finalizing a clean tree should happen only when there is an explicit reason (for example, sealing a metadata-only intent or preparing a new session), because moving `$MENTCI_TARGET_BOOKMARK` onto `@` or any described empty commit otherwise disconnects runtime history. Always resolve the runtime bookmark before repointing it—don’t leave the target bookmark floating on an undescribed empty node. As part of this preflight, confirm a research artifact lives in `Research/<priority>/<Subject>/` for the current prompt or intent, since completion is invalid without that coverage. After the physical work is described, invoke `execute session-guard` and `execute root-guard` to certify that the session synthesis and filesystem invariants are satisfied before pushing or reporting completion.

7. Before declaring the prompt complete, run `execute session-guard`; non-zero exit means session synthesis is missing or malformed.
8. Run `execute root-guard`; non-zero exit means top-level FS contract drift.
9. Advance and push once:
   - `jj bookmark set "$MENTCI_TARGET_BOOKMARK" -r @- --allow-backwards`
   - `jj git push --bookmark "$MENTCI_TARGET_BOOKMARK"`
10. This push-to-target-bookmark step is the default end-of-flow requirement for every prompt-complete execution.
11. Verify push landed before declaring completion:
   - `jj log -r "bookmarks($MENTCI_TARGET_BOOKMARK) | remote_bookmarks($MENTCI_TARGET_BOOKMARK@origin)" --no-graph`
   - completion is invalid until local and remote target bookmarks point to the finalized session lineage.
12. Only after successful push verification, optionally create a fresh child working commit for the next prompt:
   - `jj new "$MENTCI_TARGET_BOOKMARK"`
   - this new child is **next-session preparation**, not part of the completed session.
13. Do not abandon commits that are referenced by retained `session:` commit metadata (for example entries under `## Squashed Change IDs`), unless you also rewrite the referencing `session:` commit in the same rewrite sequence.
14. Every completed prompt must end with a finalized `session:` commit in the pushed target-bookmark lineage (`$MENTCI_TARGET_BOOKMARK`); leaving trailing `intent:` commits at prompt completion is a protocol violation.
14.1. Final `session:` commit message must include full context sections:
   - `## Original Prompt`
   - `## Agent Context`
   - `## Logical Changes`
   This is enforced by `execute session-guard`.
14.2. Session/release dating format is mandatory:
   - commit/session text uses `<ZodiaUnicode>.<deg>.<min>.<sec> <Year>AM`.
   - release notes and release commit messages must include both:
     1) Unicode zodiac chronography (`<ZodiaUnicode>.<deg>.<min>.<sec> <Year>AM`), and
     2) numeric protocol version string (for example `v0.12.9.59.28`).
   - release/version tags use cycle offset where `5919 AM -> 0`, `5920 AM -> 1`, etc.
15. Every completed prompt must emit/update a research artifact in `Research/<priority>/<Subject>/` (new file or existing subject update); prompts are not complete without research coverage.
16. Session push invariant: prompt completion is invalid until the finalized `session:` commit is pushed to the runtime target bookmark (`$MENTCI_TARGET_BOOKMARK`) and verified.

## 4. Dirty Tree Intent Separation
When the working copy is dirty and multiple change-intents may be present:
1. Enumerate change groups by file and purpose (proposed intent list).
2. Get approval for the grouping before proceeding.
3. For each intent group:
   - Isolate the group so only that change remains.
   - Commit the isolated group: `mentci-jj commit "intent: <short description>"`
4. After the final intent commit is ready, advance and push once:
   - `jj bookmark set "$MENTCI_TARGET_BOOKMARK" -r @`
   - `jj git push --bookmark "$MENTCI_TARGET_BOOKMARK"`

If splitting cannot be done safely, stop and request direction.

Empty working changes:
- Do not abandon or close empty JJ working changes by default; they may belong to another agent or worktree.

## Handling Side Bookmarks & Dangling Histories
Side bookmarks and dangling-looking histories are normal artifacts of parallel workstreams, rewrites, and intentional snapshots. When you encounter one, classify it into one of the following buckets before acting:
- **Active:** The bookmark represents work still in-flight. Leave it where it is, coordinate with the current owner, and avoid rebasing or pruning it until the owner seals their intent.
- **Integrated:** The bookmark’s changes have already merged into `$MENTCI_TARGET_BOOKMARK` or another canonical line. Treat it as historical evidence and do not reapply it unless you have new intent.
- **Intentionally preserved:** Release candidates, research snapshots, or emergency patches that are kept aside on purpose. Document the preservation rationale, responsible agent, and whether it needs periodic maintenance.
- **Cleanup candidate:** The bookmark is stale, abandoned, or no longer needed. Coordinate with `jj-expert` (or the owner) to prune or squash it safely, supplying before/after evidence so its removal does not confuse downstream readers.
Record each classification explicitly and verify whether downstream commit IDs or change IDs depend on the bookmark before merging or dropping it. When inspecting these histories, avoid running broad unbounded `jj log` revsets; prefer targeted revsets (for example `bookmarks(<name>)` or `@-` neighbors) and only expand once you understand the classification. This workflow keeps the runtime history manageable while still surfacing the purpose of each side bookmark.

### Time-Window Cleanup Scope
If the user asks to clean history within a time window (for example "last day" or "last 3 days"), do not limit the audit to the active target lineage alone. You must also inspect bounded detached visible heads, rewrite debris, duplicate-change remnants, and side bookmarks whose timestamps fall inside that window. Classify each candidate as preserved milestone, active side line, or cleanup candidate before acting. A cleanup is incomplete if obvious detached heads from the requested time window remain visible and continue polluting `jj log`/`visible_heads()`.

### Guard Failure Recording
If `execute session-guard` or `execute root-guard` fails because of a missing prerequisite, missing sidecar, malformed metadata, or other environmental blocker, record that blocker in a Research artifact before treating the session as complete. Do not let a contradictory subagent summary hide the failure; run direct bounded post-gates and report whether the bookmark actually moved, whether the push actually landed, and what guard remains unsatisfied.

## 5. Jailed Shipping (Workspace -> target bookmark)
When operating in the jailed workspace, always use `mentci-commit` to advance the target bookmark:

```
mentci-commit "intent: <short message>"
```

This uses `MENTCI_WORKSPACE`, `MENTCI_REPO_ROOT`, and `MENTCI_COMMIT_TARGET` to move the bookmark safely.

## 6. History Inspection
Use `mentci-jj log` to prefer `--no-signing` and avoid GPG failures.

**OOM Guard:** Never run unbounded JJ revsets in this repository (for example `all()`, `heads(all())`, or deep unbounded ancestry). Always start with bounded revsets and explicit limits.

### Change ID vs Commit ID
Jujutsu reports both a change ID and a commit ID for every revision. The change ID captures the logical content of the change and survives history rewrites, while the commit ID is the unique revision handle that changes whenever you rebase, reword, or replay that change elsewhere. When reasoning about shared work, focus on change IDs to understand which logical intent you are revisiting and use commit IDs when you need to reference the exact copy of that intent. Having this split in mind prevents erroneous assumptions about what bookmarks, push logs, or verification scripts are pointing to. Always resolve the runtime bookmark first so you know which identifier you should be aligning before moving it.

### Duplicate Change IDs
If you ever see the same change ID appearing on more than one visible revision, it usually means the same logical change was rewritten or replayed into multiple branches—this is a divergence/rewrite exposure signal, not corruption. Use it as a clue: inspect both revisions (parents, bookmarks, whether `$MENTCI_TARGET_BOOKMARK` touched them) and classify each path (active, integrated, preserved, cleanup candidate). After understanding the duplication, decide whether to merge the branch, drop the extra copy, or leave both as historical evidence. Duplicates silently confirm that multiple viewpoints exist for the same intent; they do not mean your repository is broken, but they might mean two sequences still need reconciliation.

## 7. Related References
- Conceptual model: `Library/architecture/JailCommitProtocol.md`
- Tooling overview: `Library/architecture/ToolStack.md`
- Release protocol: `Library/architecture/ReleaseProtocol.md`
