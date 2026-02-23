# Research Artifact: Author Intent Realignment Strategy

- **Solar:** ♓︎ 5° 54' 59" | 5919 AM
- **Subject:** `Author-Intent-Alignment`
- **Title:** `author-intent-realignment-strategy`
- **Status:** `proposed`

## 1. Intent
Streamline the repository's authority structure to better reflect the author's intent, specifically regarding the "weight" of guidelines and the use of capitalization to denote durability.

## 2. Findings
*   **Current State:** Most documents in `Core/` and `Library/architecture/` are `ALL_CAPS`. According to `Core/ARCHITECTURAL_GUIDELINES.md`, this denotes "Immutable Law" that is never edited.
*   **Friction:** Having nearly all protocols in `ALL_CAPS` creates a high barrier for even minor stylistic or clarity improvements, potentially leading to "authority bloat" where the agent is afraid to refine stable but mutable contracts.
*   **Author's Suggestion:** Reserve `ALL_CAPS` for a very small set of documents requiring explicit human approval. Shift guidelines (like `SEMA_RUST_GUIDELINES.md`) to `PascalCase` to denote "Stable Contract" status—durable and authoritative, but open to refinement by the agent to satisfy the higher laws.

## 3. Proposed Realignment (Casing Shift)
The proposed tiering logic:

| Tier | Casing | Meaning | Change Protocol |
| :--- | :--- | :--- | :--- |
| **Supreme Law** | `ALL_CAPS` | Fundamental mission/safety. | **Human Approval Only.** |
| **Stable Contract** | `PascalCase` | Authoritative guidelines/protocols. | Agent-refinable (Sema style). |
| **Implementation** | `lowercase` | Transient details. | Freely refactorable. |

### 4. Candidate for `ALL_CAPS` (Supreme Law)
*   `ARCHITECTURAL_GUIDELINES.md` (The Meta-Rule)
*   `HIGH_LEVEL_GOALS.md` (The Mission)
*   `AGENTS.md` (The Contract)

### 5. Candidates for `PascalCase` (Stable Contracts)
*   All `SEMA_*_GUIDELINES.md` -> `SemaRustGuidelines.md`, etc.
*   `VERSION_CONTROL.md` -> `VersionControl Protocol.md`
*   `ASKI_POSITIONING.md` -> `AskiPositioning.md`
*   `CHRONOGRAPHY.md` -> `ChronographySpec.md`

## 6. Execution Strategy
1.  Formally define the new tiering in `Core/ARCHITECTURAL_GUIDELINES.md`.
2.  Rename files to match their new durability tier.
3.  Update all cross-references in `Core/AGENTS.md`, `Library/RESTART_CONTEXT.md`, and other core files.
4.  Run `root_guard` and `reference_guard` to ensure integrity.
