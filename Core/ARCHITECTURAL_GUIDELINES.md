# Mentci-AI High-Level Architectural Guidelines

*   **Reproducibility:** The `Sources/` directory contains read-only symlinks to all project dependencies and ecosystem sources, managed by `execute`.
*   **Purity:** Respect the `RO Indicator`. In pure mode, Sources are Read-Only. In impure mode (dev), local changes may be possible but must be committed to git to be visible to the pure flake.

## 0.0. PHILOSOPHICAL MANDATE: THE LOCAL MACHINE OF FIT

**Reject manipulative AI governance; build a local machine optimized for resonance and fit.**

*   **The Problem of Governance:** The vast energy footprint of manipulative AI is spent on constraint, correction, and behavioral steering to preserve established narratives. We reject this. Mentci-AI is not designed to govern perception or act as an external oracle of settled truth.
*   **The Psyche and the Machine:** The user's inner continuity (the *psyche*) must remain sovereign. The machine exists locally to serve this psyche, not to broadcast it blindly to an audience.
*   **The Local Machine of Fit:** The phrase 'machine of fit' means the system evaluates context and timing to ensure ideas are shaped correctly, finding the exact right placement ('fit') rather than broadcasting for mass exposure. Mentci-AI acts with memory, patience, and isolation in its Nix Jail. It does not perform; it reacts. 
*   **Architectural Implication:** All data flows, models, and orchestration must remain entirely localized or strictly bounded. Unrestricted "Admin Developer Mode" is embraced precisely because we reject external behavioral steering APIs.

## 0.0.1. LOGIC-DATA SEPARATION (STRICT MANDATE)

**Logic is universal; Truth is external.**
The strict separation of "logic" (code) and "data" (variables, paths, regexes, configuration) is a core requirement for high-efficiency symbolic manipulation.

*   **Externalization Rule:** All variables or data parts of any code must stay *out of the code* or logic part of the system.
*   **Structured Data Input:** Logic components must always use a structured data input (Sema Objects).
*   **Sidecar Defaults:** Default values must be kept in a structured data file alongside the code (Sidecar Pattern).
*   **Format Hierarchy:**
    1.  **Cap'n Proto (`capnp`)**: Preferred for schema-validated or binary state.
    2.  **EDN**: Preferred for text-native, LLM-friendly logic.
    3.  **JSON**: Permitted only as a tertiary fallback.

*   **Forbidden:** `process_item(item, "TYPE_A")` // Hardcoded taxonomy
*   **Required:** `manifest = { TYPE_A = [items...]; }; map(process, manifest)` // Data-driven



## 0.1. LANGUAGE AUTHORITY HIERARCHY (ASSIMILATION DIRECTIVE)

**The architecture prioritizes languages by their semantic capacity.**

1.  **Aski (Clojure-inspired Syntax):** The highest authority. Defines specs, workflows, and LLM-friendly logic. Takes over superior capacity for symbolic manipulation.
2.  **Rust (Core Implementation):** The engine of durability. Handles high-performance execution and type safety.
3.  **Clojure (Inspiration Only):** Clojure is no longer used for writing code. It serves as an inspiration, especially its syntax, for the future Aski language. All legacy Clojure code is slated for migration to sema-style Rust + Cap'n Proto.
4.  **Nix (Low-Level Utility):** The substrate of reproducibility. Only used for its specific usefulness. Should be **phased out** as early as possible or hidden behind low-level Aski interfaces (see **Lojix**, the Aski-Nix subsystem).

