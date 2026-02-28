---
name: using-git-worktrees
description: Use when starting feature work that needs isolation from current workspace - orchestrates independent jj clones using the content-addressed rebase strategy.
---

# Using Git Worktrees (Updated for Fractal DVCS)

## Overview
This skill governs the creation of isolated workspaces. It has been upgraded from traditional `git worktree` / `jj workspace` commands to the **Crypto-Content-Addressed Rebase** strategy.

## Protocol: Independent Clones

1. **No Shared Op-Logs**: You must not use `jj workspace add` for parallel agent flows, as this creates a shared operation log that leads to `stale working copy` races during concurrent rebases.
2. **Full Clones**: To isolate work, perform a full `jj git clone` of the target repository into a new, distinct directory.
3. **Spacename Ownership**:
    - The new clone MUST be assigned an `ownedSpacename` (a designated bookmark or bookmark-prefix it has exclusive rights to mutate).
    - You must write this claim to the root before commencing work.

## Execution Flow

1. Determine the path for the new clone: `../Mentci-AI--<intent>`
2. Execute `jj git clone <source_repo> <target_path>`
3. Set up the local environment (e.g., `direnv allow`).
4. Switch to the newly created directory.
5. Create the root non-writable `authority.bin` (or equivalent Cap'n Proto `MentciCommit` message) asserting the `ownedSpacename`.
