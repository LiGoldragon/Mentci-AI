---
name: sema-programmer
description: Use when implementing or refactoring Rust/Nix/Cap'n Proto work under Sema Object Style, Logic-Data separation, and component-local schema policy
---

> **Related skills:** `/skill:independent-developer`, `/skill:test-driven-development`, `/skill:systematic-debugging`, `/skill:verification-before-completion`

# Sema Programmer

## Overview
This skill enforces **Sema-grade implementation quality** for Mentci-AI. It is a lower-level technical building block for the `/skill:independent-developer`.

Primary goals:
1. Preserve **Logic-Data Separation**.
2. Keep all application logic in **Rust** (outside Nix infra concerns). No ad-hoc scripts or Python.
3. Keep schemas **component-local** and messages hash-synced.
4. Keep commits atomic, auditable, and protocol-compliant.
5. **Universal Object Specification:** Embrace the Object approach as universal. Everything is an object, and even free functions are really just object methods in an orchestration shell.

## Preconditions

Before editing code:
1. Run `jj status`.
2. If tree is dirty, isolate and commit existing intent first.
3. Confirm data authority artifact exists (`.edn` and/or `.bin` sidecar) before coding defaults.
4. For external tooling/ecosystem claims, run quick Linkup validation (`linkup_web_search`/`linkup_web_answer`) before asserting status or maturity.

## Sema Implementation Flow

### 0) Modern Style Boundary Rule
- When integrating legacy or externally generated code (e.g., Cap'n Proto), wrap the inclusion in a dedicated module with surgical lint allowances:
  ```rust
  #[allow(unused_parens, dead_code, unused_imports, non_snake_case, unused_qualifications)]
  pub mod [name]_capnp {
      include!(concat!(env!("OUT_DIR"), "/[name]_capnp.rs"));
  }
  ```
- This isolates the generator's style from the Actor's domain logic.
- Every component must follow the 'Mentci-STT' fractal structure: `src/` for logic, `contracts/` for EAV/Cap'n Proto definitions, and `tests/` for isolated suites.

### 1) Universal Object Approach & Trait-Domain Rule
- **Everything Is an Object:** Reusable behavior belongs to named types or traits. Free functions exist only as orchestration shells in binaries.
- **Single Object In/Out:** Every method accepts at most *one explicit object argument* and returns *exactly one object*. If multiple inputs/outputs are needed, define a new object.
- **Complexity Guard:** Favor useful and simple object-based logic over difficult-to-reason-about systems. Implementation details should be encapsulated within clear object boundaries and traits.
- **Direction Encodes Action:** Prefer `from_*`, `to_*`, `into_*`. Avoid verbs like `read`, `write`, `load`, `save` when direction conveys meaning.
- **Trait-Domain Rule:** Any behavior in the semantic domain of an existing trait must be expressed as a trait implementation (e.g., `FromStr`), rather than a new inherent method.

### 2) Model the Data First (Sidecar Pattern)
- Define or update sidecar data (`.edn` or `capnp` preferred for external data).
- Use Cap'n Proto schemas for transport/state contracts. **Schema Is Sema:** Transmissible objects are defined in schemas.
- **Contract Isolation:** Inter-component communication channels must be defined as independent Cap'n Proto/EDN contracts, preparing for a DVCS architecture where contracts live in dedicated repositories.
- **Logic-Data Separation:** Implementation files must not contain hardcoded paths, regexes, or numeric constants. Do not hardcode env names, model IDs, or prompt payloads in logic.
- **Init Envelope Purity (Very High Importance):** Runtime launch and initialization configuration must arrive as *one Cap'n Proto init message object*. Environment variables are not used as domain-state inputs.

### 3) Keep Logic in Rust & Actor-First Concurrency
- Implement behavior in Rust objects/traits. No Python, Clojure, or ad-hoc shell for runtime logic.
- **Actor-First Concurrency:** All multi-step symbolic transformations and concurrent tasks must be implemented as supervised actors using the `ractor` framework.
- **Component Boundaries:** Treat every component as a strict boundary. They must interact only through schema-validated channels (symbolic messaging).

### 4) Verify in Full Workspace Context
- Run targeted checks first, then `cargo check --workspace`.
- Validate warning hygiene and generated-code boundaries.

### 5) Commit Protocol
- Commit each intent atomically.
- **Header Standard:** All commit and session documentation MUST follow the template defined in the `independent-developer` skill (Original Prompt, Context, Summary, Validation).
- **Prompt Fidelity:** The agent MUST prioritize using the exact original prompt from the user.
- Push `dev` and verify local/remote bookmark alignment using the **Bookmark Movement Protocol**.
- **Session Handover:** Always end the interaction by creating a new empty commit (`jj new`) to leave a clean, empty worktree for the next interaction.

## Anti-Patterns (Forbidden)
- Hardcoding configuration data into Rust logic.
- Regex/sed/python patching for source mutation.
- Editing generated artifacts as source-of-truth.
- Leaving workspace with unresolved warnings when claiming completion.

## Completion Checklist
- [ ] Data authority is externalized and current.
- [ ] Rust implementation aligns with Sema object conventions (Single Object In/Out, Trait domains).
- [ ] `cargo check --workspace` clean.
- [ ] Research artifact updated for non-trivial findings.
- [ ] Intent commits + final session metadata committed and pushed.
- [ ] Empty commit created at the end of the session (`jj new`).