# Strategy: Malli Verbosity Reduction

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Developer Efficiency + Schema Clarity)

## 1. Goal
Reduce repetitive Malli authoring noise while preserving:
- Sema single-object contracts
- explicit schema truth
- instrumentation compatibility
- methods-on-objects-first semantics (mirrored from Rust trait-domain rule)
- domain-noun object naming (no `ParseInput`, no type-redundant names like `InputText`)
- maximum signal with minimal syntax noise in Clojure authoring

## 2. Scope
- Clojure script schemas under `scripts/`.
- `defn*` function-signature ergonomics in `scripts/lib/malli.clj`.
- `main` entrypoint ergonomics in `scripts/lib/malli.clj`.
- protocol/record-first expression of domain behavior in Clojure.
- reduction of redundant unary map wrappers (for example `[:map [:raw :string]]`).
- No wire-format or runtime behavior changes in this phase.

## 2.1 Review Outcome (Methods-First Alignment)
The strategy is feasible, but it must not optimize only syntax surface. It also
needs a structural mirror of `Core/SEMA_RUST_GUIDELINES.md`:
- behavior in an existing semantic method domain belongs in protocol methods;
- free functions remain orchestration wrappers;
- syntax sugar (`defobj`, `main`, `impl`) should support protocol-based object methods
  rather than reinforce ad-hoc free-function style.

## 3. Proposed Syntax Targets
### 3.1 Schema Declaration Sugar
Current:
```clojure
(def Source
  :string)
```

Target:
```clojure
(defobj Source
  :string)
```

Expansion:
- `defobj` supports scalar and map forms.
- For unary payloads, prefer scalar schemas over single-member maps.
- Internal canonical schema remains unchanged for validation/runtime.

### 3.2 Entrypoint Sugar (Implemented)
Current:
```clojure
(defn* -main [:=> [:cat Input] :any] [input] ...)
```

Implemented:
```clojure
(main Input [input] ...)
```

Implemented auto-arg shorthand:
```clojure
(main Input
  ...)
```
Where arg is derived from type symbol by lowercasing first letter
(`Input -> input`).

Expansion:
- expands to `defn* -main` with inferred `:any` output in concise mode.
- accepts Malli lite input forms in concise mode and compiles them to schema types.
- supports explicit function schema form for stricter contracts.

Lite example:
```clojure
(main {:args [:vector :string]} [input] ...)
```

Example explicit schema:
```clojure
(main [:=> [:cat Input] :any] [input] ...)
```

## 4. Implementation Plan
1. Add new macros in `scripts/lib/malli.clj`:
- `defobj` for map schema sugar
- `main` for concise `-main` declaration (implemented)
- optional `defscalar` alias for unary object declarations

2. Keep `defn*` fully supported.
- No forced migration.
- `defn*` remains canonical fallback for complex arities/schemas.

3. Add methods-first helper path:
- Introduce optional `impl` macro shape for protocol method schemas where useful:
```clojure
(impl to-lines Source Lines [source] ...)
```
- Expansion target remains explicit Malli-compatible function schema.

4. Pilot conversion on one low-risk script:
- `scripts/session_guard/main.clj` or `scripts/reference_guard/main.clj`
- Ensure pilot includes at least one protocol/domain method extraction.

Pilot executed:
- Converted `Components/scripts/root_guard/main.clj` to `main` macro with `Input` naming.
- Verified runtime and script validation for this candidate.

5. Validate with:
- `bb scripts/validate_scripts/main.clj`
- script-local execution tests

6. If pilot is stable, draft migration guide for broader rollout.
7. Add compile-time failure for unresolved inferred output types.
8. Add compile-time failure for flow/redundant object names:
- reject `*Input`/`*Output` flow-role suffixes in object schemas.
- reject type-redundant names like `*Text` when schema is scalar `:string`.
9. Add lint guard for unary map redundancy:
- reject or warn on single-member map schemas unless annotated as required for compatibility.
10. Add context-local entrypoint naming guard:
- in `main.clj`, prefer schema name `Input`; flag `MainInput` and similar redundant restatements.

## 5. Non-Goals (Phase 1)
- Removing map field keys from multi-field object schemas.
- Auto-inferring output schemas from function bodies.
- Refactoring all existing scripts in one pass.
- Replacing all orchestrator free functions with protocols where no domain object exists.

## 6. Success Criteria
- Pilot script shows measurable schema/signature line-count reduction.
- Validation and instrumentation behavior unchanged.
- Readability remains acceptable to maintainers during code review.
- Methods-first conformance check passes: domain behavior is protocol/record anchored.
- Single-member map usage is reduced to compatibility-only cases with explicit rationale.

## 7. Failure Modes and Mitigation
1. **Macro opacity during debugging**
- Mitigation: document macro expansion examples in `scripts/lib/malli.clj`.

2. **Experimental dependency instability**
- Mitigation: avoid hard dependency on `malli.experimental.lite`; implement local expansion logic first.

3. **Over-aggressive migration**
- Mitigation: staged adoption by script, with rollback path to `defn*`.

## 8. Implementation Feasibility Review

### Verdict
Partially feasible. Core direction is implementable, but current placeholder naming
(`fn`) is not executable in Clojure as written.

### Blockers
1. **General `fn` macro alias remains blocked**
- The symbol `fn` is a language special form and cannot be repurposed as a project
  macro entrypoint in normal call position.
- Replaced in practice by implemented `main` macro for entrypoint concision.

2. **Validator coupling to `defn*`**
- `Components/scripts/validate_scripts/main.clj` currently enforces presence of
  `defn*` + `mentci.malli` usage in scripts.
- Any migration away from `defn*` requires validator evolution.

3. **Missing implementation scaffolding**
- `Components/scripts/lib/malli.clj` currently provides only `defn*` and `enable!`.
- `defobj` / `impl` / scalar lint guards are not implemented yet.

### Feasible Path
1. Keep `defn*` as baseline.
2. Replace `fn` placeholder with a non-colliding macro name (for example `fn*`).
3. Implement `defobj` and optional `impl` in `Components/scripts/lib/malli.clj`.
4. Update validator to accept `defn*` and/or new sanctioned macro forms.
5. Pilot on one script, then expand incrementally.

### Scope Classification
- `defobj`: feasible now.
- `impl`: feasible now.
- `fn` (exact spelling): not feasible (special form collision).
- `main`: implemented and feasible now for entrypoints.
- `main` + Malli lite input form: implemented and feasible now.
- unary-map noise reduction: feasible with boundary-aware migration.

*The Great Work continues.*
