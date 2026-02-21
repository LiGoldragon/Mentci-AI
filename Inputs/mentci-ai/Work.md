# Mentci-AI Work Context

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

## Author's Intent
The goal is to develop **Mentci-AI**, a Nix-and-Rust based AI daemon, adhering strictly to the **Sema Object Style** and **Capitalization-Based Durability Rules**.

The environment must support "Level 5" programming (machine-level interaction, debugging, deep system introspection) and integrate specific existing tools and methodologies.

## Critical Requirements
1.  **Inputs**: Incorporate `criomos` and `webpublish` (GitHub) as flake inputs.
2.  **Level 5 Tooling**: Provide tools for machine-level programming (GDB, strace, valgrind, and high-authority orchestration utils).
3.  **Jail Environment**: Implement a "pure flake jail" where the agent can operate with all inputs available in a writable workspace.

## Current State
- [x] **Repository Initialized**: `Mentci-AI` created with `jj` (git backend).
- [x] **Scaffolding**: `flake.nix`, `Cargo.toml`, and `src/main.rs` established.
- [x] **Locking**: `flake.lock` and `Cargo.lock` managed.
- [x] **Inputs Integration**: `criomos`, `webpublish`, `sema`, `attractor`, and `opencode` integrated as flake inputs.
- [x] **Level 5 Tools**: GDB, Strace, Valgrind, and Rsync added to the unified `nix develop` shell.
- [x] **Jail Construction**: `jail.nix` and `jail_launcher.py` implemented; multi-workspace model (Root vs. `workspace/`) active.
- [x] **Shipping Protocol**: `mentci-commit` tool and "Commit-on-Success" mandate established.
- [x] **Version Control**: Ecliptic Chronographic Versioning enforced (Current: v0.12.1.28.44).

## Active Objectives

1.  [x] **Level 4 Orchestration**: Define Cap'n Proto RPCs for agent-to-agent communication in `schema/mentci.capnp`.

2.  [x] **Engine Expansion**: Implement the `Codergen` and `Wait.human` handlers in the Rust daemon (`src/main.rs`).

3.  [x] **Migration Path**: Execute the transition from Gemini CLI to the OpenCode/DeepSeek-V4 stack as per `docs/guides/MIGRATION_GEMINI_TO_OPENCODE.md`. (Infrastructure established).

4.  [x] **Graph Syntax**: Implement DOT parser (`src/dot_loader.rs`) for defining agent workflows (`workflows/*.dot`).

5.  [x] **State Persistence**: Implement `CheckpointManager` to save execution state to `.checkpoints/`.

6.  [x] **Documentation**: Reorganized project documentation into `docs/` structure.
7.  [x] **Attractor Specs**: Extracted DOT data schema from `inputs/untyped/attractor` into `docs/specs/ATTRACTOR_DOT_REFERENCE.md`.
