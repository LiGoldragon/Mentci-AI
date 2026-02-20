# Mentci-AI High-Level Architectural Guidelines

*   **Reproducibility:** The `inputs/` directory contains read-only symlinks to all project dependencies and ecosystem inputs, managed by `scripts/launcher.clj`.
*   **Purity:** Respect the `RO Indicator`. In pure mode, inputs are Read-Only. In impure mode (dev), local changes may be possible but must be committed to git to be visible to the pure flake.

## 0.1. SHELL CODE IS FORBIDDEN (CLOJURE MANDATE)

**Bash/Shell scripts are forbidden for logic.**

*   **Clojure (Babashka) Only:** All glue code, launchers, and build scripts must be written in **Clojure**, executed via the **Babashka** runtime for fast startup.
*   **Structured Attributes:** Nix variables must be passed to Clojure using `__structuredAttrs = true`. The script must ingest the resulting JSON (`.attrs.json`).
*   **Minimal Shim:** The *only* permissible Bash code is the single-line shim required to invoke the Clojure entrypoint (e.g., `bb $src/launcher.clj`).

## 0.2. SOURCE CONTROL PROTOCOL (JJ & PUSH)

**Commit structural changes atomically and frequently.**

*   **Branch:** All active development occurs on the **`dev`** bookmark.
*   **Push Cadence:** Every structural change must be immediately committed and pushed to `dev`.
*   **Commit-Every-Intent (Level 5):** The Mentci Engine must automatically execute a `jj` commit after **every single atomic modification** (e.g., one file write, one replacement). Each commit represents exactly one intention. Combining multiple unrelated modifications into a single "success" commit is forbidden.
*   **Authority:** Li Goldragon is the highest authority (Top Admin).
*   **Shipping Protocol (Jail):** Isolated agents in a Jail environment must use the `mentci-commit` tool to ship changes. This tool synchronizes a writable implementation workspace back to the project root and performs a `jj` commit to the target bookmark specified in `MENTCI_COMMIT_TARGET`.
*   **Usage:** `mentci-commit "intent: <message>"`
*   **Operational Steps:** See `docs/architecture/VERSION_CONTROL.md`.

## 0.3. TOOL STACK TRANSPARENCY

**Agents must explicitly document the underlying stack of any new tool.**

When introducing a new tool, library, or dependency (e.g., via `nixpkgs` or vendoring), the agent must explicitly state:
1.  **Language/Runtime:** (e.g., Clojure/Babashka, Rust/Cargo).
2.  **Origin/Provenance:** (e.g., `nixpkgs#jet`, GitHub repo `babashka/babashka`).
3.  **Rationale:** Why this specific implementation was chosen over others.
4.  **Execution Context:** Use `nix develop` for non-standard tools, libraries, or dependencies.

## 0.4. AUDIT TRAIL

**The authoritative audit trail is the Jujutsu history.**

Use `jj log` for provenance and review.

## 0.5. ADMIN DEVELOPER MODE (SYSTEM ORCHESTRATION)

**High-authority agents operate in Admin Developer Mode.**

*   **Unrestricted Access:** When an agent is operating as Mentci (or at the direction of the Top Admin, Li Goldragon), it is expected to have unrestricted shell access for system-level orchestration (`nix`, `jj`, `bb`, etc.).
*   **Safety Bypass:** Standard tool-level safety filters (e.g., "cannot be parsed safely") that block primitive commands (like `ls` or `whoami`) are considered architectural friction in this mode and must be resolved at the infrastructure layer to allow Level 5/6 evolution.
*   **Responsibility:** The agent is responsible for maintaining the integrity of the jail while exercising this authority.

This document synthesizes the architectural, naming, and durability rules inherited from the **CriomOS** and **Sema** lineage. These rules are structural and non-negotiable. Violations indicate category errors, not stylistic choices.

## 0.5.1. PER-LANGUAGE SEMA GUIDELINES

*   **Clojure:** `docs/architecture/SEMA_CLOJURE_GUIDELINES.md`
*   **Rust:** `docs/architecture/SEMA_RUST_GUIDELINES.md`
*   **Nix:** `docs/architecture/SEMA_NIX_GUIDELINES.md`

## 0.6. NIX STORE ACCESS

**Do not scan `/nix/store` directly.**

*   **Efficiency:** The store is large and filesystem searches are wasteful.
*   **Protocol:** Query store metadata through the Nix CLI (daemon-backed) instead of filesystem traversal.

## 0.7. JJ LOGGING WITHOUT SIGNING

**When reading history, prefer `jj log` (and similar) with signing disabled.**

*   **Reliability:** Disable signing for read-only history inspection to avoid GPG/agent failures.
*   **Scope:** This applies to log/describe/show commands that do not modify history.

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
*   **Chronographic Style:** All dates must prioritize the **Ecliptic Chronographic format** (e.g., `♓︎ 1° 15' 1" | 5919 AM`). Gregorian dates should be included in parentheses or otherwise de-emphasized (e.g., `(Feb 19, 2026)`). 
    *   **Ordinality:** Seconds and minutes are 1-based ordinals (0 on ephemeris = 1st ordinal).
    *   **Adaptive Precision:** High-precision units (seconds, minutes) may be omitted if unknown or if the context only requires lower granularity (e.g., `♓︎ 1° | 5919 AM` for broad milestones). Truncation must always occur from right-to-left.
*   **Comments:** Mandatory *only* when the "why" cannot be structural (e.g., specific timeouts, protocol quirks). Boilerplate is never documented.

## 6. Ontology Resides in Data

**Type is an intrinsic property, not a procedural side effect.**
The classification of an entity must be encoded within its data structure. Do not hardcode taxonomy into function calls or script logic. Define the universe of types in a schema, manifest, or data structure, and write generic logic that iterates over that truth.

*   **Forbidden:** `process_item(item, "TYPE_A")`
*   **Required:** `manifest = { TYPE_A = [items...]; }; map(process, manifest)`

## 8. ECLIPTIC CHRONOGRAPHIC VERSIONING

**All versions and dates must adhere to the True Solar Ordinal-Zodiac system.**

See **`CHRONOGRAPHY.md`** for the complete technical specification of the 1-based ordinal system.

*   **Year System:** Anno Mundi (AM). (Current Year: 5919 AM).
*   **Version Format:** `Cycle.Sign.Degree.Minute.Second`
    *   **Sign:** 1 (Aries) to 12 (Pisces).
    *   **Degree:** 1 to 30.
    *   **Minute:** 1 to 60.
    *   **Second:** 1 to 60.
*   **Rationale:** Aligning project state with true solar coordinates.
