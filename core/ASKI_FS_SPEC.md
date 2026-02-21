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

    subgraph Inputs
        I1["Inputs
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

### 2.1 Inputs (`Inputs/`)
- **Mode:** Read-Only (Mount points to Nix Store or immutable snapshots).
- **Behavior:** Acts as the "Sensory Input" for the agent.
- **Propagation:** Changes in inputs (via `jj git fetch` or snapshot updates) trigger **Commit-Based Edit Notifications**, which can initiate new agentic flows.

### 2.2 Outputs (`Outputs/`)
- **Mode:** Writable (Scoped to the current session).
- **Behavior:** Intended for consumption by the Parent Agent or external supervisors.
- **Lifecycle:** Once validated, outputs are often promoted to the `Inputs/` of another agent or merged into the primary repository.

### 2.3 Components (`core/`, `src/`, `scripts/`)
- **Mode:** Writable (via Subflows).
- **Behavior:** Subflows edit a **Temporary Branch** (anonymous `jj` revision).
- **Promotion:**
    - **:success** -> The change is committed and becomes the new input for subscribed agents.
    - **:failure** -> Initiates a **Debug Loop** (Ref: `strategies/debugging/`).

## 3. Implementation Rules
- Agents **must** respect the `RO` (Read-Only) status of `Inputs/`.
- Every writable operation **must** result in an atomic `jj` commit.
- Filesystem boundaries (directories) represent **Ontological Shifts** in data durability.

*The Great Work continues.*
