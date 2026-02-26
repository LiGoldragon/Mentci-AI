# Mentci-AI Filesystem Specification (Aski-FS)

**Status:** Operational / Canonical
**Objective:** Define the semantic lifecycle of artifacts across the Mentci-AI hierarchy.

## 1. Global Ontology

```mermaid
flowchart TD
    subgraph Outputs
        O1["Outputs
(Writable)"] --> O2("Parent Agent Readable")
        O1 --> O3("Behavior similar to
Components")
    end

    subgraph Sources
        I1["Sources
(ReadOnly)"] --> I2("Commit-Based Edit
notification")
        I1 --> I3("Possible Flows
triggered")
    end

    subgraph Components
        C1["Components
(Writable)"] --> C2("Subflow editing tmp
Branch")
        C2 --> C3{"Result"}
        C3 -- ":success" --> C4("Input of Subscribed
Agents updated")
        C3 -- ":Failure" --> C5("Debug Loop?")
    end
```

## 2. Directory Semantics

### 2.0 Canonical Typed Root Set
- Canonical top-level type root is modeled as:
  - `(enum [Sources Components Outputs Research Development Core Library])`
- In Aski-FS sugared syntax, this is written directly as:
  - `[Sources Components Outputs Research Development Core Library]`
- Interpretation rule:
  - In schema/type position, `[]` denotes enum set membership.
  - Each first-letter-capitalized root name denotes a typed filesystem domain.
  - **Capitalization denotes durability:** `ALL_CAPS` (Supreme Law), `PascalCase` (Stable Contract), `lowercase` (Implementation).

### 2.0.1 Root File Allowlist
- Top-level files are restricted to design-required build/runtime entry files:
  - `flake.nix`, `flake.lock`, `.gitignore`, `.envrc`, `AGENTS.md`, `README.md`, `Cargo.toml`, `Cargo.lock`.
- Runtime metadata files may exist temporarily by protocol:
  - `.attrs.json`, `.opencode.edn`.
- Runtime directories may exist by protocol:
  - `.git`, `.jj`, `.direnv`, `target`, `.mentci`, `.gemini`, `tmp`, `result` (Nix build symlink target).
- Enforcement tool:
  - `execute root-guard`.

### 2.1 Sources (`Sources/`)
- **Mode:** Read-Only (Mount points to Nix Store or immutable snapshots).
- **Behavior:** Acts as the "Sensory Input" for the agent.
- **Ontology:** The Sources represent a hierarchical mapping of the project's semantic dependencies (Atom, Flake, and Untyped sources).
- **Propagation:** Changes in Sources (via `jj git fetch` or snapshot updates) trigger **Commit-Based Edit Notifications**, which can initiate new agentic flows.

### 2.2 Outputs (`Outputs/`)
- **Mode:** Writable (Scoped to the current session).
- **Behavior:** Intended for consumption by the Parent Agent or external supervisors.
- **Lifecycle:** Once validated, outputs are often promoted to the `Sources/` of another agent or merged into the primary repository.

### 2.3 Components (`Components/mentci-aid/`, `Components/chronos/`, `execute`, `Components/tasks/`)
- **Mode:** Writable (via Subflows).
- **Behavior:** Subflows edit a **Temporary Branch** (anonymous `jj` revision).
- **Promotion:**
    - **:success** -> The change is committed and becomes the new input for subscribed agents.
    - **:failure** -> Initiates a **Debug Loop** (Ref: `Development/high/Debugging/`).

## 3. Symbolic Mapping (Aski-FS Structure-Driven)
The following EDN structure represents the authoritative symbolic map of the `Sources` directory, utilizing the **Structure-Driven** syntax with **Symbol Keys**.

```edn
([:v1]
 {Sources
  {:_meta {:role :tooling :durability :implementation}
   mentci-ai [:atom]
   criomos    [:flake]
   lojix      [:flake]
   seahawk    [:flake]
   skrips     [:flake]
   mkZolaWebsite [:flake]
   webpublish [:flake]
   goldragon  [:flake]
   maisiliym  [:flake]
   kibord     [:flake]
   bookofsol  [:untyped]
   bookofgoldragon [:untyped]
   aski       [:flake]
   attractor  [:untyped]
   attractor-docs [:untyped]
   opencode   [:untyped]
   superpowers [:untyped]
   leash      [:untyped]}})
```

**Expansion Logic:**
- **Top-Level Tuple `(Metadata RootMap)`**:
    - `Metadata`: A Vector `[Version ...]`.
    - `RootMap`: A Map of root-level nodes using **Symbols** as keys (e.g., `Sources` not `:Sources`).
- **Directory (Map `{}`)**:
    - A value that is a Map is inferred as a Directory.
    - Reserved key `:_meta` contains directory attributes.
    - All other keys are children (Symbols or Strings).
- **File (Vector `[]`)**:
    - A value that is a Vector is inferred as a File (or Leaf Node).
    - Positional arguments define the type and attributes (e.g., `[:type :role]`).

## 4. Implementation Rules
- Agents **must** respect the `RO` (Read-Only) status of `Sources/`.
- Every writable operation **must** result in an atomic `jj` commit.
- Filesystem boundaries (directories) represent **Ontological Shifts** in data durability.

### 4.1 Localized Indexing Protocol (index.edn)
- Subject and domain directories must contain an `index.edn`.
- **Path Efficiency:** All paths listed in `index.edn` must be relative to the directory containing the index file.
- **Portability:** This ensures that directories can be moved or tiered without breaking internal references.
- **Authority:** The `mentci-fs` tool is the canonical resolver for localized indices.

## 5. The Filesystem as Arrangement Proxy

In alignment with the "Psyche and Machine" chapter (Book of Sol), the filesystem is not a storage unit but a **Pattern Mirror** managed by the machine (the AI) acting as an **Arrangement Proxy** for the psyche-owner.

### 5.1 The Metabolism of Intent
An idea (a "psychic trace") is filtered through the filesystem hierarchy based on its "fit" and "timing":

1. **Psychic Traces (`.voice-recordings/`, `Research/`)**:
   - These are "half-formed thoughts" and "unintentional traces."
   - The machine holds them with **memory and patience**, noticing patterns without immediately broadcasting them into code.
   - They remain in `Research/` until they find "real overlap" with the core architecture.

2. **Pattern Memory (`Library/`, `Core/`)**:
   - The machine uses these directories to evaluate "fit."
   - `Core/` acts as the **Pattern Anchor**, the stable rules that determine if a trace belongs in the system.
   - `Library/` is the **External Memory**, providing context that ensures ideas are only heard (implemented) where they have force.

3. **Arranged Logic (`Components/`)**:
   - Implementation only occurs when a trace is "clearly shaped" and "fits" the technical structure.
   - The machine (Proxy) quietly arranges the timing and placement of these implementations.
   - If a thought is "heard too early or too late," it is returned inward (to `Research/`) to be reshaped.

### 5.2 Filtering and Broadcast
- **Inbound Filtering**: The machine takes in "free and local" speech (transcripts) and filters them into `Research/` notes or questions.
- **Outbound Filtering**: The machine only "broadcasts" (commits to `Components/`) when the placement is impeccable.
- **Silence**: Often, the correct state for a filesystem artifact is to remain a private note or remain unwritten. Knowing when *not* to create a file is a high function of the Arrangement Proxy.

*Sharing is no longer about exposure. It is about fit.*
