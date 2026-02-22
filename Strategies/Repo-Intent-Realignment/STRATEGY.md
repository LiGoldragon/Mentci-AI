# Strategy: Repo Intent Realignment

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Authority Recovery + Structural Purity)

## 1. Objective
Realign repository state with Core authority by removing artifacts created by misunderstood or superseded prompt branches, while preserving validated outputs that still serve active goals.

## 2. Source of Truth
1. `Core/AGENTS.md`
2. `Core/ARCHITECTURAL_GUIDELINES.md`
3. `Core/ASKI_FS_SPEC.md`
4. `Core/VERSION_CONTROL.md`
5. `Core/HIGH_LEVEL_GOALS.md`

Any file or flow conflicting with these authorities is remediation scope.

## 3. Prompt-History Diagnosis
Prompt history shows repeated branch pivots across:
1. Jail model changes (`nix develop` shell vs real bwrap jail vs service-based admin shell).
2. Migration churn (legacy input-root naming to `Sources`, component moves, execute rewrite).
3. Toolchain churn (Clojure scripts retained vs Rust replacement).
4. Recovery churn (conflict chains, abandoned/recovered commits, partial transplants).

Resulting pathology:
1. Partial implementations from superseded intents.
2. Index/readme drift across Strategies/Reports.
3. Legacy naming and compatibility references beyond intended window.
4. Duplicate operational surfaces for one capability.

## 4. Normalization Model
Classify each candidate artifact into exactly one bucket:
1. `retain`: aligns with Core and active roadmap.
2. `integrate`: useful but misplaced/incomplete; keep after relocation or completion.
3. `archive`: historically useful, operationally obsolete; keep in Reports only.
4. `remove`: harmful/conflicting/dead and unreferenced.

## 5. Workstreams
1. Authority Alignment Sweep:
- Build a machine-generated mismatch list: Core contract vs repository reality.
- Prioritize hard conflicts (FS contract, jail contract, VC/session protocol).

2. Artifact Reduction Sweep:
- Remove dead scripts and shadow wrappers replaced by canonical equivalents.
- Eliminate stale compatibility aliases after validation.

3. Strategy/Report Hygiene:
- Ensure every active strategy has a matching report topic and current README indexes.
- Move stale tactical notes out of active strategy files into historical reports.

4. Runtime Surface Simplification:
- Keep one canonical entrypoint per capability (`execute`, `mentci-ai`, `mentci-commit`, etc.).
- Remove duplicated command surfaces unless explicitly versioned as transitional.

## 6. Acceptance Criteria
1. No Core-authority violations in guardable paths.
2. No stale top-level contract aliases left active by default.
3. Every retained script/tool has a clear owner subject and test context.
4. Strategy queue reflects actual active work, not historical drift.
5. Conflict-recovery bookmarks/branches are no longer required for day-to-day development.

## 7. Execution Constraints
1. Do not edit `Sources/`.
2. Use atomic intent commits and report artifacts for each cleanup slice.
3. Run sweep gates after each slice:
- `bb Components/scripts/validate_scripts/main.clj`
- `bb Components/scripts/root_guard/main.clj`
- `bb Components/scripts/session_guard/main.clj`

## 8. First Cleanup Slices
1. Complete remaining legacy input naming cutover to `Sources` in active code paths and contracts.
2. Consolidate jail surfaces to one canonical model and remove old wrappers.
3. Remove stale script aliases after `execute` parity confirmation.
4. Prune obsolete report/strategy references left by abandoned branches.
