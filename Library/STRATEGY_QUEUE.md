# Strategy Order of Importance (The Queue)

**Status:** Operational
**Objective:** Prioritize pre-implementation strategies based on their impact on architectural resiliency and efficiency.

## 1. Strategy-Order-of-Importance Rules
1.  **Resiliency First:** Strategies that harden the core engine, improve the development loop, or ensure data integrity are prioritized.
2.  **Stable Base Mandate:** Daring strategies that significantly alter the user experience or foundation are de-prioritized until a "Level 5 Stable Base" is reached.
3.  **Dependency Alignment:** If Strategy B depends on the output of Strategy A, Strategy A is escalated.

## 2. Tiered Priority Catalog

Priority triage for every current category across both trees (`Development/` and `Research/`) is maintained in:

- `Development/high/Repo-Intent-Realignment/PRIORITY_TRIAGE.md`

Tier definitions:
- `high`: required for current authority, resiliency, and execution correctness.
- `medium`: useful and active, but not blocking core stabilization.
- `low`: parked, folded into canonical owners, or explicitly deferred.

## 3. Active High-Priority Execution Order
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

## 4. Current Execution Context
1. Deterministic section sweep is in progress and currently completed through section `6` (`Development/low`).
2. Remaining sweep sections are `Research/high`, `Research/medium`, `Research/low`, and root files.
3. R&D mirror integrity is mandatory: every active execution subject in `Development/` must keep a synchronized topic index in `Research/`.

*The Great Work continues.*
