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
2. Keep all application logic in **Rust** (outside Nix infra concerns).
3. Keep schemas **component-local** and messages hash-synced.
4. Prefer **structural editing** over brittle text patching.
5. Keep commits atomic, auditable, and protocol-compliant.

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

## Sema Implementation Flow

### 1) Model the Data First
- Define or update sidecar data (`.edn` preferred for text authority).
- Use Cap'n Proto schemas for transport/state contracts.
- **Contract Isolation:** Inter-component communication channels must be defined as independent Cap'n Proto/EDN contracts, preparing for a DVCS architecture where contracts live in dedicated repositories.
- Do not hardcode paths, env names, model IDs, or prompt payloads in logic.

### 2) Keep Logic in Rust
- Implement behavior in Rust objects/traits.
- **Component Boundaries:** Treat every component as a strict boundary (future standalone `jj` repository). They must interact only through schema-validated channels.
- Use one-object-in / one-object-out patterns where practical.
- Avoid ad-hoc shell/Python/Clojure for runtime logic.

### 3) Edit Structurally
- Use `structural_edit` for source transformations when feasible.
- If structural matching fails, use precise `edit` with minimal scope.
- Record structural-edit limitations in research when encountered.

### 4) Verify in Full Workspace Context
- Run targeted checks first, then `cargo check --workspace`.
- Validate warning hygiene and generated-code boundaries.
- For generated files, place lint allowances on wrapper module boundaries (never patch generated output directly).

### 5) Commit Protocol
- Commit each intent atomically.
- Every commit message MUST follow the "Standard Intent Header":
  ```markdown
  ## Original Prompt
  <The exact user prompt that initiated this logical change>

  ## Context
  <High-level architectural/session status, e.g., "Salvaging history" or "Evolving Datalog substrate">

  ## Summary
  <Bullet points of specific logical and physical changes>
  ```
- **Generalization Rule:** Keep specific implementation details, transient commit hashes, or temporary session identifiers out of formal documentation/skills unless they are being used as a demonstrable example of a low-level technical property (e.g., hash algorithms or encoding schemes).
- **Session Commits:** For final session finalization, use the `session:` prefix and include "Logical Changes" and "Validation" sections.
- Push `dev` and verify local/remote bookmark alignment using the **Bookmark Movement Protocol**.

## Anti-Patterns (Forbidden)
- Hardcoding configuration data into Rust logic.
- Regex/sed/python patching for source mutation.
- Editing generated artifacts as source-of-truth.
- Leaving workspace with unresolved warnings when claiming completion.

## Completion Checklist
- [ ] Data authority is externalized and current.
- [ ] Rust implementation aligns with Sema object conventions.
- [ ] Structural edit used where practical; fallbacks documented if needed.
- [ ] `cargo check --workspace` clean.
- [ ] Research artifact updated for non-trivial findings.
- [ ] Intent commits + final session metadata committed and pushed.
