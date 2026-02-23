# Strategy: Actor-Based Scripts Migration

## Objective
Re-implement the `execute` orchestration layer and its constituent scripts in Rust using the `ractor` actor framework.

## Roadmap
1. [ ] **Phase 1: Foundation**
    - Add `ractor` dependency to `mentci-aid`.
    - Define the core `Actor` traits for symbolic manipulation.
2. [ ] **Phase 2: Orchestration**
    - Refactor `execute.rs` to spawn an `Orchestrator` actor.
    - Implement message routing from CLI to internal actors.
3. [ ] **Phase 3: Migration (The Scripts)**
    - [ ] `RootGuard`
    - [ ] `ReferenceGuard`
    - [ ] `ProgramVersion`
    - [ ] `SubjectUnifier`
4. [ ] **Phase 4: Guideline Integration**
    - Update `SEMA_RUST_GUIDELINES.md` with actor-first concurrency rules.

## Architectural Notes
- **Erlang Lineage:** `ractor` aligns with the project's emphasis on actors and single-ownership.
- **Bootstrapping:** The migration reduces reliance on `bb` for core repository maintenance.
