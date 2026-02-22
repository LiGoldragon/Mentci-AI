# Inputs History Purge Verification

Date: 2026-02-22

## Verification Commands
```bash
git log --all --oneline -- Inputs inputs_backup | wc -l
git rev-list --objects --all | rg '(^| )Inputs/|(^| )inputs_backup/' | wc -l
```

## Results
- Commits touching purged paths in reachable history: `0`
- Reachable objects at purged paths: `0`

## Repository State
- Working copy clean after purge finalization.
- `Inputs/` and `inputs_backup/` remain ignored by `.gitignore`.

## Backup Artifacts
- `/tmp/mentci-history-purge/pre-purge.bundle`
- `/tmp/mentci-history-purge/show-ref-before.txt`
