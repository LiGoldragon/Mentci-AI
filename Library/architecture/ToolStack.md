# Mentci-AI Tool Stack

## Core Development
### Rust
- **Role:** Primary implementation language for the Mentci daemon.
- **Usage:** Performance-critical logic, state management, and orchestration.

### Nix
- **Role:** Environment management and isolation (Jail).
- **Usage:** Defines the reproducible development shell and project Sources via `flake.nix` and `Components/nix/jail.nix`.

## Agentic Interface & Orchestration
### Jujutsu (jj)
- **Role:** Version control and workspace management.
- **Usage:** Provides the `jj` command for atomic changes and multi-workspace isolation.
- **Automation:** See `Core/VersionControlProtocol.md` for the operational workflow.
- **Protocol:** See `JailCommitProtocol.md` for details on the implementation-to-host shipping model.

### mentci-commit
- **Role:** The "Hole in the Jail" for shipping manifested code from the implementation workspace back to the host.
- **Implementation:** Babashka wrapper around `jj` cross-workspace commands.
- **Usage:** `mentci-commit "message"` advances the `dev` bookmark to the current workspace state.

### mentci-jj
- **Role:** Standardized `jj` entrypoints for status, log, and commit in the active workspace.
- **Implementation:** Babashka command router for common `jj` flows.
- **Usage:** `mentci-jj status`, `mentci-jj log`, `mentci-jj commit "message"`.

## Data & Serialization
### EDN (Extensible Data Notation)
- **Role:** Structured data interchange format.
- **Usage:** Used for specifications and configuration data.

### Jet
- **Role:** High-performance data processing and transformation.
- **Usage:** Used for serializing/deserializing structured attributes.

### Cap'n Proto
- **Role:** Schema definition and RPC.
- **Usage:** Defines the semantic truth of atoms and daemon interfaces in `Components/schema/`.

## Scripting & Glue
### Clojure (Babashka)
- **Role:** The "Mandatory Glue" for project orchestration.
- **Usage:** Used for launchers and build scripts to manage complex logic without shell scripts.
