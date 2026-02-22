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
- protocol/record-first expression of domain behavior in Clojure.
- reduction of redundant unary map wrappers (for example `[:map [:raw :string]]`).
- No wire-format or runtime behavior changes in this phase.

## 2.1 Review Outcome (Methods-First Alignment)
The strategy is feasible, but it must not optimize only syntax surface. It also
needs a structural mirror of `Core/SEMA_RUST_GUIDELINES.md`:
- behavior in an existing semantic method domain belongs in protocol methods;
- free functions remain orchestration wrappers;
- syntax sugar (`defobj`, `fn`) should support protocol-based object methods
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

### 3.2 Function Signature Sugar
Current:
```clojure
(defn* to-lines [:=> [:cat Source] [:vector :string]] [input] ...)
```

Target (input-first form):
```clojure
(fn to-lines Source [input] ...)
```

Expansion:
- output schema is never silently weakened to `:any`
- macro uses one of two safe modes:
1. explicit output:
```clojure
(fn to-lines Source [:vector :string] [input] ...)
```
2. deterministic inferred output (by naming contract):
```clojure
;; Source => Lines (must exist, else compile-time error)
(fn to-lines Source [input] ...)
```

## 4. Implementation Plan
1. Add new macros in `scripts/lib/malli.clj`:
- `defobj` for map schema sugar
- `fn` for single-input signature sugar with explicit or deterministic output typing
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

*The Great Work continues.*
