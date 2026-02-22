# Playbook: Workspace/Pwd History Purge + JJ Config Local Override

## 0. Execution Policy
1. JJ-first for normal operations (status, commit evolution, bookmark alignment, push).
2. Git-only exceptions in this playbook are limited to:
- full-history rewrite (`git filter-repo`)
- rewrite cleanup (`git reflog expire`, `git gc`)
- backup bundle creation (`git bundle create`)

## 1. Preconditions
1. Maintenance window approved.
2. Clean working tree in maintenance clone.
3. Backup refs captured:
```bash
git show-ref > /tmp/pre-rewrite-show-ref.txt
git bundle create /tmp/mentci-pre-workspace-pwd-purge.bundle --all
```

## 2. Migrate JJ Config Off Tracked File
1. Add local override path and ignore it.
2. Update shell/docs so `JJ_CONFIG` points to local override only when available.
3. Verify:
```bash
test -f .mentci/local/jj/config.toml || true
jj config list >/dev/null
```

## 3. Rewrite History
```bash
git filter-repo \
  --path workspace \
  --path '$(pwd)' \
  --invert-paths
```

## 4. Cleanup and Verify
```bash
git for-each-ref --format='%(refname)' refs/original/ | xargs -r -n 1 git update-ref -d
git reflog expire --expire=now --all
git gc --prune=now --aggressive

git log --all -- workspace '$(pwd)'
git rev-list --objects --all | rg '(workspace/|\\$\\(pwd\\)/)'
```

## 5. Reconcile and Publish
1. Run `jj git import` after Git rewrite/cleanup.
2. Align bookmarks (`dev`, `main`) to intended rewritten commits.
3. Publish with JJ first:
```bash
jj git push --remote origin --bookmark dev --bookmark main
```
4. If JJ push safety checks block required rewrite publication and manual confirmation is complete, use controlled fallback:
```bash
git push --force origin dev main
```
5. Recreate/push release tag if required by current policy.

## 6. Consumer Migration Notice
1. Announce forced history rewrite.
2. Require reclone or hard reset to rewritten baseline.
3. Invalidate caches referencing old object IDs.
