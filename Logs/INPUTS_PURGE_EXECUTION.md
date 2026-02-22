# Inputs History Purge Execution

Date: 2026-02-22

## Objective
Remove `Inputs/` and `inputs_backup/` recursively from all reachable Git history.

## Safety Snapshot
- Ref snapshot written: `/tmp/mentci-history-purge/show-ref-before.txt`
- Full backup bundle written: `/tmp/mentci-history-purge/pre-purge.bundle`

## Rewrite Command Executed
```bash
FILTER_BRANCH_SQUELCH_WARNING=1 \
  git filter-branch --force \
  --index-filter 'git rm -r --cached --ignore-unmatch Inputs inputs_backup' \
  --prune-empty --tag-name-filter cat -- --all
```

## Post-Rewrite Cleanup
```bash
git for-each-ref --format='%(refname)' refs/original | xargs -r -n 1 git update-ref -d
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```

## JJ Interaction Note
`jj` imported rewritten Git refs and temporarily produced conflicted descendant working revisions. The working copy was stabilized by creating a fresh working commit on rewritten `dev` (`jj new dev`), resulting in a clean state.
