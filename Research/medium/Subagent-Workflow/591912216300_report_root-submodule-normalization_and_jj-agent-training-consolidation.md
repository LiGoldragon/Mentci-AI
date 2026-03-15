# Root submodule normalization and JJ agent training consolidation

## Prompt
Normalize root submodules so they behave consistently like `Components/CriomOS`, including `Components/mentci-execute`, and consolidate JJ guidance into one training file for Git-trained agents. Wire that training into the independent-developer skill and fix the sema-programmer file-path link.

## Findings
- Root repo metadata mismatch was real at `.gitmodules` level:
  - `Components/mentci-execute` was tracked as a root gitlink (`160000`) but had no `.gitmodules` entry.
  - `.gitmodules` also contained a duplicate `Components/chronos-lib` block.
- `Components/maisiliym` was not the problematic case:
  - it was already present in both `.gitmodules` and the root git index as a gitlink.
- After normalization, `.gitmodules` now matches the root-tracked component gitlinks, including:
  - `Components/CriomOS`
  - `Components/maisiliym`
  - `Components/mentci-execute`
  - the other tracked `Components/*` gitlinks already recorded in root.
- `git submodule status --recursive` no longer fails with a missing-mapping fatal for `Components/mentci-execute`.
- `Components/mentci-execute` still appears with a leading `-` in `git submodule status` because the path currently contains an independent checkout rather than initialized superproject submodule metadata under `.git/modules/...`.
- For this session, repo-metadata normalization was the safe stopping point:
  - index gitlink + `.gitmodules` mapping are now consistent.
  - converting the local independent checkout into a fully initialized superproject-managed submodule would be a separate worktree-level maintenance step and may require network or manual backup/reconciliation.
- JJ training coverage was consolidated into one new doc:
  - `Library/documentation/JujutsuJjUsage.md`
- The new JJ training emphasizes the recurring agent trap around the empty working-copy commit:
  - JJ's empty working node is normal.
  - trying to “remove” it can produce more empty working nodes and agent loops.
  - the real rule is to never move the runtime bookmark to an empty commit and to verify `origin` before claiming completion.
- The `independent-developer` skill now explicitly points to:
  - `@Library/documentation/JujutsuJjUsage.md`
  - and retains the sema inherited-authority file-path reference:
    - `@.pi/skills/sema-programmer/SKILL.md`

## Verification
- `git submodule status --recursive`
  - no missing-mapping fatal remains
  - `Components/mentci-execute` is registered but uninitialized in this worktree (`-54d7...`)
- `cd Components/mentci-vcs && cargo test`
  - passed
- `cargo run --manifest-path Components/mentci-vcs/Cargo.toml -- sync-required-submodules`
  - `required submodule sync: 20 tracked, 0 need sync, 0 issues, mode=dry-run`

## Outcome
This session fixed the authoritative root metadata inconsistency and added one canonical JJ explainer for Git-trained agents. The remaining `Components/mentci-execute` difference is worktree-local initialization state, not a root metadata mismatch.
