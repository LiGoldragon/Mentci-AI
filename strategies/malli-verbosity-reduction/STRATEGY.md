# Strategy: Malli Verbosity Reduction

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Developer Efficiency + Schema Clarity)

## 1. Goal
Reduce repetitive Malli authoring noise while preserving:
- Sema single-object contracts
- explicit schema truth
- instrumentation compatibility

## 2. Scope
- Clojure script schemas under `scripts/`.
- `defn*` function-signature ergonomics in `scripts/lib/malli.clj`.
- No wire-format or runtime behavior changes in this phase.

## 3. Proposed Syntax Targets
### 3.1 Schema Declaration Sugar
Current:
```clojure
(def ParseInput
  [:map
   [:raw :string]])
```

Target:
```clojure
(defobj ParseInput
  {:raw :string})
```

Expansion:
- `defobj` expands to canonical Malli vector schema (`[:map ...]`).
- Internal canonical schema remains unchanged for validation/runtime.

### 3.2 Function Signature Sugar
Current:
```clojure
(defn* parse-descriptions [:=> [:cat ParseInput] [:vector :string]] [input] ...)
```

Target (input-first form):
```clojure
(defn1 parse-descriptions ParseInput [input] ...)
```

Expansion:
- output schema is never silently weakened to `:any`
- macro uses one of two safe modes:
1. explicit output:
```clojure
(defn1 parse-descriptions ParseInput [:vector :string] [input] ...)
```
2. deterministic inferred output (by naming contract):
```clojure
;; ParseInput => ParseOutput (must exist, else compile-time error)
(defn1 parse-descriptions ParseInput [input] ...)
```

## 4. Implementation Plan
1. Add new macros in `scripts/lib/malli.clj`:
- `defobj` for map schema sugar
- `defn1` for single-input signature sugar with explicit or deterministic output typing

2. Keep `defn*` fully supported.
- No forced migration.
- `defn*` remains canonical fallback for complex arities/schemas.

3. Pilot conversion on one low-risk script:
- `scripts/session_guard/main.clj` or `scripts/reference_guard/main.clj`

4. Validate with:
- `bb scripts/validate_scripts/main.clj`
- script-local execution tests

5. If pilot is stable, draft migration guide for broader rollout.
6. Add compile-time failure for unresolved inferred output types.

## 5. Non-Goals (Phase 1)
- Removing map field keys (`:raw`, `:path`, etc.) from object schemas.
- Auto-inferring output schemas from function bodies.
- Refactoring all existing scripts in one pass.

## 6. Success Criteria
- Pilot script shows measurable schema/signature line-count reduction.
- Validation and instrumentation behavior unchanged.
- Readability remains acceptable to maintainers during code review.

## 7. Failure Modes and Mitigation
1. **Macro opacity during debugging**
- Mitigation: document macro expansion examples in `scripts/lib/malli.clj`.

2. **Experimental dependency instability**
- Mitigation: avoid hard dependency on `malli.experimental.lite`; implement local expansion logic first.

3. **Over-aggressive migration**
- Mitigation: staged adoption by script, with rollback path to `defn*`.

*The Great Work continues.*
