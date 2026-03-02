# Strategy: Clojure Deprecation and Migration to Rust+Capnp-Spec

## 1. Goal
Lower the importance of Clojure in the Mentci-AI project. Clojure is no longer permitted for writing new code and is considered a legacy language. However, its syntax remains an inspiration for the future Aski language. All existing Clojure code must be systematically migrated to sema-style Rust+capnp-spec.

## 2. Rationale
- **Performance and Type Safety**: Rust provides superior durability, performance, and type-safety through the actor model.
- **Data Validation**: Cap'n Proto specifications (`capnp-spec`) provide robust, strict schema definitions across process boundaries.
- **Aski Inspiration**: Clojure's syntax provides excellent LLM-friendly structuring and semantic alignment for symbolic manipulation, laying the groundwork for the Aski language, but it should not be the runtime target.

## 3. Modification of Guidance
The following guidelines have been or must be updated to reflect this demotion:
- `Core/AGENTS.md`: Remove the "Clojure (Babashka) Mandate" and emphasize that Clojure is "Legacy only". Add a migration mandate.
- `Core/ARCHITECTURAL_GUIDELINES.md`: Redefine Clojure as "Inspiration Only" rather than a prototyping layer. Emphasize Rust actor models and Cap'n Proto.
- `Core/SEMA_CLOJURE_GUIDELINES.md`: Add a prominent deprecation notice at the top of the file, explaining that new features should not be added and the file serves only to maintain legacy scripts until their migration.

## 4. Migration Plan (Clojure to Rust+Capnp-Spec)
1.  **Inventory Assessment**: Execute `find Components/scripts -name "*.clj"` to identify all legacy Babashka/Clojure scripts.
2.  **Schema Translation (Malli -> Capnp)**: For each script, convert its Malli input/output schemas into `capnp` specifications (placed in `Components/schema/`).
3.  **Rust Re-implementation**:
    - Re-implement the script logic as a Rust actor (using the `ractor` framework) within a new or existing Rust workspace component.
    - Ensure the logic follows strict "Sema Object Style" (`Core/SEMA_RUST_GUIDELINES.md`), utilizing `capnp-spec` for input/output messages.
4.  **Integration & Testing**:
    - Swap the `execute` orchestrator target in the Nix `shellHook` from the Clojure script to the Rust binary.
    - Run the existing test suites (`test_deps.clj`, `test.clj` equivalents) to verify parity.
5.  **Decommissioning**:
    - After verifying stable execution in the `dev` branch, delete the legacy `*.clj` file and its tests.
    - Update `Library/RestartContext.md` and `Outputs/Logs/ReleaseMilestones.md` to reflect the removal of the specific Clojure component.

## 5. End State
The project will execute all orchestration, tasks, and system glue entirely in Rust using Sema Object style and Cap'n Proto schema types. The Aski syntax will eventually emerge for higher-level domain specifications, drawing inspiration from Clojure's syntax without relying on its runtime.