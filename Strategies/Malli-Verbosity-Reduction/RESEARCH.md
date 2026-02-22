# Research: Malli Verbosity Reduction

## Objective
Evaluate whether Mentci Clojure/Malli typing can be made less verbose for:
- object schema declarations
- function signatures

## Current Pain Points
1. Repetitive map declaration boilerplate:
```clojure
(def ParseInput
  [:map
   [:raw :string]])
```

2. Repetitive function schema wrapper boilerplate:
```clojure
(defn* parse-descriptions [:=> [:cat ParseInput] [:vector :string]] [input] ...)
```

## Findings
1. Malli supports multiple schema syntaxes.
- Vector syntax (current default).
- Map/AST syntax (`{:type ...}`), but upstream notes this is internal for persistence.
- Lite syntax (`malli.experimental.lite`) for concise schema authoring.

2. Lite syntax can express maps without explicit `:map` in user code.
- Example pattern from upstream:
```clojure
(l/schema {:x :int
           :y [:maybe :string]})
;; => [:map [:x :int] [:y [:maybe :string]]]
```

3. Function schema syntax still resolves to `[:=> [:cat ...] ...]`.
- Upstream function instrumentation APIs use `m/=>` with `[:=> [:cat ...] ...]`.
- Therefore, reducing function-signature verbosity requires a local macro layer, not only Malli config changes.

## Feasibility Assessment
1. Removing `:map` is feasible via local sugar.
- Two viable approaches:
- local macro around vector syntax
- `malli.experimental.lite` wrapper

2. Removing field keys (for example `:raw`) is not generally feasible without losing object semantics.
- Key names are the schema-level contract for map objects.
- Key removal is only possible by changing representation (tuple/vector/scalar), which changes API shape and breaks existing call sites.

3. Reducing function signature noise is feasible with a local macro convention, but output typing must remain explicit or deterministic.
- Example target authoring form:
```clojure
(defn1 parse-descriptions ParseInput [input]
  ...)
```
- Expansion strategy:
- **Do not default to `:any`** (weakens schema contract).
- Preferred options:
- explicit output form in macro call
- deterministic output inference rule (for example `ParseInput -> ParseOutput`, failing if unresolved)
- emitted schema remains fully explicit (`[:=> [:cat ParseInput] ParseOutput]`)

## Risk Notes
1. `malli.experimental.lite` is experimental upstream; hard dependency could increase long-term maintenance risk.
2. Over-sugar can obscure actual schema shape during debugging.
3. Macro migration across all scripts should be incremental, not big-bang.
