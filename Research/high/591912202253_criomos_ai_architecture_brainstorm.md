# CriomOS AI-First Architecture Brainstorm

## Current State Analysis
The `criomos` component currently implements a highly customized Nix evaluation architecture (`mkCriomOS`, `mkWorld`, `pkdjz`, custom `horizon` and `world` abstractions).
- **Non-Canonical Nix:** It manually calls `evalNixos` and builds custom evaluation loops instead of leveraging standard NixOS modules, `flake-parts`, or standard `perSystem` outputs.
- **Logic mixed with configuration:** The Nix code is acting as the primary derivation authority, computing complex logic to resolve nodes, edges, and network topologies, violating the *Sema Programmer* principle: "Keep Nix as Consumer, Not Derivation Authority."

## AI-First Architecture Vision
An AI-first architecture treats the Large Language Model as an orchestrator that interacts with structured logical data, not a text engine writing complex ad-hoc scripts or Nix language strings. 

To achieve this, `criomos` must be modernized under two pillars:

### 1. Sema-Grade Logic-Data Separation (Rust + Cap'n Proto / CozoDB)
The logic of "what exists in the world" (nodes, users, network edges, hardware capabilities) must be extracted from Nix and moved into structured data.
- **Schema is Sema:** Define the world topology in Cap'n Proto schemas and store the facts in CozoDB (Datalog).
- **Rust Core:** A Rust application will read the CozoDB state and deterministically compile the world topology into a flat, structured configuration file (e.g., `attrs.json` or a minimal Nix attrset).
- **Nix as Dumb Consumer:** The Nix layer will be reduced to standard NixOS modules that simply `builtins.fromJSON` the Rust output and apply the configuration to standard NixOS primitives. All custom evaluation (`pkdjz`, `mkCriomOS`) will be deleted.

### 2. Canonical Nix Modernization
- **Flake-Parts Integration:** Restructure `flake.nix` to use `flake-parts`.
- **Standardized Outputs:** Expose standard `nixosModules` and `packages` instead of relying on custom top-level flake outputs.
- **Modularity:** Let standard NixOS module composition (`imports = [ ... ]`) handle the merging of configurations, instead of custom recursive attribute merging.

### 3. AI Orchestration & MCP (Model Context Protocol)
- Instead of LLMs modifying `flake.nix` directly to add servers or change network zones, the AI will interact with a **CriomOS MCP Server**.
- The MCP server will expose tools to query the Datalog substrate (`get_nodes`, `get_edges`) and propose changes logically (`add_node(name, species, size)`).
- This creates a **Typed Protocol Boundary**, ensuring the AI can confidently orchestrate infrastructure changes without hallucinating Nix syntax.

## Migration Plan (Phased Approach)
1. **Schema Definition:** Define the Cap'n Proto contracts for `CriomOS` entities (Node, User, NetworkEdge, Disks, ZFS properties).
2. **Data Extraction:** Translate the current `criomos/data` and `implicitNodes.nix` into Datalog assertions in CozoDB.
3. **Rust Compiler:** Build the Rust core that queries CozoDB and projects the `attrs.json` for Nix.
4. **Nix Refactoring:** Create a canonical NixOS module that consumes `attrs.json`. Swap out `mkCriomOS` for this new module.
5. **Validation:** Verify the final NixOS derivations (`toplevel`, `isoImage`) match the legacy output.
6. **Cleanup:** Delete the legacy `nix/mk*` directories.

## Inquiries for the Human Operator
1. **Storage Substrate:** Should the initial migration target plain Cap'n Proto files for the world state, or should we immediately integrate the CozoDB/Datalog layer for querying?
2. **Strategy vs Incrementalism:** Should we build the Rust compiler side-by-side with the current Nix evaluation and do a hard cutover, or should we incrementally replace Nix modules one by one (e.g., starting with users, then networking)?
3. **MCP Priority:** Should the CriomOS MCP server be developed concurrently with the schema definitions, or deferred until the Nix refactor is complete?
