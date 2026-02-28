# Strategy: Crypto-Content-Addressed Rebase System

## Core Problem
Currently, using full DVCS (Jujutsu) workspaces or heavy monorepos leads to:
1. **Repository Bloat**: Old files, ancient commits, and deprecated architectural experiments remain in the model's context window.
2. **Context Poisoning**: LLMs read ancient, irrelevant commits or files as "ground truth", slowing down development and leading to hallucinated dependencies on deprecated code.
3. **Workspace Stale Issues**: Using standard `jj` worktrees across boundaries causes state/race conflicts (e.g., `Error: The working copy is stale`) because they share the underlying operation log.

## The Author's Intent
Transition from shared-operation-log `jj` worktrees to **Independent Full `jj` Clones**, orchestrated by a new architectural concept:

> **Crypto-Content-Addressed Rebase System**

### Mechanism Design
Instead of a standard Git/JJ repository growing infinitely with every change:
1. **The Cap'n Proto "Commit"**: Instead of a simple text message, a "commit" is a structured Cap'n Proto message.
2. **Lineage Linking**: This message links cryptographically to an ancestor tree (a minimal, single-straight-trunk clone of its parent state).
3. **Content Archiving**: The ancestor state is stored in an archiving format (making it fast to verify, hash, and transmit).
4. **Context Pruning**: When a big redesign happens, a new root is declared. The LLM only sees the active minimal surface area. The "ancient history" is mathematically linked but physically archived out of the active context window.

## Resolution to Current Friction
1. **Nix Dev Problem**: A clean, isolated clone guarantees that `nix develop` evaluates against an immutable, self-contained root without cross-contamination.
2. **The Stale Problem**: Independent clones have their own `jj` op-logs. Rebaing `dev` in one tree has zero mechanical effect on the op-log of another tree.

## Next Steps for Implementation (Sema-Grade)
1. Model the new Cap'n Proto schema for this "Structured Commit Message" (`MentciCommit` or similar).
2. Define the archiving format specification.
3. Update the agent skills (`independent-developer`, `sema-programmer`) to enforce this new workflow over traditional `jj workspace` branching.
