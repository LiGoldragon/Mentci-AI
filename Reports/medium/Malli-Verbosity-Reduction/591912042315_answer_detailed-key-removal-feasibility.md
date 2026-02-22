# Agent Report

- Date: `2026-02-22`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Malli-Verbosity-Reduction`
- Title: `Detailed Analysis: Key Removal Feasibility (:raw, etc.)`

## Prompt

detailed report on "Key removal (:raw, etc.): not feasible without semantic breakage." - what is the purpose of ":raw" in this context. Is semantic breakage a definitive obstacle to implementation? Can we get the data some other way (context, object type (list, vector, map, etc), macro reader?)

## Executive Answer

- `:raw` is not decorative; it is the **named slot of a map-shaped object contract**.
- Removing keys from map schemas is a semantic change, not just syntax reduction.
- This is a definitive obstacle for **backward-compatible keyless maps**.
- It is **not** a definitive obstacle for implementation overall if you accept one of:
  1. internal canonical keys + external sugar,
  2. a different data shape (tuple/vector/scalar/record),
  3. an adapter/migration layer.

## What `:raw` Does in This Codebase

In current usage, `:raw` is the field carrying unprocessed text input. Example in `Components/scripts/session_guard/main.clj`:
- Schema: `ParseInput = [:map [:raw :string]]`
- Access: `(str/split-lines (:raw input))`

So `:raw` provides:
1. **Addressability**: stable named lookup (`(:raw input)`).
2. **Validation semantics**: Malli enforces presence/type at that key.
3. **Meaning**: distinguishes this string from other strings in the same object.

## Why Removing Map Keys Breaks Semantics

Map-key semantics are part of the object type contract:
- `[:map [:raw :string]]` means “a map with a required slot named `:raw`”.
- Dropping key names changes both representation and API expectations.

Breakage surfaces:
1. Call sites expecting maps with named keys fail.
2. Destructuring and keyword lookup semantics disappear.
3. Error reports and schema docs lose stable field identity.

## Is Semantic Breakage Definitive?

### For keyless maps while preserving current API
Yes, effectively definitive. A map without keys is a contradiction.

### For achieving less verbosity overall
No. You can still reduce authoring verbosity while preserving semantics.

## Alternatives and Tradeoffs

### A) Keep keys internally, hide them in authoring syntax (recommended)
Example authoring sugar:
```clojure
(defobj ParseInput {:raw :string})
```
Expansion stays canonical:
```clojure
(def ParseInput [:map [:raw :string]])
```
- Pros: no runtime/API breakage; tooling compatibility retained.
- Cons: keys still exist (by design).

### B) Switch to positional vectors/tuples
Example:
```clojure
(def ParseInput [:tuple :string])
```
- Pros: no key names in shape.
- Cons: semantic clarity loss; high migration cost; brittle positional coupling.

### C) Use scalar object wrappers when there is exactly one field
Example concept: treat input as `:string` directly and wrap/unwrap at boundary.
- Pros: concise for truly unary data.
- Cons: fails once object needs a second field; migration churn likely.

### D) Record/protocol abstraction with hidden storage detail
Expose methods (`to-lines`) and avoid direct map-key usage at call sites.
- Pros: aligns methods-on-objects-first rule.
- Cons: still needs underlying field identity somewhere unless fully positional/scalar.

### E) Reader/macro magic to infer missing keys
Could infer keys from declaration context, but generated schema must still contain keys.
- Pros: concise surface syntax.
- Cons: indirection/debug complexity; does not eliminate semantic need for keys.

## Can Context or Object Type Replace Keys?

Partially, but with constraints:
1. **Context-only inference**: fragile and non-local; hurts readability and tooling.
2. **Object type only** (list/vector/map distinction): insufficient for multi-field named semantics.
3. **Macro reader**: can transform syntax, but cannot avoid final need for explicit field identity if representation remains map-based.

## Practical Recommendation

1. Keep map keys as canonical semantics.
2. Introduce syntax sugar (`defobj`, `defn1`) that expands to canonical Malli forms.
3. For specific high-noise unary objects, optionally allow scalar/tuple forms with explicit boundary adapters.
4. Enforce no `:any` fallback in function output typing.
5. Pilot on one script before broader migration.

## Bottom Line

- If the object remains a map, key removal is not compatible with preserving current semantics.
- If changing representation is acceptable, keyless designs are implementable but constitute an intentional API/schema migration.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
