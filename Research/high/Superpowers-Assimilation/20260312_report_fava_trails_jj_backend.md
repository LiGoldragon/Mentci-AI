
# FAVA Trails: Programmatic Jujutsu Orchestration Report

## Overview
FAVA Trails orchestrates Jujutsu (JJ) programmatically through the `JjBackend` class in `Sources/fava-trails/src/fava_trails/vcs/jj_backend.py`. It utilizes a **colocated JJ mode** (sharing the same repository root with a Git backend) to manage data repositories without needing standard JJ worktrees.

## Key Findings

### Lock-Free Parallel Branching & Repo Locks
- **Shared Locks:** `JjBackend` uses a class-level `_repo_locks: dict[str, asyncio.Lock]` to ensure thread/task safety across multiple `JjBackend` instances working on the same repo root.
- **Serialization:** This lock serializes global operations (e.g., `push`, `fetch`, `gc`) preventing race conditions in a concurrent, asynchronous environment.
- **Path-Scoped Operations:** Most operations are path-scoped (e.g., `log`, `diff`, `commit`) using the `trail_path` relative to the repository root.

### FAVA's Equivalents in Jujutsu
- **Commit:** Implemented in `commit_files()`. It checks dirty paths, asserts no cross-trail pollution, performs `describe -m <message>` to assign a description (blocking phantom/empty commits), and then executes `new -m "(new change)"` to start a new mutable change state.
- **Checkout:** FAVA does not use a direct "checkout" command. Instead, it relies on JJ's natural ability to operate on the working copy `@` and commit mutations. Programmatic state changes are handled via `new` (creating a child commit) or `op restore` (rolling back to a previous operation).

### Implementation Strategy
- **Colocation:** Initialized via `git init --colocate` if needed, ensuring interoperability between Git and JJ.
- **Snapshot Mode:** Configures `ui.conflict-marker-style` to `snapshot` for robust, easily parseable conflict management.
- **Auto-Repair:** The `push` operation includes a `_repair_undescribed_commits()` step that automatically describes legacy empty commits to ensure pushability to remote git remotes.
