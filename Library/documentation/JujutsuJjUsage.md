# Jujutsu (jj) usage guide for Git-trained agents

Source of truth: Core/VersionControlProtocol.md

This guide explains the JJ concepts most likely to confuse a Git-trained agent in this repository. It is concise by design. For formal protocol and completion rules, read @Core/VersionControlProtocol.md.

## Quick checklist for agents
- Treat `jj` as workflow authority. Git is backend transport only.
- Inspect bounded state first: `jj status`, bounded `jj log`, `jj diff --summary`.
- Do not move the runtime bookmark to an `(empty)` commit.
- Verify the remote bookmark on `origin` before claiming completion.
- Treat nested JJ repositories separately from root submodule metadata.

## 1. Git backend vs JJ authority
JJ may use a Git repository as the storage backend, but JJ's logical authority lives in JJ: bookmarks, change IDs, and the working-copy state. Do not assume Git refs or Git history are the source of truth for JJ workflow decisions. Use `jj` commands and JJ bookmarks for authoritative state changes.

## 2. Bookmarks vs branches
A JJ bookmark is the movable named pointer you should treat like a branch tip in day-to-day work. Git branches and JJ bookmarks are conceptually similar, but they are not the same control surface when JJ manages a Git backend.

Safe pattern:
```bash
env JJ_EDITOR=: VISUAL=: EDITOR=: jj log -r 'bookmarks("dev") | remote_bookmarks("dev@origin") | @ | @-' --no-graph -n 8
```

The repository protocol treats bookmark movement plus push as one atomic completion moment.

## 3. Change IDs vs commit IDs
JJ exposes both change IDs and commit IDs.
- Change ID: the logical identity of the change across rewrites.
- Commit ID: the specific instantiated revision.

Use change IDs when reasoning about logical work across rewrites. Use commit IDs when you need the exact backend revision currently referenced by a bookmark or remote.

## 4. The empty working-copy commit
JJ represents the working-copy state via an explicit working node. That node is often empty until real edits exist. This is normal.

Why agents get trapped:
- A Git-trained agent sees an empty node and tries to “delete” or “remove” it.
- JJ then creates or repositions another working node, because a working-copy commit is part of JJ's model.
- The agent loops, thinking the previous cleanup failed.

Correct mental model:
- The empty working-copy commit is not accidental debris by default.
- Do not try to remove it just because it is empty.
- Only move the runtime bookmark onto a non-empty finalized revision.
- A fresh empty child after completion is normal next-session preparation.

## 5. Origin verification
Local state is not authoritative completion state in this repository. `origin` is truth.

Safe pattern:
```bash
env JJ_EDITOR=: VISUAL=: EDITOR=: jj log -r 'bookmarks("dev") | remote_bookmarks("dev@origin")' --no-graph -n 4
```

Completion is not real until the finalized bookmark move has been pushed and verified on `origin`.

## 6. Bounded revsets
Never start with broad or unbounded revsets in this repository. Use explicit anchors and small limits.

Safer examples:
```bash
jj log -r '@ | @-' --no-graph -n 8
jj log -r 'bookmarks("dev") | remote_bookmarks("dev@origin") | @ | @-' --no-graph -n 8
jj log -r 'unresolved()' --no-graph -n 20
```

Avoid exploratory revsets like `all()` or broad head scans unless there is an explicit reason and a bounded scope.

## 7. Nested JJ repos vs Git submodules
These are different things.

- Nested JJ repo: a component with its own `.jj` authority. Treat it as a separate JJ context. Resolve its own bookmark before mutation.
- Git submodule: root repo metadata that records a gitlink plus `.gitmodules` mapping. Root submodule registration does not replace the need to use JJ inside a nested JJ repo.

Example: `Components/CriomOS` is both a root-tracked submodule path and its own JJ repo. Root repo metadata and nested-repo JJ workflow are separate concerns.

## 8. Common agent pitfalls
- Using Git as workflow authority in a JJ repo.
- Treating `(empty)` working nodes as errors that must be removed.
- Moving a target bookmark to `@` or another empty revision.
- Claiming completion before verifying `origin` alignment.
- Using unbounded revsets that dump too much history into context.
- Confusing root submodule metadata with nested JJ history authority.
- Continuing after contradictory evidence instead of running a deterministic post-gate.

## Further reading
- @Core/VersionControlProtocol.md
- @Core/AGENTS.md
- @Library/RestartContext.md
