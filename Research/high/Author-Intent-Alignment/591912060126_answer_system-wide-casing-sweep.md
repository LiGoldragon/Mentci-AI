# Research Artifact: System-Wide Authority and Casing Sweep

- **Solar:** ♓︎ 6° 1' 26" | 5919 AM
- **Subject:** `Author-Intent-Alignment`
- **Title:** `system-wide-casing-sweep`
- **Status:** `finalized`

## 1. Intent
Perform a comprehensive sweep of the repository to ensure that the new casing rules (`ALL_CAPS` for Supreme Law, `PascalCase` for Stable Contract) are correctly applied, links are updated, and obsolete concepts are addressed.

## 2. Actions Taken

### 2.1 Casing Enforcement
*   **Supreme Law:** Verified that only the approved set of files remains in `ALL_CAPS`:
    - `Core/ARCHITECTURAL_GUIDELINES.md`
    - `Core/HIGH_LEVEL_GOALS.md`
    - `Core/AGENTS.md`
    - `Core/SEMA_RUST_GUIDELINES.md`
    - `Core/SEMA_CLOJURE_GUIDELINES.md`
    - `Core/SEMA_NIX_GUIDELINES.md`
*   **Stable Contract:** Renamed all other authoritative protocols, specs, and overviews to `PascalCase`.
*   **Standard Subject Files:** Renamed all instances of `STRATEGY.md`, `REPORT.md`, `ROADMAP.md`, etc., to their `PascalCase` equivalents across the entire `Development/` and `Research/` trees.
*   **External Cleanup:** Renamed `Library/external/criomos/` files to `PascalCase` for local consistency.

### 2.2 Link Synchronization
*   Executed a global search and replace to fix broken links to renamed protocol and spec files.
*   Updated internal references in `Core/AGENTS.md`, `Core/ARCHITECTURAL_GUIDELINES.md`, and `Library/RestartContext.md`.
*   Hardened the `reference_guard` script to handle the new casing and Supreme Law locations.

### 2.3 Obsolete Concept Audit
*   **Aski Headers:** Verified that legacy context headers ("--- Context from:") have been removed.
*   **Aski Precursors:** Confirmed that `Sajban` is correctly marked as a predecessor to SEMA.
*   **Inputs/ Alias:** Confirmed that all `Inputs/` references are correctly identified as transitional aliases.

## 3. Findings
The system is now fully aligned with the refined authority model. The clear distinction between immutable Supreme Law and agent-refinable Stable Contracts is visible at the filesystem layer.

## 4. Section Status: Cleaned
All core and library documentation is now synchronized with the high-weight author intent captured in recent transcriptions.
