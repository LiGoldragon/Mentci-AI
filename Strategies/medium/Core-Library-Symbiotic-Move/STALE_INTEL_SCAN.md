# Stale Intel Scan (Post-Move)

## Scope
Check for stale authority references after Core/Library migration.

## Scan Anchors
- scan_change_id: `ltkttsyqmozk`
- parent_change_id: `xzlswptlmxkn`

## Results
### Stale Authority Paths
Patterns:
- `docs/architecture/VERSION_CONTROL.md`
- `docs/guides/RESTART_CONTEXT.md`
- `docs/architecture/SEMA_*_GUIDELINES.md`

Matches:
```
tasks/high_level_goals/goal_1_attractor_dot_job_handoff.md:56:- [x] Add a short run summary to `docs/guides/RESTART_CONTEXT.md` (date, commit id, artifact paths).
workflows/mentci.aski-fs.deps.edn:4:          {:path "docs/architecture/VERSION_CONTROL.md" :section "Auto-Commit"}]}
workflows/mentci.aski-fs.deps.edn:6:   :read [{:path "docs/architecture/SEMA_CLOJURE_GUIDELINES.md" :section "Primary Rules"}
workflows/mentci.aski-fs.deps.edn:7:          {:path "docs/architecture/SEMA_RUST_GUIDELINES.md" :section "Primary Rules"}
workflows/mentci.aski-fs.deps.edn:8:          {:path "docs/architecture/SEMA_NIX_GUIDELINES.md" :section "Primary Rules"}]}
workflows/mentci.aski-fs.deps.edn:9:  {:when {:touch "docs/architecture/SEMA_CLOJURE_GUIDELINES.md"}
workflows/mentci.aski-fs.deps.edn:11:  {:when {:touch "docs/architecture/SEMA_RUST_GUIDELINES.md"}
workflows/mentci.aski-fs.deps.edn:13:  {:when {:touch "docs/architecture/SEMA_NIX_GUIDELINES.md"}
workflows/mentci.aski-fs.deps.edn:19:  {:when {:touch "docs/guides/RESTART_CONTEXT.md"}
scripts/reference_guard/main.clj:64:                  (str path-lc ": forbidden reference to docs/guides/RESTART_CONTEXT.md"))
docs/architecture/TOOLS.md:16:- **Automation:** See `docs/architecture/VERSION_CONTROL.md` for the operational workflow.
docs/architecture/JjAutomation.md:3:This document is superseded by `docs/architecture/VERSION_CONTROL.md`, which is the primary reference for JJ workflows.
docs/architecture/RELEASE_PROTOCOL.md:47:- `docs/guides/RESTART_CONTEXT.md`
docs/architecture/RELEASE_PROTOCOL.md:68:- Version Control: `docs/architecture/VERSION_CONTROL.md`
docs/architecture/JAIL_COMMIT_PROTOCOL.md:5:Operational steps live in `docs/architecture/VERSION_CONTROL.md`.
Reports/Guidelines-Wisdom-Reincorporation/591912044701_answer_guidelines-wisdom-reincorporation.md:15:Compared current Rust guideline against Sources/criomos original, traced history, and confirmed the reduction was introduced intentionally at initial creation of docs/architecture/SEMA_RUST_GUIDELINES.md (not a later accidental truncation).
```

### Lowercase Core Path Reintroduction
Patterns:
- `core/`
- `core/programs/`

Matches:
```
strategies/Top-Level-FS-Spec/STRATEGY.md:10:- Canonical authority root moved from `core/` to `Core/`.
strategies/Top-Level-FS-Spec/STRATEGY.md:11:- Program modules moved from `core/programs/` to `Library/`.
strategies/Top-Level-FS-Spec/STRATEGY.md:16:2. Add explicit guard checks for stale lowercase root references (`core/`, `core/programs/`).
strategies/Core-Library-Symbiotic-Move/STRATEGY.md:21:- `core/programs/*` -> `Library/*`
strategies/Core-Library-Symbiotic-Move/STRATEGY.md:22:- `core/INTENT_DISCOVERY.md` -> `Library/INTENT_DISCOVERY.md`
strategies/Core-Library-Symbiotic-Move/STRATEGY.md:23:- `core/*` mandate files -> `Core/*`
strategies/Core-Library-Symbiotic-Move/STRATEGY.md:37:- no active `core/` or `core/programs/` references remain
strategies/Core-Library-Symbiotic-Move/MOVES.md:4:- `core/` -> `Core/`
strategies/Core-Library-Symbiotic-Move/MOVES.md:5:- `core/programs/` -> `Library/`
strategies/Core-Library-Symbiotic-Move/MOVES.md:8:- `core/INTENT_DISCOVERY.md` -> `Library/INTENT_DISCOVERY.md`
```

## Interpretation
- Any non-empty stale-authority section is migration debt and should be resolved before further architectural moves.
- Lowercase Core/Library path matches in active docs/scripts indicate drift and should be blocked by guard rules.
