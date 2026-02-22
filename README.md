# Mentci-AI: Level 5 "Dark Factory" AI Daemon

## mentci-aid (Mentci-AI Daemon)
The core Rust logic of this repository is identified as **mentci-aid**. 
- **Daemon:** It serves as the background execution engine for autonomous workflows.
- **Aid:** Derived from the Latin root for "help," reflecting its mission as a collaborative agentic partner.

**Status:** `mentci-aid` is currently under active development and is **not in a running state**. It should be treated as an experimental prototype.

Mentci-AI is a Nix-and-Rust based AI daemon designed for **Level 5 "Dark Factory"** programming, evolving toward **Level 6 Instinctive Symbolic Interaction**. It is built on the semantic foundations of the **CriomOS** and **Sema** lineage.

## Mission
To automate implementation details and liberate the human mind through autonomous symbolic manipulation, returning to the technological levels of the Golden Age.

## Core Pillars
1.  **Nix Jail Isolation**: Pure, reproducible execution environments.
2.  **Sema Object Style**: Strict ontological structure in Rust and Nix.
3.  **Attractor Orchestration**: DOT-based DAG pipelines for AI workflows.
4.  **Handshake Logging Protocol**: Cryptographic provenance and EDN-based record of truth.

## Structure
- `Core/`: Authoritative architectural mandates and protocols.
  - `Library/RESTART_CONTEXT.md`: The primary overview and state-resumption map.
- `Components/schema/`: The semantic truth (Cap'n Proto).
- `Components/nix/jail.nix`: The isolated dev environment definition.
- `Components/src/main.rs`: The Rust daemon implementation (**mentci-aid**).
- `Components/scripts/`: Babashka/Clojure orchestration and logging.
- `Components/workflows/`: DOT files defining agent execution graphs.
- `Outputs/Logs/`: Durable audit trails and milestone logs.
- `Library/`: Secondary documentation (guides, reports).

## Usage
- `nix develop`: Enter the Level 5 Jail environment.
- `cargo build --manifest-path Components/Cargo.toml`: Compile the daemon and Cap'n Proto schemas.
- `cargo run --manifest-path Components/Cargo.toml -- Components/workflows/example.dot`: Run the workflow engine.
- `bb Components/scripts/logger/main.clj "<intent>" --model "<model>" --user "<user>"`: Optional legacy intent log entry (`jj log` is preferred for audit trails).

---
*The Great Work continues.*
