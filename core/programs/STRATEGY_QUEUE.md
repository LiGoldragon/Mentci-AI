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
| **3** | **Project-Hardening** | `strategies/project-hardening/` | Goal 0 | Structural reorg, schemas, and test coverage. |
| **4** | **Debugging** | `strategies/debugging/` | Goal 0 | Protocol for system-wide failure resolution. |
| **5** | **Aski-Conversion** | `strategies/aski-conversion/` | Goal 0 | Macro-based conversion between sugar and canonical EDN specs. |
| **6** | **Aski-Refinement** | `strategies/aski-refinement/` | Goal 0 | Reducing object count via contextual overloading and delimiter semantics. |
| **7** | **Attractor** | `strategies/attractor/` | Goal 0 & 1 | Stabilizes the agentic workflow standard and assimilation of core components. |
| **8** | **Mentci-RFS** | `strategies/mentci-rfs/` | Goal 0 | Daring IDE paradigm shift. De-prioritized to ensure it is built on a stable `mentci-aid` base. |

## 3. Implementation Status
- **Strategy-Development:** **Implemented.** Tooling: `scripts/tool_discoverer/main.clj`.
- **Artifact-Sweep:** **Hardening.** Added `ARTIFACT_ANALYSIS.md` and prevention strategy.
- **Artifact-Sweep:** **Hardening (Phase 2 queued).** Added recommit-recovery plan in `strategies/artifact-sweep/PHASE_2_RECOMMIT_RECOVERY.md` to remove post-rewrite DAG artifacts and queue drift.
- **Project-Hardening:** **Implemented.** (Structural Phase). Scripts reorganized, schemas defined.
- **Debugging:** **Operational.** Protocol in `strategies/debugging/STRATEGY.md`.
- **Aski-Conversion:** **Drafted.** Strategy in `strategies/aski-conversion/STRATEGY.md`.
- **Aski-Refinement:** **Drafted.** Strategy in `strategies/aski-refinement/STRATEGY.md`.
- **Attractor:** **Implemented.** Validator in `src/attractor_validator.rs`.
- **Mentci-RFS:** Feasibility study complete. Ready for Strategy-Development.

*The Great Work continues.*
