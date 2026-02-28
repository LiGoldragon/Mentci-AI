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
- Every commit message must include:
  - `## Prompt`
  - `## Context`
  - `## Summary`
- Push `dev` and verify local/remote bookmark alignment.

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
