# Direction Update: Split into Smaller Repositories

## Intent
Transition from a monorepo-centric operational model to a multi-repository topology with explicit sidecar Saṃskāra databases and handshake-governed coordination.

## Immediate Implications for VersionOne
- VersionOne becomes the canonical planning and protocol surface for the split strategy.
- Research required for this transition is duplicated locally under `VersionOne/research/` for direct adjacency with protocol and data artifacts.
- The master Saṃskāra database remains responsible for discovering sidecar databases and bringing sidecar `samskarad` processes online.

## Coordination Model
- Each sub-repo can own a concern-local Saṃskāra DB.
- The master DB tracks sidecar DB metadata, process health, and handshake state.
- Sidecars can either:
  1. expose federated query endpoints,
  2. replicate selected relations into master,
  3. or run a hybrid strategy based on policy.

## Reliability
- All cross-DB interactions are handshake-gated.
- Lease/failback semantics remain mandatory for robust handover and recovery.

## Next Research Focus
- CozoScript-first migration guide with executable examples as tests.
- Sidecar registry schema for multi-repo discovery and controlled bring-up.
