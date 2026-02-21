# Mentci-AI: Level 5 "Dark Factory" AI Daemon

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

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
- `core/`: Authoritative architectural mandates and protocols.
  - `core/programs/RESTART_CONTEXT.md`: The primary overview and state-resumption map.
- `schema/`: The semantic truth (Cap'n Proto).
- `jail.nix`: The isolated dev environment definition.
- `src/main.rs`: The Rust daemon implementation (**mentci-aid**).
- `scripts/`: Babashka/Clojure orchestration and logging.
- `workflows/`: DOT files defining agent execution graphs.
- `Logs/`: Durable audit trails and milestone logs.
- `docs/`: Secondary documentation (guides, reports).

## Usage
- `nix develop`: Enter the Level 5 Jail environment.
- `cargo build`: Compile the daemon and Cap'n Proto schemas.
- `cargo run -- workflows/example.dot`: Run the workflow engine.
- `bb scripts/logger.clj "<intent>" --model "<model>" --user "<user>"`: Optional legacy intent log entry (`jj log` is preferred for audit trails).

---
*The Great Work continues.*
