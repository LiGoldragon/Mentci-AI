# Nix Environment Stabilization

**Status:** Implementation Complete / Stabilization Target

## 1. Problem: Nix Build/Shell Fragility
The `nix develop` environment was failing due to missing `Cargo.lock` files and root-level filesystem drift that violated the `root_guard` contract.

## 2. Solution: Structural Integrity
Maintain a strict correspondence between component source code and Nix derivations.

### 2.1 Component Lockfiles
Every Rust component defined as a standalone derivation (using `craneLib`) MUST have a tracked `Cargo.lock` file.
- **Rule:** `cargo update` must be run whenever `Cargo.toml` dependencies change.
- **Guard:** `git add -N Cargo.lock` to ensure Nix can see the file during derivation evaluation.

### 2.2 Root Guard Compliance
The `root_guard` ensures the repository root remains clean. Any necessary runtime configuration directories (like `.mentci`) must be explicitly allowed in `Components/scripts/root_guard/main.clj`.

## 3. Implementation Details
- Component: `Components/mentci-vcs`
- Guard: `Components/scripts/root_guard`
- Action: Added `Cargo.lock`, updated `allowed-runtime-dirs`, and removed extraneous root artifacts (`result`, `outputs/`).

*The Great Work continues.*
