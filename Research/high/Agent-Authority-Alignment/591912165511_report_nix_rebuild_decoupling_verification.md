# Nix Rebuild Decoupling Verification

## Goal
Stop unrelated Mentci repository edits from forcing split Rust component rebuilds in routine package builds.

## Implemented changes
1. **Bootstrap minimization**
   - `Components/nix/mentci_ai.nix` now packages `ai-src` (bootstrap shim) instead of `mentci-aid-src` orchestration surface.
   - `flake.nix` and `Components/nix/default.nix` were wired with `ai_src`.

2. **Execute decoupling completion**
   - `execute` remains a dedicated derivation (`Components/nix/execute.nix`) and no longer depends on `${mentci_ai}/bin/execute`.
   - `Components/nix/execute_check.nix` now validates `${execute}/bin/execute`.

3. **Repo-root path coupling removal**
   - Removed `repo_root + "/Cargo.lock"` from split component derivations.
   - Removed `${repo_root}/Components/schema` dependency injection.
   - Replaced schema wiring with scoped `schema_src = ../schema` path passing in `Components/nix/default.nix`.

4. **Lockfile strategy stabilization**
   - Components with own lockfiles use `src + "/Cargo.lock"`: `mentci_ai`, `execute`, `chronos`.
   - Components currently lacking lockfiles use `../../Cargo.lock`: `mentci_user`, `mentci_mcp`, `mentci_stt`, `mentci_launch`, `mentci_box`.
   - `mentci_box` was pinned to shared lock after stale upstream lock mismatch (`tempfile` vendoring failure).

## Verification evidence

### Full package build run
Executed:
- `nix build --no-link .#mentciAi .#execute .#chronos .#mentciStt .#mentciUser .#mentciMcp .#mentciLaunch .#mentciBox .#mentciBoxDefault .#pi .#piWithExtensions .#unifiedLlm .#vtcode`

Result:
- Succeeded after lock strategy fixes.

### Rebuild trigger checks after unrelated commits
After additional tiny docs commits, executed:
- `nix build --dry-run --no-link ...same attr set...`

Result:
- **No derivations scheduled** (empty dry-run output).

### Dirty tree probe
With an uncommitted docs-only modification present, executed:
- `nix build --dry-run --no-link ...same attr set...`

Result:
- Warning: dirty git tree
- **No derivations scheduled** (empty dry-run output)

## Conclusion
For the tested package set, unrelated repo commits and dirty-tree docs edits no longer trigger component rebuild churn. The prior broad coupling through `repo_root` in lock/schema plumbing is no longer the active rebuild driver for these targets.
