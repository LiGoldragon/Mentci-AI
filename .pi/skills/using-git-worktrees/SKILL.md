---
name: using-git-worktrees
description: Use when starting feature work that needs isolation from current workspace - orchestrates independent jj clones using the content-addressed rebase strategy.
---

## Repo-Local Nix Purity Rule
- Treat every repository as a self-contained world during Nix evaluation.
- Never reference files from a parent repo, sibling checkout, ad-hoc absolute path, or undeclared local path escape inside Nix code.
- If reusable Nix code is needed, it must live inside the active repository or arrive through a declared flake input; if we create that code, it belongs in a repository and our repository workflow remains Git-backed JJ.
- Deep modules must not `../`-escape repo boundaries to find package code. Root-wire shared derivations from the active repo root and pass them down through module arguments / `specialArgs`.


# Using Git Worktrees (Updated for Fractal DVCS)

## Overview
This skill governs the creation of isolated workspaces. It has been upgraded from traditional `git worktree` / `jj workspace` commands to the **Crypto-Content-Addressed Rebase** strategy.

## Protocol: Independent Clones

All non-trivial JJ/git handling in this skill MUST go through the `jj-agent` agent by default. This skill defines the isolation policy; `jj-agent` performs and verifies the clone/setup VCS steps. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

Direct Git workflow usage is heresy; Git is backend transport only. A clone/setup flow is not complete until the resulting bookmark movement and push to `origin` have both happened and been verified.

1. **No Shared Op-Logs**: You must not use `jj workspace add` for parallel agent flows, as this creates a shared operation log that leads to `stale working copy` races during concurrent rebases.
2. **Full Clones**: To isolate work, perform a full `jj git clone` of the target repository into a new, distinct directory.
3. **Spacename Ownership**:
    - The new clone MUST be assigned an `ownedSpacename` (a designated bookmark or bookmark-prefix it has exclusive rights to mutate).
    - You must write this claim to the root before commencing work.

## Execution Flow

1. Determine the path for the new clone: `../Mentci-AI--<intent>`
2. Ask `jj-agent` to perform the bounded clone/setup VCS steps and return evidence for the created workspace state. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.
3. Set up the local environment (e.g., `direnv allow`).
4. Switch to the newly created directory.
5. Create the root non-writable `authority.bin` (or equivalent Cap'n Proto `MentciCommit` message) asserting the `ownedSpacename`.
