# Fresh Affected Parts Snapshot

- Snapshot Change ID: `ltkttsyqmozk`
- Parent Change ID: `xzlswptlmxkn`

## Freshness Contract
This snapshot is valid only if the target branch still contains change `ltkttsyqmozk` at head.

Verification command for next prompt:
```bash
current=$(jj log -r @ --no-graph -T 'change_id.short()')
echo "current_change=${current} expected=ltkttsyqmozk"
```

If the change differs, rerun this inventory before implementation.

## Affected Paths (From `@-..@` diff)

affected_path_count: 60

affected_paths:
- `AGENTS.md`
- `Logs/ARTIFACT_SWEEP_REPORT.md`
- `Logs/OBSOLESCENCE_STRIKES.edn`
- `Logs/RECOMMIT_BREAK_AUDIT.md`
- `Logs/RELEASE_MILESTONES.md`
- `Logs/RESEARCH_ANALYSIS.md`
- `README.md`
- `Reports/Core-Library-Symbiotic-Move/README.md`
- `Reports/Docs-Top-Level-Obsolescence-Audit/5919_12_04_53_21_answer_docs-top-level-obsolescence-audit.md`
- `Reports/Docs-Top-Level-Obsolescence-Audit/README.md`
- `Reports/Guidelines-Wisdom-Reincorporation/5919_12_04_47_01_answer_guidelines-wisdom-reincorporation.md`
- `Reports/History-Inputs-Purge/5919_12_04_48_12_answer_inputs-history-purge-strategy.md`
- `Reports/Recover-Lost-Work-After-Rewrite/5919_12_04_48_49_answer_recover-lost-work-after-rewrite.md`
- `Reports/Workspace-Ancient-Copy-Root-Cause/5919_12_04_53_20_answer_workspace-ancient-copy-root-cause.md`
- `Reports/Workspace-Ancient-Copy-Root-Cause/README.md`
- `scripts/program_version/main.clj`
- `scripts/reference_guard/main.clj`
- `strategies/Agent-Authority-Alignment/INTEGRATION.md`
- `strategies/Agent-Authority-Alignment/STRATEGY.md`
- `strategies/Artifact-Sweep/ARTIFACT_ANALYSIS.md`
- `strategies/Artifact-Sweep/PHASE_2_RECOMMIT_RECOVERY.md`
- `strategies/Artifact-Sweep/STRATEGY.md`
- `strategies/Artifact-Sweep/src/sweep.clj`
- `strategies/Commit-Protocol-Merge-Fanin/STRATEGY.md`
- `strategies/Core-Library-Astral-Migration/STRATEGY.md`
- `strategies/Core-Library-Astral-Migration/SURVEY.md`
- `strategies/Core-Library-Symbiotic-Move/FRESH_AFFECTED_PARTS.md`
- `strategies/Core-Library-Symbiotic-Move/FRESH_SCAN_PROTOCOL.md`
- `strategies/Core-Library-Symbiotic-Move/MOVES.md`
- `strategies/Core-Library-Symbiotic-Move/REPORT.md`
- `strategies/Core-Library-Symbiotic-Move/STALE_INTEL_SCAN.md`
- `strategies/Core-Library-Symbiotic-Move/STRATEGY.md`
- `strategies/Core-Root-Migration-Audit/STRATEGY.md`
- `strategies/Core-Root-Migration-Audit/SURVEY.md`
- `strategies/Guidelines-Wisdom-Reincorporation/EXPANSION.md`
- `strategies/Guidelines-Wisdom-Reincorporation/RESEARCH.md`
- `strategies/Guidelines-Wisdom-Reincorporation/STRATEGY.md`
- `strategies/History-Inputs-Purge/STRATEGY.md`
- `strategies/Session-Aggregation-Guard/STRATEGY.md`
- `strategies/Top-Level-FS-Spec/STRATEGY.md`
- `strategies/Universal-Program-Pack/ARCHITECTURE.md`
- `strategies/Universal-Program-Pack/ROADMAP.md`
- `strategies/Universal-Program-Pack/STRATEGY.md`
- `workflows/stt_context_terms.edn`
- `{core => Core}/AGENTS.md`
- `{core => Core}/ARCHITECTURAL_GUIDELINES.md`
- `{core => Core}/ASKI_FS_SPEC.md`
- `{core => Core}/ASKI_POSITIONING.md`
- `{core => Core}/CONTEXTUAL_SESSION_PROTOCOL.md`
- `{core => Core}/HIGH_LEVEL_GOALS.md`
- `{core => Core}/MENTCI_AID.md`
- `{core => Core}/SEMA_CLOJURE_GUIDELINES.md`
- `{core => Core}/SEMA_NIX_GUIDELINES.md`
- `{core => Core}/SEMA_RUST_GUIDELINES.md`
- `{core => Core}/VERSION_CONTROL.md`
- `{core => Library}/INTENT_DISCOVERY.md`
- `{core/programs => Library}/OBSOLESCENCE_PROTOCOL.md`
- `{core/programs => Library}/RESTART_CONTEXT.md`
- `{core/programs => Library}/STRATEGY_DEVELOPMENT.md`
- `{core/programs => Library}/STRATEGY_QUEUE.md`

## Search Index Hints
- Global `Core/` + `Library/` reference hits: 156
- Primary roots touched:
  - `Core/`
  - `Library/`
  - `scripts/`

## Regeneration
Use this command to refresh snapshot data in a future prompt:
```bash
jj diff --summary -r '@-..@'
rg -n 'Core/|Library/' -g'!target' -g'!.git' -g'!.jj'
```
