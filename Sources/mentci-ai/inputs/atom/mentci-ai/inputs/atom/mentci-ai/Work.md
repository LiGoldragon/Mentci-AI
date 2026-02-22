# Mentci-AI Work Context

## Author's Intent
The goal is to develop **Mentci-AI**, a Nix-and-Rust based AI daemon, adhering strictly to the **Sema Object Style** and **Capitalization-Based Durability Rules**.

The environment must support "Level 5" programming (machine-level interaction, debugging, deep system introspection) and integrate specific existing tools and methodologies.

## Critical Requirements
1.  **Inputs**: Incorporate `criomos` and `webpublish` (GitHub) as flake inputs.
2.  **Level 5 Tooling**: Provide tools for machine-level programming (as discussed in the context of "Level 5" concepts: GDB, introspection, potentially assembly utils).
3.  **Jail Environment**: Implement `jail.nix` to define a "pure flake jail" — a highly isolated, reproducible development environment where the agent can operate with all inputs available.

## Current State
- [x] **Repository Initialized**: `Mentci-AI` created with `jj` (git backend).
- [x] **Scaffolding**: Basic `flake.nix`, `Cargo.toml`, and `src/main.rs` created.
- [x] **Locking**: `flake.lock` and `Cargo.lock` generated.
- [ ] **Inputs Integration**: `criomos` and `webpublish` are not yet added to `flake.nix`.
- [ ] **Level 5 Tools**: "Level 5" tools (e.g., GDB, low-level debuggers) need to be explicitly added to the dev shell.
- [ ] **Jail Construction**: `jail.nix` does not yet exist.
