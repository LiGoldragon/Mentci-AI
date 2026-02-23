# Strategy: Clojure to Rust Migration

## Objective
Evolve the `execute` component into a native Rust symbolic orchestrator using `ractor`, removing the legacy Clojure script layer.

## Roadmap
1. [ ] **Phase 1: Housekeeping**
    - Delete obsolete scripts (`logger`, `session_metadata`, `solar_prefix`).
    - Remove references to manual logging in `README.md`.
2. [ ] **Phase 2: Actor Foundation**
    - Implement `OrchestratorActor` in `execute.rs`.
    - Define `SymbolicMessage` enum for dispatching tasks.
3. [ ] **Phase 3: Integrity Migration**
    - Port `root_guard` logic to `RootGuardActor`.
    - Port `reference_guard` logic to `LinkGuardActor`.
4. [ ] **Phase 4: Session Migration**
    - Port `intent` and `session_finalize` to `SessionActor`.

## Architectural Notes
- **Actor-First:** No shared state between guards. Each guard is a supervised, idempotent worker.
- **Aski Substrate:** Actor messages and configurations are represented as Aski structured data.
