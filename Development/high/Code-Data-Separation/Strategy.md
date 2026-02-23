# Strategy: Code-Data Separation

## Objective
Enforce strict separation of logic and data across the Mentci-AI repository by updating high-authority guidelines and refactoring components to use sidecar configuration files.

## Roadmap
1. [ ] **Phase 1: Guideline Hardening**
    - Update `Core/ARCHITECTURAL_GUIDELINES.md` with the Externalization Rule.
    - Update `Core/SEMA_RUST_GUIDELINES.md` with the Sidecar Pattern.
2. [ ] **Phase 2: Core Refactoring**
    - Refactor `execute` actors to load all guard rules from sidecar files.
    - Refactor `mentci-fs` to externalize the `Aski-FS` ontology.
3. [ ] **Phase 3: Tooling Support**
    - Implement a `ConfigurationLoader` actor/library in Rust.
    - Standardize on `capnp` for binary-to-struct mapping.

## Decision Log
- **Authority:** High (Direct author intent from Torremolinos note).
- **Preferred Format:** `capnp` > `edn` > `json`.
