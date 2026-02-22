# Strategy Order of Importance (The Queue)

**Status:** Operational
**Objective:** Prioritize pre-implementation strategies based on their impact on architectural resiliency and efficiency.

## 1. Strategy-Order-of-Importance Rules
1.  **Resiliency First:** Strategies that harden the core engine, improve the development loop, or ensure data integrity are prioritized.
2.  **Stable Base Mandate:** Daring strategies that significantly alter the user experience or foundation are de-prioritized until a "Level 5 Stable Base" is reached.
3.  **Dependency Alignment:** If Strategy B depends on the output of Strategy A, Strategy A is escalated.

## 2. Current Sorted Queue

| Rank | Subject | Strategy Path | Linked Goal | Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **1** | **Strategy-Development** | `strategies/strategy-development/` | Goal 0 | Hardens the process of finding and testing tools. High efficiency gain. |
| **2** | **Artifact-Sweep** | `strategies/artifact-sweep/` | Goal 0 | Surgically removes redundant framing blocks to improve token economy and purity. |
| **3** | **History-Inputs-Purge** | `strategies/history-inputs-purge/` | Goal 0 | Removes `Inputs/` and `inputs_backup/` from full VCS history and installs reintroduction guards. |
| **4** | **Project-Hardening** | `strategies/project-hardening/` | Goal 0 | Structural reorg, schemas, and test coverage. |
| **5** | **Agent-Authority-Alignment** | `strategies/agent-authority-alignment/` | Goal 0 | Enforces canonical authority/path contracts and prevents reference drift. |
| **6** | **Debugging** | `strategies/debugging/` | Goal 0 | Protocol for system-wide failure resolution. |
| **7** | **Aski-Conversion** | `strategies/aski-conversion/` | Goal 0 | Macro-based conversion between sugar and canonical EDN specs. |
| **8** | **Aski-Refinement** | `strategies/aski-refinement/` | Goal 0 | Reducing object count via contextual overloading and delimiter semantics. |
| **9** | **Session-Aggregation-Guard** | `strategies/session-aggregation-guard/` | Goal 0 | Enforces final session synthesis and prevents prompt-attribution gaps. |
| **10** | **Malli-Verbosity-Reduction** | `strategies/malli-verbosity-reduction/` | Goal 0 | Reduces schema/signature boilerplate while preserving instrumentation contracts. |
| **11** | **Commit-Protocol-Merge-Fanin** | `strategies/commit-protocol-merge-fanin/` | Goal 0 | Repairs session synthesis topology to require true final merge fan-in over logical intents. |
| **12** | **Guidelines-Wisdom-Reincorporation** | `strategies/guidelines-wisdom-reincorporation/` | Goal 0 | Restores human-reviewed guideline depth and harmonizes cross-language semantics. |
| **13** | **STT-Context-Decoding** | `strategies/stt-context-decoding/` | Goal 0 | Improves voice-prompt reliability via repository-context lexical bias and confidence-gated transcription. |
| **14** | **Attractor** | `strategies/attractor/` | Goal 0 & 1 | Stabilizes workflow standard and assimilation of core components. |
| **15** | **Universal-Program-Pack** | `strategies/universal-program-pack/` | Goal 0 | Defines portable path contracts and reusable program-pack structure. |
| **16** | **Mentci-RFS** | `strategies/mentci-rfs/` | Goal 0 | Daring IDE paradigm shift. De-prioritized to ensure it is built on a stable `mentci-aid` base. |

## 3. Implementation Status
- **Strategy-Development:** **Implemented.** Tooling: `scripts/tool_discoverer/main.clj`.
- **Artifact-Sweep:** **Hardening.** Added `ARTIFACT_ANALYSIS.md` and prevention strategy.
- **Artifact-Sweep:** **Hardening (Phase 2 queued).** Added recommit-recovery plan in `strategies/artifact-sweep/PHASE_2_RECOMMIT_RECOVERY.md` to remove post-rewrite DAG artifacts and queue drift.
- **History-Inputs-Purge:** **Drafted + Implemented.** Strategy and runbook prepared; reachable Git history purge completed and verified.
- **Project-Hardening:** **Implemented.** (Structural Phase). Scripts reorganized, schemas defined.
- **Agent-Authority-Alignment:** **In Progress.** Canonical authority/path normalization and integration matrix added.
- **Debugging:** **Operational.** Protocol in `strategies/debugging/STRATEGY.md`.
- **Aski-Conversion:** **Drafted.** Strategy in `strategies/aski-conversion/STRATEGY.md`.
- **Aski-Refinement:** **Drafted.** Strategy in `strategies/aski-refinement/STRATEGY.md`.
- **Session-Aggregation-Guard:** **In Progress.** Session-synthesis gate and guard script added.
- **Malli-Verbosity-Reduction:** **Drafted.** Research and staged macro strategy prepared.
- **Commit-Protocol-Merge-Fanin:** **Drafted.** Merge-topology repair strategy prepared.
- **Guidelines-Wisdom-Reincorporation:** **Drafted.** Source comparison and phased reintegration strategy prepared.
- **STT-Context-Decoding:** **Drafted.** Context-biased voice transcription strategy and maintained lexicon seed prepared.
- **Attractor:** **Operational + Draft Roadmap.** Validator in `src/attractor_validator.rs`; broader standardization remains in `strategies/attractor/DRAFT.md`.
- **Universal-Program-Pack:** **Drafted.** Mission, architecture, roadmap, and strategy documents prepared.
- **Mentci-RFS:** Feasibility study complete. Ready for Strategy-Development.

*The Great Work continues.*
