# Research Artifact: Minimal High-Authority File Set

- **Solar:** ♓︎ 5° 60' 47" | 5919 AM
- **Subject:** `Author-Intent-Alignment`
- **Title:** `minimal-high-authority-file-set`
- **Status:** `proposed`

## 1. Intent
Identify and define the minimal set of "Supreme Law" (`ALL_CAPS`) files that constitute the repository's immutable core, requiring explicit human approval for any modification.

## 2. Findings
The current repository contains an excessive number of `ALL_CAPS` files (over 40), which dilutes the semantic significance of the casing and creates unnecessary friction for agent-driven refinement of stable protocols.

## 3. Proposed Minimal High-Authority Set (Supreme Law)
The following files are considered the "Constitution" of Mentci-AI. They must be in `ALL_CAPS` and are marked as **Fully Human Review Data**. Agents must not modify these files without explicit direction.

| File | Role |
| :--- | :--- |
| `Core/ARCHITECTURAL_GUIDELINES.md` | The Meta-Rule governing all repository structures and tiers. |
| `Core/HIGH_LEVEL_GOALS.md` | The fundamental mission and durable objectives of the project. |
| `Core/AGENTS.md` | The non-negotiable contract and enforcement rules for AI agents. |
| `Core/SEMA_RUST_GUIDELINES.md` | Structural rules for Rust implementation (Single Owner / Actor Model). |
| `Core/SEMA_CLOJURE_GUIDELINES.md` | Structural rules for Clojure/Babashka logic. |
| `Core/SEMA_NIX_GUIDELINES.md` | Structural rules for Nix infrastructure. |

## 4. Integration of High-Weight Intent
Recent transcriptions from `TheBookOfGoldragon` emphasize that these core structural rules (Sema, Aski, Actor flow) are the "True North" of the project. Elevating these guidelines to Supreme Law ensures that the agent's implementation remains strictly aligned with the author's vision of impeccability and high-efficiency symbolic manipulation.

## 5. Authority Registry
A canonical registry of these tiers is now maintained in `Core/index.edn`.

### 4.1 Core Protocols
*   `ContextualSessionProtocol.md` -> `ContextualSessionProtocol.md`
*   `ExtensionIndexProtocol.md` -> `ExtensionIndexProtocol.md`
*   `MentciAidProtocol.md` -> `MentciAidProtocol.md` (or move to component)

### 4.2 Library Overviews
*   `RestartContext.md` -> `RestartContext.md`
*   `StrategyDevelopment.md` -> `StrategyDevelopment.md`
*   `StrategyQueue.md` -> `StrategyQueue.md`
*   `IntentDiscovery.md` -> `IntentDiscovery.md`
*   `ObsolescenceProtocol.md` -> `ObsolescenceProtocol.md`

### 4.3 Technical Specifications
*   All files in `Library/specs/`, `Library/architecture/`, and `Library/astrology/` should adopt `PascalCase` or `lowercase` as appropriate for their implementation tier.

## 5. Summary
Reducing the "Supreme Law" set to just **3 files** provides maximum clarity on what the agent *must not* touch without direction, while liberating the agent to maintain the health and consistency of the rest of the protocol substrate.
