# Sema Object Style — Clojure + Malli

**DEPRECATION NOTICE**: Clojure is no longer used for writing code in the Mentci-AI project. All new orchestration and tooling must be written in Rust (actor model) + Cap'n Proto. This file remains strictly for maintaining legacy Clojure scripts until their migration. Clojure's primary role in this project is as an inspiration (specifically its syntax) for the future Aski language.

This document defines the mandatory Sema object rules for Clojure and its extensions
(Malli, Babashka). The rules are structural. Violations indicate category error, not style.

## Signal-to-Noise Principle

Clojure in this project is used to maximize semantic signal with minimal syntactic noise.
Representations should be as small as possible while preserving object meaning and future evolution.

## Primary Rules

1. **Single Object In/Out**
   Every function accepts exactly one explicit object argument and returns exactly one
   object. When multiple Sources or outputs are required, define a new object.

2. **Everything Is an Object**
   Reusable behavior belongs to named objects (schemas + namespaces). Free functions
   exist only as orchestration shells.

3. **Schema Is Sema**
   Schemas are the authoritative truth. Encodings are incidental.

4. **Methods on Objects First**
   Domain behavior must be expressed as protocol methods implemented by object-bearing
   types/records. Namespace free functions are orchestration shells, not domain homes.

5. **Object Names Must Be Domain Nouns**
   Object schema names must not encode flow (`ParseInput`, `BuildOutput`) or redundant
   type words (`InputText`, `UserString`) when schema shape already declares type.

6. **Init Envelope Purity (Very High Importance)**
   Runtime launch/init configuration is ingested from one Cap'n Proto init message
   object (or generated equivalent object), not assembled from ad-hoc env vars.

## Clojure Object Model

### Object Definition
Sema objects are Malli-defined schemas. Object identity is the schema name.
Use map objects for multi-field domain boundaries.
Avoid single-member maps for unary payloads when no additional named fields are required.

```clojure
(def Greeting
  [:map
   [:text :string]])
```

Unary payload example (preferred over `[:map [:raw :string]]` when semantics are unary):

```clojure
(def Source
  :string)
```

### Project Macro: struct (Schema Short Style)

Use `struct` for concise schema declarations, analogous to `main`.
Map literals are interpreted as map schemas and scalar forms stay scalar.

Map object example:

```clojure
(struct Remount
  {:name :string
   :sourcePath :string
   :targetPath :string})
```

Unary payload example:

```clojure
(struct Path
  :string)
```

Equivalent legacy forms:
- `{:name :string ...}` -> `[:map [:name :string] ...]`
- `:string` stays `:string`

### Function Signature Rule
Every function takes a single input object and returns a single output object.

```clojure
(def GreetingRequest
  [:map
   [:name :string]])

(def GreetingResponse
  [:map
   [:message :string]])

(defn greet [input]
  (when-not (m/validate GreetingRequest input)
    (throw (ex-info "Invalid greet input" {:errors (me/humanize (m/explain GreetingRequest input))})))
  {:message (str "Hello " (:name input))})
```

### Methods-on-Objects-First Rule
When behavior belongs to an object domain, define a protocol and implement it on a
named object type (usually `defrecord`).

```clojure
(def Source
  :string)

(def Lines
  [:map
   [:lines [:vector :string]]])

(defprotocol ParseDescriptions
  (to-lines [source]))

(extend-type String
  ParseDescriptions
  (to-lines [source]
    {:lines (->> (clojure.string/split-lines source)
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
(def ConfigSource
  :string)

(def Config
  :string)

(defn config-from-file [input]
  (when-not (m/validate ConfigSource input)
    (throw (ex-info "Invalid config-from-file input" {:errors (me/humanize (m/explain ConfigSource input))})))
  (slurp input))
```

## Malli Requirements

*   Every function must define an input schema and validate it.
*   Output schemas should be defined and validated when outputs cross module boundaries.

### Preferred Validation Style (m/=> + instrumentation)

Use Malli function schemas with instrumentation instead of manual `m/validate`.

```clojure
(m/=> greet [:=> [:cat GreetingRequest] GreetingResponse])
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
(defn* greet [:=> [:cat GreetingRequest] GreetingResponse]
  [input]
  {:message (str "Hello " (:name input))})
```

### Project Macro: main

Use `main` for concise `-main` entrypoint declarations.
In `main.clj`, prefer entrypoint schema name `Input` (not `MainInput`) because
file context already provides the `main` semantic layer.
It expands to `defn* -main` with either:
- inferred schema: `[:=> [:cat InputSchema] :any]`
- explicit function schema when provided.

In concise mode, non-symbol input forms are compiled via Malli lite syntax.

```clojure
(main Input [input]
  (println input))
```

Auto-arg form (derived from type name):

```clojure
(main Input
  (println input))
```

Malli lite input form:

```clojure
(main {:args [:vector :string]} [input]
  (println input))
```

Explicit schema form:

```clojure
(main [:=> [:cat Input] :any] [input]
  (println input))
```

### Project Macro: impl

Use `impl` for protocol method implementations with Malli instrumentation.
Like `main`, it supports concise input-schema forms with default `:any` output.

Concise form:

```clojure
(impl DefaultTool ToolOps run-for Input [this input]
  ...)
```

Concise form with explicit output:

```clojure
(impl DefaultTool ToolOps run-for Input :string [this input]
  ...)
```

Concise auto-arg form:

```clojure
(impl DefaultTool ToolOps run-for Input
  (println input))
```

Explicit schema form:

```clojure
(impl DefaultTool ToolOps run-for [:=> [:cat :any Input] :string] [this input]
  ...)
```

## Namespace Discipline

*   A namespace is a semantic layer.
*   Related objects are grouped in a single namespace (e.g., `mentci.intent`).
*   Do not duplicate meaning in function names. The namespace conveys the noun.
*   Protocol names and object names carry durable semantics; helper wrappers remain thin.
*   Scalar schema naming rule: if schema is scalar (for example `:string`), avoid flow wrappers/suffixes like `DeletePathInput`; use domain nouns like `Path`.
*   Single-item map ban: `[:map [:path :string]]` is invalid unless explicitly justified for backward compatibility; default is scalar `:string`.

## Documentation Protocol

Documentation is impersonal, timeless, and precise. Document only non-boilerplate behavior.
