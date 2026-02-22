# Priority Triage: Strategy and Report Subjects

## Objective
Classify every current subject category under `Development/` and `Research/` into:
- `high`
- `medium`
- `low`

The classification is aligned to `Development/high/Repo-Intent-Realignment/STRATEGY.md`:
- authority recovery first
- operational resiliency first
- remove or absorb redundant planning tracks

## Canonical Priority Matrix

| Subject | Priority | Consolidation Note |
| :--- | :--- | :--- |
| `Repo-Intent-Realignment` | `high` | Canonical cleanup owner subject. |
| `Strategy-Development` | `high` | Keep as top execution-enablement subject. |
| `Agent-Authority-Alignment` | `high` | Keep as authority contract enforcement subject. |
| `Artifact-Sweep` | `high` | Keep as artifact removal and drift-prevention subject. |
| `Top-Level-FS-Spec` | `high` | Keep as filesystem contract authority subject. |
| `Commit-Protocol-Merge-Fanin` | `high` | Keep as session/commit protocol authority subject. |
| `Debugging` | `high` | Keep as deterministic defect-sweep subject. |
| `Project-Hardening` | `high` | Keep as structural hardening subject. |
| `Workspace-Pwd-History-Purge-And-Jj-Config-Migration` | `high` | Keep as history/runtime-boundary hardening subject. |
| `Recover-Lost-Work-After-Rewrite` | `high` | Keep until recovery scope is fully closed. |
| `Scripts-Rust-Migration` | `high` | Keep until script-surface replacement parity is complete. |
| `Prompt-Report-System` | `high` | Keep as reporting/session hygiene control subject. |
| `Subject-Consolidation` | `medium` | Keep; apply only when duplicates are proven. |
| `Component-Dependency-Flow` | `medium` | Track under `Project-Hardening` for implementation slices. |
| `Core-Library-Symbiotic-Move` | `medium` | Track under `Repo-Intent-Realignment` for ownership hygiene. |
| `Guidelines-Wisdom-Reincorporation` | `medium` | Keep as guideline-quality uplift subject. |
| `Jail-Crypto-Key-Passing` | `medium` | Keep as security implementation subject. |
| `Attractor` | `medium` | Keep for Goal 1 assimilation path. |
| `Aski-Conversion` | `medium` | Consolidate planning cross-links with `Aski-Refinement`. |
| `Aski-Refinement` | `medium` | Consolidate planning cross-links with `Aski-Conversion`. |
| `Malli-Verbosity-Reduction` | `medium` | Keep while Clojure script surface remains active. |
| `Align-Release-Tag-To-Commit-Zodia-Ordinal-Time` | `low` | Fold ongoing tracking into `Commit-Protocol-Merge-Fanin`. |
| `STT-Context-Decoding` | `low` | Keep parked until core stability gates are green. |
| `Universal-Program-Pack` | `low` | Keep parked; avoid new implementation slices now. |
| `Mentci-RFS` | `low` | Keep as deferred, high-risk UX/system shift. |
| `Zodiac-Strategy-Division` | `low` | Fold ongoing tracking into `Commit-Protocol-Merge-Fanin`. |

## Consolidation Policy for This Slice
1. No subject directories are deleted in this slice.
2. Redundant tracking is consolidated by canonical ownership notes in the matrix above.
3. Future report entries for low-priority folded subjects should be redirected to:
- `Commit-Protocol-Merge-Fanin` for release/timestamp/zodiac tracking.
- `Repo-Intent-Realignment` for ownership and scope cleanup.

## Immediate Execution Order (`high` only)
1. `Strategy-Development`
2. `Repo-Intent-Realignment`
3. `Agent-Authority-Alignment`
4. `Top-Level-FS-Spec`
5. `Commit-Protocol-Merge-Fanin`
6. `Artifact-Sweep`
7. `Project-Hardening`
8. `Scripts-Rust-Migration`
9. `Workspace-Pwd-History-Purge-And-Jj-Config-Migration`
10. `Debugging`
11. `Prompt-Report-System`
12. `Recover-Lost-Work-After-Rewrite`
