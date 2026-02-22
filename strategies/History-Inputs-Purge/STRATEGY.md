# Strategy: Purge `Inputs/` and `inputs_backup/` from VCS History

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Repository Integrity + VCS Purity)

## 1. Objective
Remove `Inputs/` and `inputs_backup/` recursively from the entire repository history and prevent reintroduction.

## 2. Why This Is Required
- `Inputs/` is an external substrate and must remain non-versioned runtime material.
- `inputs_backup/` is backup/import residue and should not occupy durable history.
- Large imported trees bloat clone/fetch cost and pollute audit clarity.

## 3. Scope
- Rewrite all reachable refs to remove:
  - `Inputs/`
  - `inputs_backup/`
- Preserve all other project history.
- Add durable guardrails to block future tracking of these paths.

## 4. Preconditions
1. Freeze active development on `dev`/`main` during rewrite window.
2. Snapshot current refs (`git show-ref`) and create safety backup clone or bundle.
3. Confirm no required source-of-truth files still exist only under purged paths.

## 5. Execution Plan
### Phase A: Safety + Analysis
1. Generate pre-rewrite metrics:
- largest commits by file-count/line-count
- largest blobs and path distribution
2. Generate candidate impact report listing commits touching `Inputs/` and `inputs_backup/`.
3. Save reports under `Logs/` for before/after comparison.

### Phase B: History Rewrite
Preferred tool: `git filter-repo`.

Canonical command shape:
```bash
git filter-repo \
  --path Inputs \
  --path inputs_backup \
  --invert-paths
```

Notes:
- Run in a dedicated maintenance clone/worktree.
- Rewrite all local refs, tags, and JJ keep refs that map into Git refs.

### Phase C: Ref Hygiene
1. Remove obsolete refs that still retain purged objects (including stale `refs/jj/keep/*` if they preserve pre-rewrite commits).
2. Expire reflogs and run aggressive garbage collection.
3. Verify purged paths are absent from full history scan.

### Phase D: Push + Team Migration
1. Force-push rewritten `dev` and `main`.
2. Instruct all consumers to reclone or hard-reset to rewritten history baseline.
3. Rotate CI caches/artifact caches that may reference old objects.

### Phase E: Prevention
1. Keep `.gitignore` entries for `Inputs/` and `inputs_backup/`.
2. Add pre-commit/pre-push guard script to fail if either path appears tracked.
3. Add CI check to block PRs that add files under these paths.
4. Add explicit rule to `Core/VERSION_CONTROL.md` as a hard gate.

## 6. Verification Checklist
1. `git log --all -- Inputs/ inputs_backup/` returns no commits.
2. `git rev-list --objects --all` has no objects under those paths.
3. Clone size and object counts decrease measurably.
4. JJ/Git workflows still function after ref cleanup.

## 7. Risks and Mitigations
1. **Risk:** Breakage from rewritten SHAs.
- Mitigation: maintenance window, migration playbook, ref snapshot backup.

2. **Risk:** Purged objects retained by hidden refs/reflogs.
- Mitigation: explicit ref audit (`refs/jj/keep/*`, tags, backup refs), gc.

3. **Risk:** Downstream automation pinned to old SHAs.
- Mitigation: publish mapping guidance and require repin.

## 8. Deliverables
- `Logs/INPUTS_PURGE_PRECHECK.md`
- `Logs/INPUTS_PURGE_EXECUTION.md`
- `Logs/INPUTS_PURGE_VERIFICATION.md`
- Guard script (proposed): `scripts/forbid_inputs_tracking/main.clj`

## 9. Acceptance Criteria
- No reachable commit contains `Inputs/` or `inputs_backup/` paths.
- Guardrails block reintroduction locally and in CI.
- Team migration completed with rewritten baseline accepted.

*The Great Work continues.*
