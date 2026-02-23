# Stale Intel Scan (Post-Move)

## Scope
Check for stale authority references after Core/Library migration.

## Scan Anchors
- scan_change_id: `ltkttsyqmozk`
- parent_change_id: `xzlswptlmxkn`

## Results
### Stale Authority Paths
Patterns:
- `docs/architecture/VersionControlProtocol.md`
- `docs/guides/RestartContext.md`
- `docs/architecture/SEMA_*_GUIDELINES.md`

Matches:
```
tasks/high_level_goals/goal_1_attractor_dot_job_handoff.md:56:- [x] Add a short run summary to `docs/guides/RestartContext.md` (date, commit id, artifact paths).
workflows/mentci.aski-fs.deps.edn:4:          {:path "docs/architecture/VersionControlProtocol.md" :section "Auto-Commit"}]}
workflows/mentci.aski-fs.deps.edn:6:   :read [{:path "docs/architecture/SEMA_CLOJURE_GUIDELINES.md" :section "Primary Rules"}
workflows/mentci.aski-fs.deps.edn:7:          {:path "docs/architecture/SEMA_RUST_GUIDELINES.md" :section "Primary Rules"}
workflows/mentci.aski-fs.deps.edn:8:          {:path "docs/architecture/SEMA_NIX_GUIDELINES.md" :section "Primary Rules"}]}
workflows/mentci.aski-fs.deps.edn:9:  {:when {:touch "docs/architecture/SEMA_CLOJURE_GUIDELINES.md"}
workflows/mentci.aski-fs.deps.edn:11:  {:when {:touch "docs/architecture/SEMA_RUST_GUIDELINES.md"}
workflows/mentci.aski-fs.deps.edn:13:  {:when {:touch "docs/architecture/SEMA_NIX_GUIDELINES.md"}
workflows/mentci.aski-fs.deps.edn:19:  {:when {:touch "docs/guides/RestartContext.md"}
scripts/reference_guard/main.clj:64:                  (str path-lc ": forbidden reference to docs/guides/RestartContext.md"))
docs/architecture/ToolStack.md:16:- **Automation:** See `docs/architecture/VersionControlProtocol.md` for the operational workflow.
docs/architecture/JjAutomation.md:3:This document is superseded by `docs/architecture/VersionControlProtocol.md`, which is the primary reference for JJ workflows.
docs/architecture/ReleaseProtocol.md:47:- `docs/guides/RestartContext.md`
docs/architecture/ReleaseProtocol.md:68:- Version Control: `docs/architecture/VersionControlProtocol.md`
docs/architecture/JailCommitProtocol.md:5:Operational steps live in `docs/architecture/VersionControlProtocol.md`.
Research/Guidelines-Wisdom-Reincorporation/591912044701_answer_guidelines-wisdom-reincorporation.md:15:Compared current Rust guideline against Sources/criomos original, traced history, and confirmed the reduction was introduced intentionally at initial creation of docs/architecture/SEMA_RUST_GUIDELINES.md (not a later accidental truncation).
```

### Lowercase Core Path Reintroduction
Patterns:
- `core/`
- `core/programs/`

Matches:
```
development/Top-Level-FS-Spec/Strategy.md:10:- Canonical authority root moved from `core/` to `Core/`.
development/Top-Level-FS-Spec/Strategy.md:11:- Program modules moved from `core/programs/` to `Library/`.
development/Top-Level-FS-Spec/Strategy.md:16:2. Add explicit guard checks for stale lowercase root references (`core/`, `core/programs/`).
development/Core-Library-Symbiotic-Move/Strategy.md:21:- `core/programs/*` -> `Library/*`
development/Core-Library-Symbiotic-Move/Strategy.md:22:- `core/IntentDiscovery.md` -> `Library/IntentDiscovery.md`
development/Core-Library-Symbiotic-Move/Strategy.md:23:- `core/*` mandate files -> `Core/*`
development/Core-Library-Symbiotic-Move/Strategy.md:37:- no active `core/` or `core/programs/` references remain
development/Core-Library-Symbiotic-Move/MOVES.md:4:- `core/` -> `Core/`
development/Core-Library-Symbiotic-Move/MOVES.md:5:- `core/programs/` -> `Library/`
development/Core-Library-Symbiotic-Move/MOVES.md:8:- `core/IntentDiscovery.md` -> `Library/IntentDiscovery.md`
```

## Interpretation
- Any non-empty stale-authority section is migration debt and should be resolved before further architectural moves.
- Lowercase Core/Library path matches in active docs/scripts indicate drift and should be blocked by guard rules.
