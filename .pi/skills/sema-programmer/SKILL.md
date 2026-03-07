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
- **Contract Isolation (CRITICAL IMPORTANCE):** Inter-component communication channels MUST be defined as independent Cap'n Proto/EDN contracts in their own dedicated repositories or Component directories (e.g., `Components/criome-contract`). This ensures both components can version-control and use the contract without rebuilding each other on unrelated changes. This is a foundational pillar of the Criome meta-architecture.
- **Logic-Data Separation:** Implementation files must not contain hardcoded paths, regexes, or numeric constants. Do not hardcode env names, model IDs, or prompt payloads in logic.
- **Init Envelope Purity (Very High Importance):** Runtime launch and initialization configuration must arrive as *one Cap'n Proto init message object*. Environment variables are not used as domain-state inputs. This init message should ideally be addressed by a short CA hash (e.g., BLAKE3).

### 3) Keep Logic in Rust & Actor-First Concurrency
- Implement behavior in Rust objects/traits. No Python, Clojure, or ad-hoc shell for runtime logic.
- **Actor-First Concurrency:** All multi-step symbolic transformations and concurrent tasks must be implemented as supervised actors using the `ractor` framework.
- **Component Boundaries:** Treat every component as a strict boundary. They must interact only through schema-validated channels (symbolic messaging).

### 4) Verify in Full Workspace Context
- Run targeted checks first, then `cargo check --workspace`.
- Validate warning hygiene and generated-code boundaries.

### 4.1) Criome/Core Modernization Rules (New Mandatory Lane)
When touching `criome-core`, `criad`, or CriomOS data contracts, apply all rules below.

#### A) Contract Namespacing by File and Module
- Do not use long concatenated contract names as the primary organization method.
- Prefer embedded namespaces through module/file boundaries.
- Required shape:
  - `contracts/<domain>/<kind>.rs` or equivalent nested modules.
  - Access style should read as layered context (e.g., `contracts::node::Proposal`), not mega-identifiers.
- Reason: improves retrieval clarity, keeps names context-local, and reduces semantic duplication.

#### B) Species and Size Must Be Typed
- Species fields are never free-form strings in domain contracts.
- Use explicit enums for species categories (`NodeSpecies`, `UserSpecies`, etc.).
- Size/trust classes should use enum magnitudes (`Min|Low|Med|Max`) unless a true scalar is required.
- If numeric transport is required for downstream consumers (e.g., Nix projection), expose deterministic conversion methods on enums (`to_u8`, `as_str`) rather than storing raw strings everywhere.

#### C) Shared Behavior for Cross-Domain Magnitudes
- If node and user sizes share semantics, unify through one magnitude object/trait.
- Derived predicates (`is_med`, `at_least`) belong on typed objects/traits, not ad-hoc call-site conditionals.

#### D) Keep Nix as Consumer, Not Derivation Authority
- Nix receives already-structured, derivation-ready data projections.
- Do not move primary derivation complexity into Nix expressions.
- Rust/cozo pipeline computes and normalizes; Nix consumes.

#### E) Cozo Script Direction (Transitional Policy)
- Cozo script may be adopted as a first-class messaging/spec lane for Criome workflows.
- Transitional requirement:
  1. maintain existing Cap'n Proto/EDN compatibility where already authoritative,
  2. add reversible translation paths (Cozo <-> Cap'n Proto/typed Rust) before deprecating existing stores.
- Forbidden: abrupt EDN/Cap'n Proto removal without migration bridge and validation report.

#### F) Unstable Component Experimentation Label
- Non-production components (e.g., `criome-core` MVP) must be explicitly marked unstable in central component stability index artifacts.
- Experimental refactors are allowed in unstable lane, but must include:
  - explicit scope boundary,
  - migration notes,
  - tests proving behavior did not regress.

### 4.2) Sub-Agentization Heuristic for Heavy Operations
- If one flow combines 3+ distinct concern sets (e.g., VCS + schema migration + UI tooling), split into specialized sub-tasks/agents.
- Keep top-level agent focused on orchestration and integration, not all low-level operations at once.
- Required for large transcript extraction workflows: persist per-topic commits (research/design/code) instead of one large summary mutation.

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