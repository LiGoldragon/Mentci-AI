# Report: Nix `pi-withExtensions` Linkup Packaging Strategy

## Original Prompt
"make the nix pi-withExtensions, with linkup made into a pi-package nix extension and added into the meta-derivation, which is all accessed through the pi prefix var. whats the best way to nix package npm this way nowadays?"

## Summary
Implemented a Nix-native `pi-with-extensions` composition that bundles Linkup under the `PI_PACKAGE_DIR` prefix and loads it via `--extension "$PI_PACKAGE_DIR/node_modules/@aliou/pi-linkup"`.
Also moved to latest upstream `pi-mono` lock revision (non-antigravity path) and rebuilt `pi` with updated npm dependency hash.

### Implemented Structure
- `Components/nix/pi-linkup-extension.nix`
  - Packages `@aliou/pi-linkup@0.7.3` from npm tarball.
  - Packages `@aliou/pi-utils-settings@0.4.0` and wires it into `node_modules/@aliou/pi-utils-settings`.
- `Components/nix/pi-with-extensions.nix`
  - Composes from `pi_dev` payload.
  - Adds linkup package under `.../node_modules/@aliou/pi-linkup`.
  - Wrapper sets/uses `PI_PACKAGE_DIR` and injects Linkup extension path from that prefix.
- `Components/nix/default.nix`
  - Adds `pi_linkup_extension` + `pi_with_extensions`.
  - Makes dev shell use `pi_with_extensions` as the `pi` runtime.
- `flake.nix`
  - Exposes `packages.piWithExtensions` and `packages.piLinkupExtension`.

## Why this approach now
For third-party npm extension packaging in Nix, the most practical current approach for small extension sets is:
1. **Tarball pinning with `fetchurl` + SRI hashes** for reproducibility.
2. **Minimal no-build unpack derivations** for source/TS-based pi extensions.
3. **Explicit dependency wiring** for extension-local runtime deps (here: `pi-utils-settings`).
4. **Meta-derivation composition** that keeps extension paths under one controlled prefix (`PI_PACKAGE_DIR`).

This avoids stateful runtime `npm install`, keeps closure immutable, and preserves pi extension discovery semantics.

## Alternative (future scale-up)
If extension count grows significantly or transitive deps become deep, migrate to lockfile-driven npm build machinery (`buildNpmPackage` / lock import workflows) for full dependency graph automation. For the current single-extension target, tarball pinning is simpler and robust.
