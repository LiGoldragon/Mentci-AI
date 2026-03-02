# Strategy: Aski Syntax Research

## Objective
Implement `aski-lib` (Rust library) and `aski-cli` (Rust CLI) to provide a high-fidelity reader and translator for the Aski structured data format.

## Roadmap
1. [ ] **Phase 1: Foundation**
    - Create `Components/aski-lib` and `Components/aski-cli`.
    - Define `AskiValue` core enum.
2. [ ] **Phase 2: Parsing**
    - Implement the Aski parser (Clojure-like with casing rules).
    - Handle comments and metadata.
3. [ ] **Phase 3: Translation**
    - Implement `serde` support for `AskiValue`.
    - Add JSON/EDN/TOML output drivers.
4. [ ] **Phase 4: CLI Interface**
    - Build `aski-cli` for format conversion.

## Architectural Notes
- The reader must be "sophisticated"—it doesn't just parse text; it resolves **semantic weight** based on filesystem-style casing rules.
- `aski-lib` should be the universal translation bridge for Sema objects.
