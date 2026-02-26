# Research Artifact: ChatGPT Export Knowledge Lineage Mining (Sajban, Sema, Aski, Lojix, Criome)

- **Solar:** 5919.12.8.49.25
- **Subject:** `Aski-Sema-Lojix-Assimilation`
- **Title:** `chatgpt-export-knowledge-lineage-mining`
- **Status:** `canonical-intent`
- **Weight:** `High` (Direct ChatGPT export mining; higher weight to most recent updates)

## 1. Intent
To extract and synthesize the evolution of architectural and philosophical intents from a recent ChatGPT export (`c0aca3950964b7f0c43099034cd9027db8181928c360abca45d56d8d2664626f-2026-02-07...`). The focus is on the lineage and technical specifications of `Sajban`, `Sema`, `Aski`, `Lojix`, and `Criome`.

## 2. Lineage and Evolution (Highest Intent Weight on Recent Formulations)

The concepts form a continuous evolution from philosophy (Sajban) to architecture (Sema) to implementation (Aski/Lojix/Criome).

### 2.1 Sajban -> Sema (The Semantic Substrate)
*   **Original Vision (Sajban):** The "Language of Knowledge". A graph-native semantic substrate designed to replace traditional programming expressions by representing inferred meaning (psyche-to-graph) rather than articulated output. It treats natural language as a lossy interface over a hierarchical, time-aware semantic graph.
*   **Evolution to Sema:** The principles of Sajban crystallized into **Sema Object Style**.
    *   **Core Rule:** *Single Object In, Single Object Out.* All values crossing boundaries are Sema objects; primitive types are internal only.
    *   **Schema Is Sema; Encoding Is Incidental:** Transmissible objects are defined in Sema schemas. Cap'n Proto is explicitly recognized as a *temporary wire representation* (implementation detail), not the domain API.
    *   **Implementation:** Zero free functions. All logic lives on objects. Code speaks Sema.

### 2.2 Aski (The Human-Readable Surface)
*   **Role:** The textual, human-readable authoring surface for Sajban/Sema.
*   **Syntax & Rules:**
    *   A strict superset of EDN (keeping `[]`, `{}`, `()` for values).
    *   Introduces specific schema-declaration grammar: `<>` reserved for single-inner newtypes/variants, `{}` for structs, `()` for enums.
    *   Drops namespace prefixes (keys are local to enclosing type).
    *   Excludes Rust constructs outside Serde's data model (no lifetimes, multi-field tuple-structs, etc.).
*   **Architecture Flow:** Aski (Authoring) -> Sajban-CR (Canonical binary representation / cu-bincode Rust type) -> Generated Rust Types.

### 2.3 Lojix (The Implementation Abstraction)
*   **Role:** "A better abstraction of Nix". Making Nix flake usage sane.
*   **First Goal:** Lockfile (`nix.lock`) normalization. Detects version divergence for the same upstream repository and forces convergence to a single chosen revision under a deterministic policy.
*   **Architecture:**
    *   Written in Rust.
    *   Fully asynchronous actor system.
    *   Inputs use Cap'n Proto interface (Sema objects).
    *   Strict crate layout: `types` (pure domain objects), `wire` (Cap'n Proto edge adapters), `store` (cache db), `bin/lojix` (orchestration).
    *   Integrates crates like `gix` (over libgit2) and `nix-flake-metadata`.

### 2.4 Criome / CriomOS (The Topology)
*   **Role:** The overarching OS and network architecture ("The Criome").
*   **CrioSphereState:** The "last known frozen state of an infinitely changing true CrioSphere." It represents the network aspect of The Criome.
*   **Three-Layer State Model:**
    1.  `CrioSphereProposal` (Builder's state horizon).
    2.  `CrioSphereState` (Frozen canonical snapshot computed in Rust, applying rules/defaults/trust/validation).
    3.  `NodeHorizon` (State rearranged for Nix evaluation, keeping Nix out of the logic path).

## 3. High-Value Architectural Directives Extracted

1.  **Strict Logic-Data Separation:** The domain code only speaks typed objects. Externalization of defaults and parameters into structured data sidecars is mandatory.
2.  **Zero-Deserialize Messaging:** The underlying protocol vision (Sajban v0.1) explicitly targeted `rkyv` (and later Cap'n Proto/cu-bincode) for zero-copy, native-speed traversal within same-endianness domains.
3.  **Trait-Domain First:** Any behavior fitting an existing trait must be implemented as that trait, not a new method.
4.  **No Naked Strings:** Boundary values ingest structured, typed objects, not raw text tokens. Eliminates the "string -> parse" pattern in favor of "bytes -> schema-defined object."

## 4. Synthesis for Mentci
Mentci's current and future capabilities must respect this hierarchy:
*   Use **Cap'n Proto** for the wire, but name and treat the domains as **Sema objects**.
*   Use **Aski/EDN** when humans or agents need to read or author these schemas/states.
*   Design all agent logic as **Rust Actors** taking *Single Sema Object In, Single Sema Object Out*.
*   Respect **Lojix** as the overarching Nix abstraction layer for workspace determinism.
