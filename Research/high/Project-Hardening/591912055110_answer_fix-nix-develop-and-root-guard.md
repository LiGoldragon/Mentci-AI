# Research: Fix Nix Develop Environment and Root Guard

## Findings

1.  **Missing Cargo.lock:** The `mentci-vcs` component was missing a `Cargo.lock` file, causing `nix develop` to fail as `craneLib.buildPackage` (used in `Components/nix/mentci_vcs.nix`) requires it.
2.  **Root Guard Drift:** The `root_guard` check failed because the `.mentci` directory (used for runtime configuration like `jj-project-config.toml`) was not in the allowlist.
3.  **Extraneous Artifacts:** A `result` symlink and an `outputs/` directory (likely from previous `nix build` operations) were present in the root, violating the filesystem contract.

## Resolution

1.  Generated `Components/mentci-vcs/Cargo.lock` via `cargo update` and added it to git tracking (`git add -N`).
2.  Updated `execute` to include `.mentci` in `allowed-runtime-dirs`.
3.  Removed the `result` symlink and `outputs/` directory from the root.

## Verification

1.  `nix develop --command true` now succeeds.
2.  `execute root-guard` now passes.
3.  `execute session-guard` identifies the need for session finalization.
