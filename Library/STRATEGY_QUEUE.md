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
| **1** | **Strategy-Development** | `strategies/Strategy-Development/` | Goal 0 | Hardens the process of finding and testing tools. High efficiency gain. |
| **2** | **Artifact-Sweep** | `strategies/Artifact-Sweep/` | Goal 0 | Surgically removes redundant framing blocks to improve token economy and purity. |
| **3** | **History-Inputs-Purge** | `strategies/History-Inputs-Purge/` | Goal 0 | Removes `Inputs/` and `inputs_backup/` from full VCS history and installs reintroduction guards. |
| **4** | **Project-Hardening** | `strategies/Project-Hardening/` | Goal 0 | Structural reorg, schemas, and test coverage. |
| **5** | **Agent-Authority-Alignment** | `strategies/Agent-Authority-Alignment/` | Goal 0 | Enforces canonical authority/path contracts and prevents reference drift. |
| **6** | **Debugging** | `strategies/Debugging/` | Goal 0 | Protocol for system-wide failure resolution. |
| **7** | **Aski-Conversion** | `strategies/Aski-Conversion/` | Goal 0 | Macro-based conversion between sugar and canonical EDN specs. |
| **8** | **Aski-Refinement** | `strategies/Aski-Refinement/` | Goal 0 | Reducing object count via contextual overloading and delimiter semantics. |
| **9** | **Session-Aggregation-Guard** | `strategies/Session-Aggregation-Guard/` | Goal 0 | Enforces final session synthesis and prevents prompt-attribution gaps. |
| **10** | **Malli-Verbosity-Reduction** | `strategies/Malli-Verbosity-Reduction/` | Goal 0 | Reduces schema/signature boilerplate while preserving instrumentation contracts. |
| **11** | **Commit-Protocol-Merge-Fanin** | `strategies/Commit-Protocol-Merge-Fanin/` | Goal 0 | Repairs session synthesis topology to require true final merge fan-in over logical intents. |
| **12** | **Guidelines-Wisdom-Reincorporation** | `strategies/Guidelines-Wisdom-Reincorporation/` | Goal 0 | Restores human-reviewed guideline depth and harmonizes cross-language semantics. |
| **13** | **STT-Context-Decoding** | `strategies/STT-Context-Decoding/` | Goal 0 | Improves voice-prompt reliability via repository-context lexical bias and confidence-gated transcription. |
| **14** | **Attractor** | `strategies/Attractor/` | Goal 0 & 1 | Stabilizes workflow standard and assimilation of core components. |
| **15** | **Universal-Program-Pack** | `strategies/Universal-Program-Pack/` | Goal 0 | Defines portable path contracts and reusable program-pack structure. |
| **16** | **Mentci-RFS** | `strategies/Mentci-RFS/` | Goal 0 | Daring IDE paradigm shift. De-prioritized to ensure it is built on a stable `mentci-aid` base. |

## 3. Implementation Status
- **Strategy-Development:** **Implemented.** Tooling: `scripts/tool_discoverer/main.clj`.
- **Artifact-Sweep:** **Hardening.** Added `ARTIFACT_ANALYSIS.md` and prevention strategy.
- **Artifact-Sweep:** **Hardening (Phase 2 queued).** Added recommit-recovery plan in `strategies/Artifact-Sweep/PHASE_2_RECOMMIT_RECOVERY.md` to remove post-rewrite DAG artifacts and queue drift.
- **History-Inputs-Purge:** **Drafted + Implemented.** Strategy and runbook prepared; reachable Git history purge completed and verified.
- **Project-Hardening:** **Implemented.** (Structural Phase). Scripts reorganized, schemas defined.
- **Agent-Authority-Alignment:** **In Progress.** Canonical authority/path normalization and integration matrix added.
- **Debugging:** **Operational.** Protocol in `strategies/Debugging/STRATEGY.md`.
- **Aski-Conversion:** **Drafted.** Strategy in `strategies/Aski-Conversion/STRATEGY.md`.
- **Aski-Refinement:** **Drafted.** Strategy in `strategies/Aski-Refinement/STRATEGY.md`.
- **Session-Aggregation-Guard:** **In Progress.** Session-synthesis gate and guard script added.
- **Malli-Verbosity-Reduction:** **Drafted.** Research and staged macro strategy prepared.
- **Commit-Protocol-Merge-Fanin:** **Drafted.** Merge-topology repair strategy prepared.
- **Guidelines-Wisdom-Reincorporation:** **Drafted.** Source comparison and phased reintegration strategy prepared.
- **STT-Context-Decoding:** **Drafted.** Context-biased voice transcription strategy and maintained lexicon seed prepared.
- **Attractor:** **Operational + Draft Roadmap.** Validator in `src/attractor_validator.rs`; broader standardization remains in `strategies/Attractor/DRAFT.md`.
- **Universal-Program-Pack:** **Drafted.** Mission, architecture, roadmap, and strategy documents prepared.
- **Mentci-RFS:** Feasibility study complete. Ready for Strategy-Development.

*The Great Work continues.*
