# Strategy: Repo-Wide EDN to Cozo Migration (Aggressive Mode)

## Goal

Replace EDN as active runtime authority across the repository and standardize on Cozo scripts for mutable symbolic data.

## Operating Policy

1. No new runtime-authority `.edn` files.
2. New runtime authority data must be authored as `.cozo`.
3. When touching EDN runtime artifacts, replace them with Cozo equivalents in the same change.
4. EDN remains temporarily only for legacy indexes/compatibility surfaces until direct Cozo query infrastructure replaces them.

## Priority Order

1. **Init/launch authority** (highest): workspace init, agent intent, lane policy.
2. **Message/contract authority**: message catalogs and flow-driving data.
3. **Component-local runtime sidecars**.
4. **Index metadata** (`index.edn`) as final replacement wave.

## Immediate Migration Moves

- Canonical AI shim bootstrap is `AI-init.cozo`.
- Repo-mode resolution standard: `<repo>/AI-init.cozo`.
- VersionOne operational-init examples updated to Cozo-first.

## Enforcement

- Add policy checks that fail on newly introduced runtime-authority `.edn` files.
- Keep a small explicit whitelist for temporary legacy EDN files.
- Remove whitelist entries only after Cozo replacement and parity verification.

## Verification Standard

For each migrated artifact:

1. Cozo script parses and executes.
2. Resulting relations satisfy expected integrity constraints.
3. Rust loaders resolve Cozo path as authoritative input.
4. Workspace checks remain green (`cargo check --workspace` where code-paths are changed).

## Risk Control

- Do not perform big-bang replacement of all EDN files in one commit.
- Use atomic per-domain migrations with clear rollback points.
- Preserve semantic parity before deleting legacy EDN sources.
