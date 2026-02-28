# Research Report: Hermetic Flake Inputs in Fractal DVCS

## 1. The "Staged Input" Dilemma Resolved
Research during the implementation of the `mentci-user` fractalization identified a critical mismatch between `jj` (Jujutsu) and Nix Flakes. Nix Flakes require a formal Git reference to track files in a local directory.

### Key Learnings:
- **Git Colocation Mandate:** Every fractal component repository MUST be initialized with `jj git init --colocate`. Nix cannot see files inside a raw `jj` repo.
- **Named Ref Requirement:** Nix `git+file://` inputs default to the `master` or `main` branch. Since `jj` works on anonymous heads, we must maintain a `main` bookmark and ensure it is exported to the Git backend (`jj git export`).
- **Committed Lockfiles:** Nix builds in a strict sandbox. Files like `Cargo.lock` that are typically generated must be formally committed to the component's repo, or the `crane` build will fail to vendor dependencies.

## 2. Verification Procedure
To verify a new fractal component integration:
1. **Initialize:** `cd Components/<name> && jj git init --colocate`.
2. **Seal:** Run `cargo generate-lockfile` inside the component dir.
3. **Commit:** `jj commit -m "intent: seal component"`.
4. **Bookmark:** `jj bookmark set main -r @`.
5. **Link:** Add to root `flake.nix` inputs.
6. **Update:** `nix flake update <input-name>`.
7. **Test:** `nix build .#<attrName> --no-link`.

## 3. Future Improvements
- **Automated Extraction:** Create a `samskara-extract` tool to automate steps 1-5.
- **Flake Registry:** Transition from `git+file://` to a local flake registry or `jj` remote protocol once `jj` native remotes stabilize.
- **Lockfile Synchronization:** Implement a watcher that re-generates and commits `Cargo.lock` whenever `Cargo.toml` changes in a component.

---
*Verified via successful build of .#mentciUser*
*Solar: 5919.12.10.59.20*
