# Playbook: Subject Consolidation Execution

## 1. Dry-Run Analysis
1. Enumerate current subjects:
```bash
ls -1 strategies
ls -1 Reports
```
2. Generate similarity candidates (name + token overlap + path overlap).

## 2. Approve Merge Map
1. Create `merge-map.edn`:
```edn
{:alias->canonical
 {"Expand-Guidelines-Reincorporation" "Guidelines-Wisdom-Reincorporation"
  "Guideline-Wisdom-Expansion-Subaspect" "Guidelines-Wisdom-Reincorporation"}}
```
2. Validate no canonical cycles.

## 3. Apply Merges
1. Move report files from alias topic to canonical topic.
2. Update canonical topic README entries.
3. Delete alias wrapper dirs when empty.
4. Rewrite stale references.

## 4. Verify
1. `rg` for alias names returns zero active references.
2. Subject parity still valid:
```bash
bb scripts/subject_unifier/main.clj --write
```
3. Queue paths in `Library/STRATEGY_QUEUE.md` are canonical.

## 5. Guard
1. Add alias-to-canonical mapping support in subject generation paths.
2. Fail on new alias subjects in CI/local checks.
