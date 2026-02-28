# Mentci-AI: Level 5 "Dark Factory" AI Daemon
**Last Update:** `5a7805017fd2` (Fractal Universe Phase 2 Baseline)

## mentci-aid (Mentci-AI Daemon)
The core Rust logic of this repository is identified as **mentci-aid**. 
- **Daemon:** It serves as the background execution engine for autonomous workflows.
- **Aid:** Derived from the Latin root for "help," reflecting its mission as a collaborative agentic partner.

**Status:** `mentci-aid` is currently under active development and is **not in a running state**. It should be treated as an experimental prototype.

Mentci-AI is a Nix-and-Rust based AI daemon designed for **Level 5 "Dark Factory"** programming, evolving toward **Level 6 Instinctive Symbolic Interaction**. It is built on the semantic foundations of the **CriomOS** and **Sema** lineage.

## Mission
To automate implementation details and liberate the human mind through autonomous symbolic manipulation, returning to the technological levels of the Golden Age.

## Core Pillars
1.  **Fractal DVCS Isolation**: Distributed `jj` repositories coordinated via Saṃskāra and the Flow Registry.
2.  **Sema Object Style**: Strict ontological structure in Rust and Nix.
3.  **Logical Plane**: AST-aware symbolic interaction and structural editing (Tree-sitter/ast-grep).
4.  **Hermetic Nix Integration**: Pure, reproducible execution environments using Flake inputs for distributed components.

## Structure
- `Core/`: Authoritative architectural mandates and protocols.
  - `Library/RestartContext.md`: The primary overview and state-resumption map.
- `Components/samskara/`: The structural bridge and fractal orchestrator.
- `Components/schema/`: The semantic truth (Cap'n Proto).
- `Components/nix/`: The isolated dev shell and build pipeline.
- `Components/mentci-aid/`: The Rust daemon implementation (**mentci-aid**).
- `.mentci/flow_registry.db`: SQLite shadow index tracking the fractal repository network.
- `execute`: High-performance Rust/Actor orchestrator.
- `Library/`: Secondary documentation (architecture, specs, guides).

## Usage
- `nix develop`: Enter the Level 5 Jail environment.
- `execute launcher`: Initialize the Jail (provisions Sources).
- `nix run .#execute -- <command>`: Run the exported `execute` command surface without entering a dev shell.
- `nix run .#mentciBoxDefault`: Start the default Mentci-Box isolation environment.
- `nix run .#mentci-launch -- <launch-request.capnp>`: Launch Mentci-Box via systemd terminal/service request envelope.
- `cargo build --manifest-path Components/mentci-aid/Cargo.toml`: Compile the daemon and Cap'n Proto schemas.
- `cargo run --manifest-path Components/mentci-aid/Cargo.toml -- Components/workflows/example.dot`: Run the workflow engine.
- `execute root-guard`: Run the filesystem integrity guard.
- `execute link-guard`: Run the cross-tier link guard.
- `execute session-guard`: Verify the commit graph and session state.
- `execute version`: Get the content-addressed program version.
- `execute unify --write`: Enforce R&D mirror counterpart coverage.
- `execute finalize`: Cryptographically synthesize and push the session.
- `nix build .#execute`: Build the exported `execute` app package from flake outputs.
- `cargo test --manifest-path Components/mentci-aid/Cargo.toml`: Run `mentci-aid` standalone tests.
- `cargo test --manifest-path Components/chronos/Cargo.toml`: Run `chronos` standalone tests.

---
*The Great Work continues.*
