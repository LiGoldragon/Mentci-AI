# Mentci-AI High-Level Architectural Guidelines

## 0. CRITICAL OPERATIONAL RULES (AGENT MANDATE)

**ALL operations must be executed within the Nix Jail Launcher environment.**

*   **Launch Command:** `nix develop` (activates the `jail.nix` shell hook).
*   **Purpose:** This environment places all project inputs into scope as **pure Nix derivations**.
*   **Organization:** Inputs are strictly organized into directories corresponding to their **Input Type** (e.g., `Flake`, `AtomV0`, `Git`, `TypedAtom`) as defined in the Cap'n Proto schema.
*   **Purity:** Respect the `RO Indicator`. In pure mode, inputs are Read-Only. In impure mode (dev), local changes may be possible but must be committed to git to be visible to the pure flake.

## 0.1. SHELL CODE IS FORBIDDEN (PYTHON MANDATE)

**Bash/Shell scripts are forbidden for logic.**

*   **Python Only:** All glue code, launchers, and build scripts must be written in **Python**.
*   **Structured Attributes:** Nix variables must be passed to Python using `__structuredAttrs = true`. This serializes Nix data to JSON (`.attrs.json`), which the Python script must ingest.
*   **Minimal Shim:** The *only* permissible Bash code is the single-line shim required to invoke the Python entrypoint (e.g., `python3 $src/launcher.py`).
*   **Reasoning:** Python provides superior typing, error handling, and data structure manipulation compared to shell scripting, aligning with the Level 5 goal of robust, architected systems.

## 0.2. SOURCE CONTROL PROTOCOL (JJ & PUSH)

**Commit structural changes atomically and frequently.**

*   **Branch:** All active development occurs on the **`dev`** bookmark (short for `develop`).
*   **Push Cadence:** Every structural change (defined by the most concise commit message possible) must be immediately committed and pushed to `dev`.
*   **Authority:** Li Goldragon is the highest authority (Top Admin). All intent moving forward must be documented at the corresponding level of authority.
*   **Command:** `jj describe -m "feat: <concise message>"` followed by `jj git push --bookmark dev`.

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

## 6. Ontology Resides in Data

**Type is an intrinsic property, not a procedural side effect.**
The classification of an entity must be encoded within its data structure. Do not hardcode taxonomy into function calls or script logic. Define the universe of types in a schema, manifest, or data structure, and write generic logic that iterates over that truth.

*   **Forbidden:** `process_item(item, "TYPE_A")`
*   **Required:** `manifest = { TYPE_A = [items...]; }; map(process, manifest)`