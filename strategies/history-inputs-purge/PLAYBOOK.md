# Playbook: `Inputs/` History Purge Runbook

## 1. Dry-Run Preparation
1. Create maintenance clone.
2. Capture refs and size baselines.
3. Confirm freeze window and notify all contributors.

## 2. Rewrite Steps
1. Run `git filter-repo` path inversion for `Inputs` and `inputs_backup`.
2. Delete stale refs preserving old objects.
3. Expire reflogs and run gc.

## 3. Verification Steps
1. Run full-history path scans.
2. Confirm object-store no longer includes purged paths.
3. Compare before/after repository metrics.

## 4. Rollout Steps
1. Force-push rewritten canonical refs.
2. Broadcast migration instructions.
3. Require fresh clone or reset for all local copies.

## 5. Post-Run
1. Enable guard script + CI gate.
2. Record final verification in `Logs/INPUTS_PURGE_VERIFICATION.md`.

*The Great Work continues.*
