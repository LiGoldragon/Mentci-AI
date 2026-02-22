# Strategy: Project Hardening (Tests, Schemas, Reorg)

**Linked Goal:** `Goal 0: mentci-aid Stabilization`

## 1. Goal
Achieve full structural and testing coverage for all scripts and tools in the Mentci-AI ecosystem.

## 2. Reorganization: Script Autonomy
Move every script in `scripts/` to its own directory:
- `scripts/<name>/main.clj`
- `scripts/<name>/TESTING_CONTEXT.md`
- `scripts/<name>/schema.capnp`
- `scripts/<name>/test.clj` (or equivalent)

## 3. Schema is Sema
Define Cap'n Proto schemas for every tool's input and output objects. This hardens the " Russian Doll" data passing model.

## 4. Testing Context
Every tool must have a `TESTING_CONTEXT.md` defining:
- **Coverage State:** Current unit/integration test status.
- **Verification Commands:** How to run the tests.
- **Mocking Strategy:** How external dependencies (like `jj` or `nix`) are handled during tests.

## 5. Implementation Roadmap
1.  **Phase 1 (Structural)**: Reorganize `scripts/`.
2.  **Phase 2 (Semantic)**: Write `.capnp` specs.
3.  **Phase 3 (Quality)**: Implement missing tests and `TESTING_CONTEXT.md`.

*The Great Work continues.*
