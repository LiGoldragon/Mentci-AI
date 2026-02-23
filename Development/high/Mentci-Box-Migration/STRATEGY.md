# Strategy: Mentci-Box Migration

## Objective
Decouple sandbox logic from `mentci-aid` into a specialized, high-reliability bootstrap component `mentci-box`.

## Roadmap
1. [ ] Create component directories.
2. [ ] Define `mentci-box-lib` API (Sema style).
3. [ ] Implement `mentci-box` CLI.
4. [ ] Verify with smoke tests.
5. [ ] (Optional) Refactor `mentci-aid` to use the new library.

## Architectural Notes
*   **Durability:** `mentci-box` is intended to be a stable anchor for bootstrapping. It should have minimal dependencies.
*   **Ontology:** 
    *   `Sandbox` (Object): Represents the isolated execution state.
    *   `run`: Action that executes a command within the sandbox.
