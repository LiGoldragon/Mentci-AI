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

Historical note: entries below are recorded verbatim from the migration-era diff and may include legacy lowercase path segments (for example `development/...`) and pre-tier `Research/...` locations.

affected_path_count: 60

affected_paths:
- `AGENTS.md`
- `Logs/ARTIFACT_SWEEP_Report.md`
- `Logs/ObsolescenceStrikes.edn`
- `Logs/RECOMMIT_BREAK_AUDIT.md`
- `Logs/ReleaseMilestones.md`
- `Logs/RESEARCH_ANALYSIS.md`
- `README.md`
- `Research/Core-Library-Symbiotic-Move/index.edn`
- `Research/Top-Level-FS-Spec/591912045321_answer_docs-top-level-obsolescence-audit.md`
- `Research/Top-Level-FS-Spec/index.edn`
- `Research/Guidelines-Wisdom-Reincorporation/591912044701_answer_guidelines-wisdom-reincorporation.md`
- `Research/Workspace-Pwd-History-Purge-And-Jj-Config-Migration/591912044812_answer_inputs-history-purge-strategy.md`
- `Research/Recover-Lost-Work-After-Rewrite/591912044849_answer_recover-lost-work-after-rewrite.md`
- `Research/Workspace-Pwd-History-Purge-And-Jj-Config-Migration/591912045320_answer_workspace-ancient-copy-root-cause.md`
- `Research/Workspace-Pwd-History-Purge-And-Jj-Config-Migration/index.edn`
- `scripts/program_version/main.clj`
- `scripts/reference_guard/main.clj`
- `development/Agent-Authority-Alignment/INTEGRATION.md`
- `development/Agent-Authority-Alignment/Strategy.md`
- `development/Artifact-Sweep/ARTIFACT_ANALYSIS.md`
- `development/Artifact-Sweep/PHASE_2_RECOMMIT_RECOVERY.md`
- `development/Artifact-Sweep/Strategy.md`
- `development/Artifact-Sweep/src/sweep.clj`
- `development/Commit-Protocol-Merge-Fanin/Strategy.md`
- `development/Top-Level-FS-Spec/Strategy.md`
- `development/Top-Level-FS-Spec/SURVEY.md`
- `development/Core-Library-Symbiotic-Move/FRESH_AFFECTED_PARTS.md`
- `development/Core-Library-Symbiotic-Move/FRESH_SCAN_PROTOCOL.md`
- `development/Core-Library-Symbiotic-Move/MOVES.md`
- `development/Core-Library-Symbiotic-Move/Report.md`
- `development/Core-Library-Symbiotic-Move/STALE_INTEL_SCAN.md`
- `development/Core-Library-Symbiotic-Move/Strategy.md`
- `development/Top-Level-FS-Spec/Strategy.md`
- `development/Top-Level-FS-Spec/SURVEY.md`
- `development/Guidelines-Wisdom-Reincorporation/EXPANSION.md`
- `development/Guidelines-Wisdom-Reincorporation/RESEARCH.md`
- `development/Guidelines-Wisdom-Reincorporation/Strategy.md`
- `development/Workspace-Pwd-History-Purge-And-Jj-Config-Migration/Strategy.md`
- `development/Commit-Protocol-Merge-Fanin/Strategy.md`
- `development/Top-Level-FS-Spec/Strategy.md`
- `development/Universal-Program-Pack/ArchitectureMap.md`
- `development/Universal-Program-Pack/Roadmap.md`
- `development/Universal-Program-Pack/Strategy.md`
- `workflows/stt_context_terms.edn`
- `{core => Core}/AGENTS.md`
- `{core => Core}/ARCHITECTURAL_GUIDELINES.md`
- `{core => Core}/AskiFsSpec.md`
- `{core => Core}/AskiPositioning.md`
- `{core => Core}/ContextualSessionProtocol.md`
- `{core => Core}/HIGH_LEVEL_GOALS.md`
- `{core => Core}/MentciAidProtocol.md`
- `{core => Core}/SEMA_CLOJURE_GUIDELINES.md`
- `{core => Core}/SEMA_NIX_GUIDELINES.md`
- `{core => Core}/SEMA_RUST_GUIDELINES.md`
- `{core => Core}/VersionControlProtocol.md`
- `{core => Library}/IntentDiscovery.md`
- `{core/programs => Library}/ObsolescenceProtocol.md`
- `{core/programs => Library}/RestartContext.md`
- `{core/programs => Library}/StrategyDevelopment.md`
- `{core/programs => Library}/StrategyQueue.md`

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
