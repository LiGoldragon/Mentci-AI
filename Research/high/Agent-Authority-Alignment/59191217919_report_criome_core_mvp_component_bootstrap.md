# Criome-Core MVP Component Bootstrap

## Objective
Bootstrap a runnable `criome-core` Rust component that:
1. approves batches of Crio proposals,
2. converts approvals into transactions,
3. applies transactions to a Cozo-backed Crio state,
4. serves Crio horizon messages from current state.

## Implemented scope
- New component: `Components/criome-core`
  - `src/contracts.rs`: proposal/request/transaction/horizon message contracts.
  - `src/state.rs`: in-memory canonical `CrioState` with revisioning.
  - `src/engine.rs`: `CrioCoreEngine` approval pipeline + Cozo transaction mirror.
  - `src/bin/criad.rs`: minimal CLI daemon entry (`approve`, `horizon`).
  - `tests/mvp_flow.rs`: integration tests for approve->state->horizon and duplicate rejection.
- Workspace/component registration:
  - `Cargo.toml` workspace member includes `Components/criome-core`.
  - `Components/index.edn` includes `criome-core` interfaces.
  - `Components/contracts/rust-component-repos.toml` includes `criome-core` component contract row.

## TDD evidence
- RED: added integration tests first; initial runs failed due missing modules.
- GREEN: implemented minimal contracts/state/engine to satisfy tests.
- Additional failure/fix loop: Cozo schema initialization issue fixed by explicit per-relation `:create` calls.
- Final tests passing:
  - `cargo test --manifest-path Components/criome-core/Cargo.toml`

## Runtime smoke
- Ran:
  - `cargo run --manifest-path Components/criome-core/Cargo.toml --bin criad -- approve <batch.json>`
- Output: JSON approval decision with generated transactions and `state_revision`.

## Current MVP boundaries
- Cozo backing is active via transaction mirroring inside `CrioCozoStore` (in-memory Cozo instance for MVP runtime).
- Horizon projection uses current in-process canonical state snapshot.
- Crypto/quorum verification logic is intentionally deferred to the upcoming `criad`/`criome-core` soul-key + hot-key authority layer defined in architecture discussion.

## Next extension seams
1. Replace Cozo memory backend with persistent store profile + startup replay/restore.
2. Add signed decision envelope path (external channels) while preserving unsigned local subordinate response path.
3. Replace string interpolation for Cozo writes with fully parameterized script binding.
4. Add Cap'n Proto message contract parity with CriomOS capnp definitions.
