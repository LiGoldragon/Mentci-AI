# Mentci-AI High-Level Architectural Guidelines

This document synthesizes the architectural, naming, and durability rules inherited from the **CriomOS** and **Sema** lineage. These rules are structural and non-negotiable. Violations indicate category errors, not stylistic choices.

## 1. Core Philosophy: The Semantic Layer

**Names are not descriptions; they are commitments.**
Meaning is distributed across repository names, directory paths, module names, and type definitions. Meaning must appear exactly once at the highest valid layer. Repetition across layers (e.g., `UserObject`, `Manager`) is forbidden.

## 2. Capitalization-Based Durability Rules (Filesystem)

**Capitalization in paths and filenames encodes durability**—the resistance of instructions to modification. This is orthogonal to code style.

| Tier | Casing | Meaning | Rules |
| :--- | :--- | :--- | :--- |
| **Immutable Law** | `ALL_CAPS` | Non-negotiable constraints. | Never edited. Never contradicted. Loaded first. |
| **Stable Contract** | `PascalCase` | Durable structure/intent. | Edited only by mandate. Extension permitted if meaning is preserved. |
| **Mutable Implementation** | `lowercase` | Operational detail. | Freely editable/refactorable to satisfy higher tiers. |

*   **Conflict Resolution:** `ALL_CAPS` > `PascalCase` > `lowercase`.
*   **Composition:** A single `ALL_CAPS` segment makes the entire path immutable.

## 3. Sema Object Style (Rust)

**Everything is an Object.**
*   **No Free Functions:** All behavior attaches to named types or traits.
*   **Capitalization as Ontology:**
    *   `PascalCase`: Objects (Types, Traits).
    *   `snake_case`: Actions, Relations, Flow.
*   **Trait-Domain Rule:** Any behavior fitting an existing trait (e.g., `FromStr`) must be implemented as that trait, not a new method (e.g., `parse_message`).
*   **Direction Encodes Action:**
    *   `from_*`: Acquisition/Construction.
    *   `to_*`: Emission/Projection.
    *   `into_*`: Consuming transformation.
    *   *Forbidden:* `read`, `write`, `load`, `save` when direction suffices.
*   **Single Object In/Out:** Methods accept at most one explicit object (struct) and return one object. Complex inputs/outputs require their own container structs.
*   **Schema is Sema:** Transmissible objects are defined in Sema schemas. Wire formats (Cap'n Proto) are implementation details, not domain APIs.

## 4. Sema Object Style (Nix)

**Attrsets Exist; Flows Occur.**
*   **Naming:**
    *   `camelCase`: Functions, relations, flow.
    *   `kebab-case`: Static packages, attributes.
*   **Standard Library Rule:** Never reimplement `lib` patterns. Use standard functions for merging/mapping.
*   **Single Attrset In/Out:** Reusable functions accept exactly one argument: an attribute set (named arguments). Positional arguments are forbidden for domain logic.
*   **Grouping:** Related helper functions must be grouped into namespace attrsets (e.g., `lib.message.isValid`), not scattered files.

## 5. Documentation Protocol

**Impersonal, Timeless, Precise.**
*   No first/second person ("I", "you").
*   No humor or evaluation.
*   Behavior is stated as fact.
*   **Self-Documenting Code:** Preferred over comments.
*   **Comments:** Mandatory *only* when the "why" cannot be structural (e.g., specific timeouts, protocol quirks). Boilerplate is never documented.
