# Feasibility Study: Mentci-RFS (Read-First Studio)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `core/ASKI_POSITIONING.md`.

## 1. Concept: The "Observer-Commander" Interface
The **Mentci Read-First Studio (Mentci-RFS)** is a proposed specialization of the VS Code foundation. It flips the traditional IDE paradigm: instead of an *editing* environment, it is a **Reading/Navigation** environment where **Edits are delegated to Agentic Flows.**

## 2. Feature: JJ-Boosted Navigation
Standard IDEs struggle with `jj` because they assume a linear Git-like HEAD. Mentci-RFS would treat the **Revision DAG** as the primary file browser.

- **Anonymous Branch Exploration**: Dedicated side-panel for anonymous node navigation.
- **Conflict-as-Truth**: Highlighting conflict markers as symbolic data nodes.
- **Operation Log Time-Travel**: Integrated `jj undo` and `jj op log` visualization.

## 3. Feature: Read-Only Enforcement (The "Pure Gate")
- **Global readOnlyMode**: Utilizing `files.readonlyInclude` to prevent manual edits.
- **The "Intent Trigger"**: selects code blocks and pipes instructions to `mentci-aid`.
- **Shadow Implementation**: Changes appear in anonymous `jj` commits for review.

## 4. Feasibility Analysis
- **Extension Path**: Implementing as a VS Code Extension is the highest feasibility path.
- **Agent Coupling**: Requires SSE integration with the `mentci-aid` daemon.

*The Great Work continues.*
