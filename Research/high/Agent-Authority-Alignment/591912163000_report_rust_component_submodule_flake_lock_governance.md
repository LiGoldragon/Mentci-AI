# Rust Component Split Governance: Submodules for UX, `flake.lock` for LKG

## Intent
Establish a dual-surface workflow for Mentci Rust components:
1. **Mutable co-development surface:** git submodules under `Components/*` so agents can work in one directory tree.
2. **Immutable integration authority:** flake input revisions in `flake.lock` represent the last-known-good component set.

## Why this model
- Submodules optimize agent ergonomics and local multi-repo visibility.
- Flake lock pinning optimizes reproducibility and deterministic integration builds.
- Combining both allows fast iteration without sacrificing release authority.

## Data authority
`Components/contracts/rust-component-repos.toml` is the canonical mapping for:
- component id
- workspace path
- intended flake input key
- canonical remote repo
- migration strictness flags (`required_submodule`, `required_flake_lock`)

## Verification tooling
`mentci-vcs component-split-status` checks each component against:
- git index mode (`160000`) at component path (submodule presence)
- `flake.lock` root input mapping and locked revision presence

Policy is progressive: strictness is toggled per component through manifest flags.

## Promotion protocol
1. Edit component in submodule checkout.
2. Commit + push in component repo.
3. Update submodule gitlink in super-repo.
4. Run integration verification on `dev`.
5. Update flake lock inputs for promoted components.
6. Re-run verification.
7. Commit/push super-repo `dev`.

## Operational rule
`flake.lock` is the final integration authority. Submodule head may move during local development, but lock updates happen only after successful verification.
