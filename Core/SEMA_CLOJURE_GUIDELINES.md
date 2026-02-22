# Sema Object Style — Clojure + Malli

This document defines the mandatory Sema object rules for Clojure and its extensions
(Malli, Babashka). The rules are structural. Violations indicate category error, not style.

## Primary Rules

1. **Single Object In/Out**
   Every function accepts exactly one explicit object argument and returns exactly one
   object. When multiple Inputs or outputs are required, define a new object.

2. **Everything Is an Object**
   Reusable behavior belongs to named objects (schemas + namespaces). Free functions
   exist only as orchestration shells.

3. **Schema Is Sema**
   Schemas are the authoritative truth. Encodings are incidental.

4. **Methods on Objects First**
   Domain behavior must be expressed as protocol methods implemented by object-bearing
   types/records. Namespace free functions are orchestration shells, not domain homes.

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

### Methods-on-Objects-First Rule
When behavior belongs to an object domain, define a protocol and implement it on a
named object type (usually `defrecord`).

```clojure
(def ParseInput
  [:map
   [:raw :string]])

(def ParseOutput
  [:map
   [:lines [:vector :string]]])

(defprotocol ParseDescriptions
  (to-lines [this]))

(defrecord ParseRequest [raw]
  ParseDescriptions
  (to-lines [this]
    {:lines (->> (clojure.string/split-lines (:raw this))
                 (map clojure.string/trim)
                 (remove clojure.string/blank?)
                 vec)}))
```

Mapping to Rust trait-domain rule:
- Rust `trait`/`impl` corresponds to Clojure `defprotocol` + `extend-type` or `defrecord` impl.
- If a semantic behavior has a protocol domain, do not bypass it with unrelated helper fns.

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

### Preferred Validation Style (m/=> + instrumentation)

Use Malli function schemas with instrumentation instead of manual `m/validate`.

```clojure
(m/=> greet [:=> [:cat GreetInput] GreetOutput])
(defn greet [input]
  {:message (str "Hello " (:name input))})
```

Enable instrumentation once in the entrypoint:

```clojure
(require '[malli.instrument :as mi])
(mi/instrument!)
```

### Project Macro: defn*

Use the local `defn*` macro to reduce noise. It expands to `m/=>` plus `defn`.

```clojure
(defn* greet [:=> [:cat GreetInput] GreetOutput]
  [input]
  {:message (str "Hello " (:name input))})
```

## Namespace Discipline

*   A namespace is a semantic layer.
*   Related objects are grouped in a single namespace (e.g., `mentci.intent`).
*   Do not duplicate meaning in function names. The namespace conveys the noun.
*   Protocol names and object names carry durable semantics; helper wrappers remain thin.

## Documentation Protocol

Documentation is impersonal, timeless, and precise. Document only non-boilerplate behavior.
