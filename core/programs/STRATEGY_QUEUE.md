# Strategy Order of Importance (The Queue)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `core/ASKI_POSITIONING.md`.

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
| **2** | **Attractor** | `strategies/attractor/` | Goal 0 & 1 | Stabilizes the agentic workflow standard and assimilation of core components. |
| **3** | **Mentci-RFS** | `strategies/mentci-rfs/` | Goal 0 | Daring IDE paradigm shift. De-prioritized to ensure it is built on a stable `mentci-aid` base. |

## 3. Implementation Status
- **Strategy-Development:** **Implemented.** Tooling: `scripts/tool_discoverer.clj`.
- **Attractor:** **Implemented.** Validator in `src/attractor_validator.rs`.
- **Mentci-RFS:** Feasibility study complete. Ready for Strategy-Development.

*The Great Work continues.*
