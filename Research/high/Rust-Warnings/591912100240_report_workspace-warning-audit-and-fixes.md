# Workspace Rust Warning Audit, Research Context, and Fix Strategy

## Scope
- Enumerate warnings across all Rust workspace components.
- Identify source context for each warning.
- Apply bleeding-edge Rust warning policy decisions.
- Document structural editing usage quality, feedback, and recommendations.

## Audit Command
- `cargo check --workspace`

## Initial Warning Set
1. `mentci-launch` (lib): 6 warnings
   - Type: `unused_parens`
   - Origin: generated file under `target/.../out/mentci_capnp.rs`
2. `mentci-dig` (bin): 1 warning
   - Type: `unused_imports`
   - Item: `DigStrategy`
   - File: `Components/mentci-dig/src/main.rs`
3. `mentci-user` (lib): 1 warning
   - Type: `unused_imports`
   - Item: `capnp::serialize`
   - File: `Components/mentci-user/src/lib.rs`

Total: 8 warnings.

## Research Context (Linkup-backed)
Questions researched:
1. Best practice for warnings in generated Rust code included via wrapper modules.
2. Bleeding-edge CI warning policy for multi-crate workspaces.

Convergent recommendations from research:
- Do not edit generated files directly.
- Scope lint allowances at generated boundary modules (`mod` wrapper), not crate-wide.
- Avoid global `deny(warnings)` as a brittle default under evolving compiler lint sets.
- Prefer explicit lint hygiene in source + selective suppression for generated code.

## Fix Decisions and Rationale

## A) `mentci-dig` unused import
- Fix: remove `DigStrategy` from import list.
- Reason: real source warning, deterministic cleanup, no behavior change.

## B) `mentci-user` unused import
- Fix: remove `use capnp::serialize;`.
- Reason: dead import in hand-written library code.

## C) `mentci-launch` generated lint noise
- Fix: add `#[allow(unused_parens)]` on generated-wrapper modules in `Components/mentci-launch/src/lib.rs`.
- Reason: warning originates in generated code; wrapper-level allowance is narrow and durable.

## Structural Edit Usage (Maximized) + Feedback
Structural edits performed:
- `mentci-dig/src/main.rs`: import rewrite using structural_edit.
- `mentci-user/src/lib.rs`: unused import removal using structural_edit.

Observed limitation:
- structural pattern matching for Rust module declarations (`pub mod ... { include!(...) }`) did not match reliably for wrapper-attribute insertion in `mentci-launch/src/lib.rs`.
- fallback to precise textual edit was required for the wrapper attribute insertion.

Feedback / Suggestions for structural_edit evolution:
1. Improve Rust module declaration matching support for `pub mod <name> { ... }` with macro includes.
2. Add optional AST-debug mode to show nearest matched node kind when pattern misses.
3. Support “insert attribute before matched item” helper to avoid replacement-based workarounds.
4. Add dry-run JSON diagnostics output (matched spans, replacements) for CI verification tooling.

## Post-Fix Verification
- Command: `cargo check --workspace`
- Result: zero warnings, zero errors.

## Bleeding-Edge Rust Policy Recommendation
- Keep workspace warning-free in hand-written code.
- Use narrow lint allowances only at generated boundaries.
- Keep `deny(warnings)` out of source-level global policy; enforce focused lint gates in CI.
- Re-run full workspace `cargo check` after each warning-fix commit.
