# Root Subrepo Sync And Worktree Helper Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Record the latest nested repository states in the root repo, add a policy-compliant helper for syncing nested repos into another worktree, and push the resulting non-empty history to `dev`.

**Architecture:** Treat each nested component repo as its own JJ/Git-backed world, but use the root repo to record the desired gitlink states. Implement any repeatable sync behavior as a Rust helper in `Components/mentci-vcs` rather than an ad-hoc shell script, then use JJ to finalize the root change and push it to the runtime bookmark.

**Tech Stack:** Jujutsu, Git submodule gitlinks, Rust (`Components/mentci-vcs`), Cargo tests, repo docs/plans.

---

### Task 1: Confirm root-tracked nested repo targets

**TDD scenario:** Modifying tested code — run existing checks first

**Files:**
- Inspect: `.gitmodules`
- Inspect: `Core/VersionControlProtocol.md`
- Inspect: `Components/mentci-vcs/src/lib.rs`

**Step 1: Re-run bounded status preflight**

Run: `env JJ_EDITOR=: VISUAL=: EDITOR=: jj status && git submodule status --recursive`
Expected: clean root JJ working copy; explicit nested repo drift visible.

**Step 2: Record exact nested states to sync**

Run: bounded repo-state checks for root gitlinks and nested HEADs.
Expected: list of root-recorded SHAs vs working nested SHAs.

**Step 3: Verify policy-compliant helper home**

Run: inspect `Components/mentci-vcs` and policy docs.
Expected: clear confirmation that new helper belongs in Rust, not bash.

**Step 4: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- verify the intended root sync set,
- confirm the target bookmark,
- confirm the resulting root commit will be non-empty.

### Task 2: Add the failing test for nested repo sync planning

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Modify: `Components/mentci-vcs/Cargo.toml`
- Modify: `Components/mentci-vcs/src/lib.rs`
- Test: `Components/mentci-vcs/tests/sync_nested.rs`

**Step 1: Write the failing test**

Create a test that builds a temporary repo layout and verifies the helper can derive sync actions for nested repo paths from `.gitmodules` / root gitlink state.

**Step 2: Run test to verify it fails**

Run: `cd Components/mentci-vcs && cargo test sync_nested -- --nocapture`
Expected: FAIL because the helper/API does not exist yet.

**Step 3: Write minimal implementation**

Add typed helper logic in `Components/mentci-vcs` that:
- reads root-tracked gitlink targets,
- compares them to current nested repo checkout HEADs,
- emits deterministic sync actions,
- supports a dry-run textual report.

**Step 4: Run test to verify it passes**

Run: `cd Components/mentci-vcs && cargo test sync_nested -- --nocapture`
Expected: PASS

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to verify the change remains non-empty and isolated.

### Task 3: Add a small CLI for another worktree

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Modify: `Components/mentci-vcs/Cargo.toml`
- Create: `Components/mentci-vcs/src/bin/sync_nested.rs`
- Test: reuse `Components/mentci-vcs/tests/sync_nested.rs`

**Step 1: Extend the failing test**

Add coverage for a dry-run / apply style interface or equivalent CLI entrypoint contract.

**Step 2: Run test to verify it fails**

Run: `cd Components/mentci-vcs && cargo test sync_nested -- --nocapture`
Expected: FAIL on missing CLI behavior.

**Step 3: Write minimal implementation**

Implement a small Rust binary that:
- takes a target worktree path,
- lists root-tracked nested repos,
- fetches/checks out the recorded root target for each nested repo in that worktree,
- defaults to dry-run unless `--apply` is provided.

**Step 4: Run tests**

Run: `cd Components/mentci-vcs && cargo test`
Expected: PASS

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to verify the root and nested repo state before commit.

### Task 4: Record the synced nested repo states in root and push

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: root gitlink entries for tracked nested repos

**Step 1: Stage desired nested repo pointers in root**

Run bounded staging for tracked nested repos only.
Expected: root diff shows gitlink updates and/or newly tracked gitlinks.

**Step 2: Verify non-empty root diff**

Run: `env JJ_EDITOR=: VISUAL=: EDITOR=: jj diff --summary`
Expected: non-empty summary.

**Step 3: Finalize session history**

Run JJ finalize flow with commit-context sections and push `dev` to origin.
Expected: local and remote `dev` aligned on a non-empty session commit.

**Step 4: Verify push landed**

Run bounded bookmark verification.
Expected: local/remote `dev` match.
