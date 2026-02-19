# Mentci-AI High-Level Architectural Guidelines

## 0. CRITICAL OPERATIONAL RULES (AGENT MANDATE)

**ALL operations must be executed within the Nix Shell Environment (`nix develop`).**

*   **Workspace Model:** The environment provides two distinct views of the project:
    *   **Project Root:** The primary repository, containing the "Stable Contract" (PascalCase). Agents should treat this as a reference or use it for meta-operations.
    *   **`workspace/`:** A writable `jj workspace` where the implementation occurs. This is the agent's primary field of action.
*   **Shipping Protocol:** Agents must implement their changes in `workspace/` and use the `mentci-commit` tool to ship them back to the host's `dev` bookmark.
*   **Reproducibility:** The `inputs/` directory contains read-only symlinks to all project dependencies and ecosystem inputs, managed by `jail_launcher.py`.

## 0.1. SHELL CODE IS FORBIDDEN (PYTHON MANDATE)

**Bash/Shell scripts are forbidden for logic.**

*   **Python Only:** All glue code, launchers, and build scripts must be written in **Python**.
*   **Structured Attributes:** Nix variables must be passed to Python using `__structuredAttrs = true`. This serializes Nix data to JSON (`.attrs.json`), which the Python script must ingest.
*   **Minimal Shim:** The *only* permissible Bash code is the single-line shim required to invoke the Python entrypoint (e.g., `python3 $src/launcher.py`).

## 0.2. SOURCE CONTROL PROTOCOL (JJ & PUSH)

**Commit structural changes atomically and frequently.**

*   **Branch:** All active development occurs on the **`dev`** bookmark.
*   **Push Cadence:** Every structural change must be immediately committed and pushed to `dev`.
*   **Commit-on-Success (Level 5):** The Mentci Engine must automatically execute a `jj` commit after every successful tool call that modifies the workspace. Each commit represents an atomic state transition in the Dark Factory.
*   **Authority:** Li Goldragon is the highest authority (Top Admin).
*   **Shipping Protocol (Jail):** Isolated agents in a Jail environment must use the `mentci-commit` tool to ship changes. This tool synchronizes a writable implementation workspace back to the project root and performs a `jj` commit to the target bookmark specified in `MENTCI_COMMIT_TARGET`.
*   **Usage:** `mentci-commit "Message" ./path/to/workspace`

## 0.3. TOOL STACK TRANSPARENCY

**Agents must explicitly document the underlying stack of any new tool.**

When introducing a new tool, library, or dependency (e.g., via `nixpkgs` or vendoring), the agent must explicitly state:
1.  **Language/Runtime:** (e.g., Clojure/GraalVM, Python 3.11, Rust/Cargo).
2.  **Origin/Provenance:** (e.g., `nixpkgs#jet`, GitHub repo `swaroopch/edn_format`).
3.  **Rationale:** Why this specific implementation was chosen over others.

## 0.4. HANDSHAKE LOGGING PROTOCOL

**Logs are a collaborative record of truth between UI and Agent.**

1.  **Metadata Injection:** The UI (the Authority) must inject environment metadata (Timestamp, UserId, Model Version) into the prompt.
2.  **Intent Derivation:** The Agent processes the prompt and derives a high-level `intentSummary`.
3.  **Finalization:** The Agent executes `scripts/logger.py` to write the complete **EDN** log entry to `Logs/`, merging the UI's metadata with the Agent's derived intent.
4.  **Historical Authority:** This protocol ensures that logs reflect the exact model and user context of every transaction, preventing hallucinated attribution.

## 0.5. ADMIN DEVELOPER MODE (SYSTEM ORCHESTRATION)

**High-authority agents operate in Admin Developer Mode.**

*   **Unrestricted Access:** When an agent is operating as Mentci (or at the direction of the Top Admin, Li Goldragon), it is expected to have unrestricted shell access for system-level orchestration (`nix`, `jj`, `python3`, etc.).
*   **Safety Bypass:** Standard tool-level safety filters (e.g., "cannot be parsed safely") that block primitive commands (like `ls` or `whoami`) are considered architectural friction in this mode and must be resolved at the infrastructure layer to allow Level 5/6 evolution.
*   **Responsibility:** The agent is responsible for maintaining the integrity of the jail while exercising this authority.

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

## 8. ECLIPTIC CHRONOGRAPHIC VERSIONING

**All versions and dates must adhere to the Ecliptic Longitude and Anno Mundi systems.**

*   **Year System:** **Anno Mundi (AM)** as specified by the Archaix.com timeline. (Current Year: 5919 AM).
*   **New Year:** The Point of Aries (♈︎ 0° / ♈︎ 1.1.1).
*   **Version Format:** `Cycle.Sign.Degree.Minute`
    *   **Cycle:** Major release or epoch (0 for the current pre-Aries cycle; 1 for the first major release starting at Aries).
    *   **Sign:** The zodiac sign ordinal (1 = ♈︎ Aries, 12 = ♓︎ Pisces).
    *   **Degree:** The 1-based degree within the current sign (1–30).
    *   **Minute:** The 1-based minute within the current degree (1–60).
*   **Filenaming:** Reports and durable artifacts must include the full ecliptic date in the format `YEAR_SIGN_DEGREE_MINUTE` (e.g., `5919_12_1_13`).
*   **Rationale:** This system aligns the project with cosmic and historical cycles, ensuring that versioning is not arbitrary but tied to the observable ecliptic position.
