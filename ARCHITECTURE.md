# Mentci-AI: System Architecture

The following flowchart describes the structural and operational layers of Mentci-AI, integrating the Nix Jail, Rust Daemon, and Semantic Logging protocols.

```mermaid
graph TD
    subgraph "Layer 0: Environment (Nix Jail)"
        A[nix develop] --> B(jail.nix)
        B --> C[__structuredAttrs]
        C --> D(scripts/launcher.clj)
        D --> E{inputs/}
        E --> E1[Atom Inputs]
        E --> E2[Flake Inputs]
        E --> E3[Attractor Specs]
    end

    subgraph "Layer 1: Execution (Rust Daemon)"
        F[src/main.rs] --> G[PipelineEngine]
        G --> H[Graph Traversal]
        H --> I{Handlers}
        I --> J[ExecutionEnvironment]
        J --> K[Local FS / Shell]
    end

    subgraph "Layer 2: Semantic Truth & Persistence"
        L[schema/*.capnp] -.-> F
        M[scripts/logger.clj] ==> N[Logs/*.edn]
        I -- Handshake Logging --> M
    end

    E3 -.-> G
    F -- Trait Impl --o J
```

## Architectural Components

### 1. Nix Jail (Isolation)
The project operates within a **Pure Nix Jail**. `jail.nix` uses structured attributes to pass data to `scripts/launcher.clj`, which organizes all inputs into a standardized filesystem ontology under `inputs/`.

### 2. Mentci Daemon (Rust)
A **Level 5 Pipeline Engine** built in Rust. It implements the **Attractor** specification:
*   **ExecutionEnvironment**: Decouples daemon logic from the jail's shell.
*   **PipelineEngine**: Manages graph traversal and node state.
*   **Handlers**: Atomic units of work (Start, Exit, Codergen).

### 3. Semantic Layer
*   **Cap'n Proto (`schema/`)**: Defines the "Truth" of the system (Filesystem Atoms, RPCs, Graph structures).
*   **EDN Logging (`Logs/`)**: Durable, symbolic record of agent intent and model metadata, following the Handshake Logging Protocol (implemented in Babashka).

---
*The Great Work continues.*
