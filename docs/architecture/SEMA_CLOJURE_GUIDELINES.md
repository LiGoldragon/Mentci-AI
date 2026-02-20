# Sema Object Style — Clojure + Malli

This document defines the mandatory Sema object rules for Clojure and its extensions
(Malli, Babashka). The rules are structural. Violations indicate category error, not style.

## Primary Rules

1. **Single Object In/Out**
   Every function accepts exactly one explicit object argument and returns exactly one
   object. When multiple inputs or outputs are required, define a new object.

2. **Everything Is an Object**
   Reusable behavior belongs to named objects (schemas + namespaces). Free functions
   exist only as orchestration shells.

3. **Schema Is Sema**
   Schemas are the authoritative truth. Encodings are incidental.

## Clojure Object Model

### Object Definition
Sema objects are EDN maps with a Malli schema. Object identity is the schema name.

```clojure
(def Greeting
  [:map
   [:text :string]])
```

### Function Signature Rule
Every function takes a single input object and returns a single output object.

```clojure
(def GreetInput
  [:map
   [:name :string]])

(def GreetOutput
  [:map
   [:message :string]])

(defn greet [input]
  (when-not (m/validate GreetInput input)
    (throw (ex-info "Invalid greet input" {:errors (me/humanize (m/explain GreetInput input))})))
  {:message (str "Hello " (:name input))})
```

### Direction Encodes Action
Use `from_*`, `to_*`, `into_*` when construction or emission is implied.

```clojure
(def ConfigInput
  [:map
   [:path :string]])

(def Config
  [:map
   [:raw :string]])

(defn config-from-file [input]
  (when-not (m/validate ConfigInput input)
    (throw (ex-info "Invalid config-from-file input" {:errors (me/humanize (m/explain ConfigInput input))})))
  {:raw (slurp (:path input))})
```

## Malli Requirements

*   Every function must define an input schema and validate it.
*   Output schemas should be defined and validated when outputs cross module boundaries.

## Namespace Discipline

*   A namespace is a semantic layer.
*   Related objects are grouped in a single namespace (e.g., `mentci.intent`).
*   Do not duplicate meaning in function names. The namespace conveys the noun.

## Documentation Protocol

Documentation is impersonal, timeless, and precise. Document only non-boilerplate behavior.
