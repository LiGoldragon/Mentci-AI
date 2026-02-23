# Strategy: Actor and Agentic Library Integration

## Objective
Establish a unified framework for actor-based concurrency and agentic flows within the Mentci-AI ecosystem.

## Execution Plan
1. [ ] **Phase 1: Implementation of `execute` Orchestrator**
    - Use `ractor` to build the new root actor for CLI dispatch.
2. [x] **Phase 2: Formalize Rust Guidelines**
    - Add the "Symbolic Actors" rule to `Core/SEMA_RUST_GUIDELINES.md`.
3. [ ] **Phase 3: Agentic Flow Exploration**
    - Prototype a `TheBookOfGoldragon` analyst using `Rig`.

## Decision Log
- **Concurrency:** `ractor` (Erlang lineage).
- **Agents:** `Rig` (Modular LLM standard).
- **Pipelines:** `GraphBit` (DAG-driven consistency).
