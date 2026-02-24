# Agent Report

- Chronography: `12.7.4.31 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Agent-Authority-Alignment`
- Title: `pi-rename-nix-componentization`

## Prompt

rename @Components/nix/coding_agent.nix to "pi", as that is the name of the utility - leave a report on the renaming for later project-wide renaming. the @Components/nix/pi_agent_rust.nix will be pi-rust. start with nix renaming and wherever it's used in mentci-aid or its deps


## Agent Answer

Renamed the core Nix definitions for the primary agent interfaces to align with their utility names and establish a clearer distinction between the default TypeScript implementation and the Rust validation lane.

1.  **Nix File Renaming:**
    - `Components/nix/coding_agent.nix` -> `Components/nix/pi.nix` (The TypeScript/Node.js utility).
    - `Components/nix/pi_agent_rust.nix` -> `Components/nix/pi-rust.nix` (The Rust implementation).
    - `Components/nix/coding_agent_check.nix` -> `Components/nix/pi_check.nix`.
2.  **Namespace Alignment (`Components/nix/default.nix`):**
    - Updated internal variable names: `coding_agent` -> `pi`, `pi_agent_rust` -> `pi_rust`.
    - Updated `inherit` lists and cross-component dependencies (e.g., `attractor`).
3.  **Flake Registry Update (`flake.nix`):**
    - Updated `packages` and `checks` to use `pi` and `piRust`.
    - Switched `apps.pi` to point to the TypeScript `pi` package (default operator interface) and added `apps.pi-rust` for the Rust variant.
4.  **Downstream Dependency Sync:**
    - Updated `attractor.nix` to use the new `pi` argument.
    - Updated `common_packages.nix` to use `pi` and `pi_rust`.

This renaming establishes `pi` as the primary command-line identity for the TypeScript agent while preserving `pi-rust` as the secondary lane, preparing for a broader project-wide alignment of these identifiers.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
