# Strategy: Workspace/Pwd History Purge and JJ Config Migration

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Repository Integrity + Tooling Reliability)

## 1. Objective
1. Remove accidental/non-canonical paths from full VCS history:
- `workspace/` (historical stale tree content)
- `$(pwd)/` (accidental literal top-level directory)
2. Migrate `jj-project-config.toml` usage to a per-use local override file that is gitignored.

## 2. Current State
1. `workspace/` is intentionally used by jail tooling at runtime, but historical files under that path were legacy snapshot material and should not remain in durable Git history.
2. `$(pwd)/jj-project-config.toml` exists as an accidental artifact with no canonical top-level role.
3. `nix/dev_shell.nix` currently sets `JJ_CONFIG=\"$(pwd)/jj-project-config.toml\"`, coupling shell behavior to a tracked file.

## 3. Target State
1. No reachable commit contains tracked content under `workspace/` or `$(pwd)/`.
2. Runtime workspace remains available as ephemeral, gitignored operational state.
3. JJ config behavior is driven by a local, per-use config path, e.g. `.mentci/local/jj/config.toml` (gitignored), with safe fallback to defaults.
4. Default VCS execution path is JJ-first, with a narrow explicit set of Git-only exception operations.

## 3.1 JJ-First Coverage Findings
Based on repository tooling and `jj 0.36.0` command surface:

1. Fully covered by JJ in normal flow:
- Commit metadata/content evolution (`describe`, `commit`, `split`, `squash`, `rebase`, `abandon`)
- Branch/bookmark operations (`bookmark *`)
- Remote sync (`git fetch`, `git push`, `git remote`)
- Tag management (`tag set/list/delete`)
- Workspace orchestration (`workspace *`)
- Recovery and timeline (`operation log/show/revert/restore`)

2. Partially covered / interop-sensitive:
- External Git-side mutations require `jj git import` to synchronize JJ view.
- In colocated repos this is often automatic, but explicit `jj git import` remains the safe reconciliation step after direct Git operations.

3. Git-unavoidable for this strategy class:
- Full-repository path purges and object-level rewrites (`git filter-repo`).
- Certain object store/ref maintenance tasks (`git reflog expire`, aggressive `git gc`) after rewrite.
- Backup bundle creation with broad Git compatibility (`git bundle create`), if required by migration policy.

Conclusion:
- Use JJ for all standard development and remote publication paths.
- Use raw Git only for history surgery and post-surgery object/ref hygiene.

## 4. Execution Plan
### Phase A: Config Migration First (No History Rewrite Yet)
1. Introduce a local override variable in dev shell:
- `MENTCI_JJ_CONFIG_LOCAL` (default path: `.mentci/local/jj/config.toml`)
2. Set `JJ_CONFIG` to the local override if present, else unset/fallback to JJ defaults.
3. Add `.mentci/local/` (or selected local config path) to `.gitignore`.
4. Keep a tracked template file for onboarding only, e.g. `Library/templates/jj-config.example.toml`.
5. Update docs in `Core/VERSION_CONTROL.md` and relevant tooling docs to explain per-use local setup.

### Phase B: Guardrails
1. Add reference guard rules to reject:
- tracked `workspace/**` (except explicitly allowed runtime markers if needed)
- tracked `$(pwd)/**`
- tracked local JJ config override path
2. Add CI/local validation script check for forbidden tracked paths.

### Phase C: Full History Rewrite
1. Freeze pushes and create a rewrite maintenance clone.
2. Snapshot refs and tags (`git show-ref`) and create backup bundle.
3. Run Git-only history surgery (`git filter-repo`) with inverted paths for:
- `workspace`
- `$(pwd)`
4. Run Git-only post-rewrite cleanup (`git reflog expire`, `git gc`).
5. Validate no reachable path references remain:
- `git log --all -- workspace '$(pwd)'`
- `git rev-list --objects --all | rg '^(.*\\s)?(workspace/|\\$\\(pwd\\)/)'`

### Phase D: JJ/Git Ref Reconciliation
1. Import rewritten refs in JJ (`jj git import`) and resolve bookmark targets.
2. Confirm `dev` and `main` point to intended post-rewrite commits.
3. Prefer JJ for publication (`jj git push --bookmark dev --bookmark main`) unless emergency remote repair requires raw `git push --force`.
4. Recreate/push release tag if needed based on commit-time chronography.

### Phase E: Workspace Lifecycle Hardening
1. Add startup freshness policy:
- if workspace is stale/mismatched, recreate from current trunk.
2. Treat `workspace/` as disposable runtime area only.
3. Keep durable artifacts in canonical roots only.

## 5. Risks and Mitigations
1. Risk: rewrite breaks downstream clones and pinned SHAs.
- Mitigation: migration notice + mandatory reclone/reset instructions.
2. Risk: JJ bookmarks/tags drift after rewrite.
- Mitigation: explicit post-rewrite bookmark audit and forced alignment.
3. Risk: local JJ config missing causes UX regression.
- Mitigation: provide example template and fallback behavior.

## 6. Acceptance Criteria
1. `workspace/` and `$(pwd)/` absent from all reachable history.
2. No tracked local JJ config override file.
3. Dev shell and commit tooling operate correctly with local JJ config override.
4. Guardrails block reintroduction of purged paths.

## 7. Deliverables
1. Config migration patch (shell/docs/ignore/template).
2. Rewrite playbook and verification log.
3. Post-rewrite migration notice and ref alignment log.