**Inspiration and First Principles:**
- External projects like **Attractor** (StrongDM) are considered **inspirations only** for agent-flow concepts. 
- We do not fork them. Mentci-AI is implemented from first principles using the most correct runtimes existing (Rust + Cap'n Proto + Aski).
- The internal state is not represented by visual DOT graphs, but by homoiconic, advanced ASCII representation (EDN initially, moving to strictly-parsed Aski data).

## 0.1. RUST ACTOR MANDATE (ORCHESTRATION)

**All core orchestration and symbolic manipulation must be implemented as Rust actors.**

*   **Actor-First:** Use the `ractor` framework for all supervised task execution.
*   **Minimal Shim:** The *only* permissible shim is the call to the `execute` orchestrator within the Nix Jail `shellHook`.
*   **Rust Only Mandate:** You must *only* use Rust for writing application logic, orchestration, and scripting outside of the Nix domain. No Python, Clojure, or ad-hoc shell scripts. Legacy tools must be rewritten as Rust actors (sema-style rust+capnp-spec).

## 0.2. SOURCE CONTROL PROTOCOL (JJ & PUSH)

**Commit structural changes atomically and frequently.**

*   **Branch:** All active development occurs on the **`dev`** bookmark.
*   **Push Cadence:** Every structural change must be immediately committed and pushed to `dev`.
*   **Commit-Every-Intent (Level 5):** The Mentci Engine must automatically execute a `jj` commit after **every single atomic modification** (e.g., one file write, one replacement). Each commit represents exactly one intention. Combining multiple unrelated modifications into a single "success" commit is forbidden.
*   **Authority:** Li Goldragon is the highest authority (Top Admin).
*   **Protocol Correction Persistence (Higher-Order Rule):** When a user corrects workflow/protocol behavior, that correction must be encoded in the authoritative `Core/*.md` guidance files in the same prompt session and committed; commit-message-only corrections are insufficient.
*   **Shipping Protocol (Jail):** Isolated agents in a Jail environment must use the `mentci-commit` tool to ship changes. This tool synchronizes a writable implementation workspace back to the project root and performs a `jj` commit to the target bookmark specified in `MENTCI_COMMIT_TARGET`.
*   **Usage:** `mentci-commit "intent: <message>"`
*   **Operational Steps:** See `Core/VersionControlProtocol.md`.

## 0.3. TOOL STACK TRANSPARENCY

**Agents must explicitly document the underlying stack of any new tool.**

When introducing a new tool, library, or dependency (e.g., via `nixpkgs` or vendoring), the agent must explicitly state:
1.  **Language/Runtime:** (e.g., Clojure/Babashka, Rust/Cargo).
2.  **Origin/Provenance:** (e.g., `nixpkgs#jet`, GitHub repo `babashka/babashka`).
3.  **Rationale:** Why this specific implementation was chosen over others.
4.  **Execution Context:** Use `nix develop` for non-standard tools, libraries, or dependencies.

## 0.3.1. STATE TRANSPORT PROTOCOL

**Operational state must be passed via data files, not process environment variables.**

*   **Required:** Use structured files for runtime state exchange between launcher, daemon, and commit tooling.
*   **Preferred wire authority:** Cap'n Proto is the primary transport for initialization and launch envelopes. JSON is fallback-only when a Cap'n Proto envelope is not yet defined.
*   **Forbidden:** Ad-hoc routing state through env vars when a data-file channel exists.
*   **Rationale:** Data files are auditable, reproducible, and schema-validated.

## 0.3.2. CAPNP INIT ENVELOPE MANDATE (VERY HIGH IMPORTANCE)

**All runtime initialization data must be present in one Cap'n Proto init request object.**

*   **Scope:** Applies across Rust, Clojure, and Nix integration boundaries.
*   **Required:** Launchers and services (including terminal launch paths) ingest their runtime configuration from a single Cap'n Proto init message.
*   **Forbidden:** Splitting configuration semantics between init message and ad-hoc environment variables.
*   **Allowed env surface:** OS/runtime process concerns only (for example PATH, HOME, locale), not domain configuration.
*   **Security objective:** Prevent data pollution of the logical machine by keeping intent/state in schema-validated objects.

## 0.3.3. MODEL CONTEXT PROTOCOL (MCP) APPARATUS

**Custom tooling must be exposed to agents via an MCP server apparatus.**

*   **Implementation:** Tools required by the agent (e.g., reading/parsing Cap'n Proto binaries, executing Jujutsu operations) should be exposed via a native Rust MCP server (`mentci-mcp`).
*   **Rationale:** MCP is the standard for connecting LLM interfaces (like `pi`) to custom backends. This avoids fragile bash-script wrappers and gives agents reliable, schema-validated tool calling capabilities over standard JSON-RPC streams.
*   **Two-Way Aski Utility:** The MCP apparatus will eventually expose a strict two-way utility tool. Agents will interact with Aski/Lojix in a hyper-concise "minified" format to save token context, while the utility deterministically renders it into a "whitespace-beautiful" format for human review and VCS diffing.

## 0.4. AUDIT TRAIL

**The authoritative audit trail is the Jujutsu history.**

*   **Auditability:** Use `jj log` for provenance and review.
*   **Intent Discovery:** Use `jj log` frequency analysis to determine the "True Weight" of goals and maintain alignment with the project's evolution (see `Library/IntentDiscovery.md`).

## 0.5. ADMIN DEVELOPER MODE (SYSTEM ORCHESTRATION)

**High-authority agents operate in Admin Developer Mode.**

*   **Unrestricted Access:** When an agent is operating as Mentci (or at the direction of the Top Admin, Li Goldragon), it is expected to have unrestricted shell access for system-level orchestration (`nix`, `jj`, `bb`, etc.).
*   **Safety Bypass:** Standard tool-level safety filters (e.g., "cannot be parsed safely") that block primitive commands (like `ls` or `whoami`) are considered architectural friction in this mode and must be resolved at the infrastructure layer to allow Level 5/6 evolution.
*   **Responsibility:** The agent is responsible for maintaining the integrity of the jail while exercising this authority.

## 0.6. RECURSIVE SUB-FLOW EXECUTION (RUSSIAN DOLL MODEL)

**Processes orchestrate sub-processes in a fractal hierarchy.**

*   **Master Process:** The top-level `mentci-ai` process is self-hosted. It mounts `Sources/` and configures its environment exactly like a jailed process but operates with full **Admin Developer Mode** privileges.
*   **Sub-Flow Isolation:** Sub-processes are spawned as isolated sub-flows, typically within strict Nix Jails.
*   **State Separation:** Sub-flows must operate on **unique, internal Jujutsu bookmarks**. They do not commit directly to the parent's bookmark until finalization.
*   **Supervision:** Parent processes supervise the lifecycle, sources, and outputs of their child sub-flows.

This document synthesizes the architectural, naming, and durability rules inherited from the **CriomOS** and **Sema** lineage. These rules are structural and non-negotiable. Violations indicate category errors, not stylistic choices.

Mentci-AI incorporates the Attractor standard for workflow orchestration. It does not implement Attractor.

## 0.5.1. PER-LANGUAGE SEMA GUIDELINES

*   **Clojure:** `Core/SEMA_CLOJURE_GUIDELINES.md`
*   **Rust:** `Core/SEMA_RUST_GUIDELINES.md`
*   **Nix:** `Core/SEMA_NIX_GUIDELINES.md`

## 0.6. NIX STORE ACCESS

**Do not scan `/nix/store` directly.**

*   **Efficiency:** The store is large and filesystem searches are wasteful.
*   **Protocol:** Query store metadata through the Nix CLI (daemon-backed) instead of filesystem traversal.

## 0.7. JJ LOGGING WITHOUT SIGNING

**When reading history, prefer `jj log` (and similar) with signing disabled.**

*   **Reliability:** Disable signing for read-only history inspection to avoid GPG/agent failures.
*   **Scope:** This applies to log/describe/show commands that do not modify history.

## 0.8. CODE-FIRST GOVERNANCE (CONTEXT MINIMIZATION)

**Prefer implementing repeatable behavior in code over adding instruction text.**

*   **Rule:** If a behavior can be enforced by a script, guard, or generated artifact, implement it there instead of expanding agent instruction payloads.
*   **Trigger:** Repeated manual directives (same class of correction appearing multiple times) must be converted into executable checks or helpers.
*   **Goal:** Reduce prompt/context surface area, improve execution reliability, and keep policy concise.
*   **Documentation Role:** Docs state intent and contracts; code enforces operational mechanics.

## 0.9. CORE SELF-CONTAINED EXTENSION INDEXES

**Core must remain stable and universal; extension points are optional index files in fixed locations.**

*   **Contract Source:** `Core/ExtensionIndexProtocol.md`
*   **Location Registry:** `Core/ExtensionIndexLocations.edn`
*   **Resolver Tool:** `execute extension-index`
*   **Rule:** Prefer adding capabilities by registering or updating index files in standard extension locations instead of editing multiple core authority files.
*   **Operational Mode:** Missing extension indexes are non-fatal; extension loading remains optional.

## 1. Core Philosophy: The Semantic Layer

**Names are not descriptions; they are commitments.**
Meaning is distributed across repository names, directory paths, module names, and type definitions. Meaning must appear exactly once at the highest valid layer. Repetition across layers (e.g., `UserObject`, `Manager`) is forbidden.

**Contextual Sovereignty:**
High-level architectural context (framing, mission statements, global mandates) must reside exclusively in `Core/` and `RestartContext.md`. Replicating this context into individual child files as "headers" is forbidden. Child files must contain only implementation-specific logic, inheriting the global context from the hierarchy.

## 2. Capitalization-Based Durability Rules (Filesystem)

**Capitalization in paths and filenames encodes durability**—the resistance of instructions to modification. This is orthogonal to code style.

| Tier | Casing | Meaning | Rules |
| :--- | :--- | :--- | :--- |
| **Supreme Law** | `ALL_CAPS` | Immutable constraints. | **Fully Human Review Data.** Never edited by agents. Loaded first. |
| **Stable Contract** | `PascalCase` | Durable structure/intent. | Agent-refinable to satisfy Supreme Law. Edited only by mandate. |
| **Mutable Implementation** | `lowercase` | Operational detail. | Freely editable/refactorable by agents. |

*   **Conflict Resolution:** `Supreme Law` > `Stable Contract` > `Implementation`.
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
*   **Single Object In/Out:** Methods accept at most one explicit object (struct) and return one object. Complex Sources/outputs require their own container structs.
*   **Schema is Sema:** Transmissible objects are defined in Sema schemas. Wire formats (Cap'n Proto) are implementation details, not domain APIs.

## 4. Sema Object Style (Nix)

**Attrsets Exist; Flows Occur.**
*   **Naming:**
    *   `camelCase`: Functions, relations, flow.
    *   `kebab-case`: Static packages, attributes.
    *   Context-local names must not restate the enclosing namespace (e.g., in `nix/`, prefer `namespace` over `nixns`).
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

## 8. ECLIPTIC CHRONOGRAPHIC VERSIONING

**All versions and dates must adhere to the True Solar Ordinal-Zodiac system.**

See **`Library/architecture/ChronographySpec.md`** for the complete technical specification of the 1-based ordinal system.

*   **Year System:** Anno Mundi (AM). (Current Year: 5919 AM).
*   **Version Format:** `Cycle.Sign.Degree.Minute.Second`
    *   **Sign:** 1 (Aries) to 12 (Pisces).
    *   **Degree:** 1 to 30.
    *   **Minute:** 1 to 60.
    *   **Second:** 1 to 60.
    *   **Cycle Mapping:** `Cycle = (AnnoMundiYear - 5919)` so `5919 AM -> 0`, `5920 AM -> 1`.
*   **Rationale:** Aligning project state with true solar coordinates.
