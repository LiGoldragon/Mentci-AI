# Sema Object Style — Nix

This document defines the mandatory Sema object rules for Nix. The rules are
structural. Violations indicate category error, not style.

## Primary Rules

1. **Single Attrset In/Out**
   Reusable functions accept exactly one argument: an attrset. Outputs are a
   single attrset. Positional arguments are forbidden for domain logic.

2. **Attrsets Exist; Flows Occur**
   Attrsets are nouns that exist independently of evaluation. Flows are functions
   that occur during evaluation. Names must reflect this distinction.

3. **Schema Is Sema**
   Schemas define truth. Encodings are incidental.

## Naming and Ontology

*   `camelCase` denotes functions, relations, flow.
*   `kebab-case` denotes static packages or attributes that are not functions.
*   Avoid suffixes that restate the type (`Package`, `Module`, `Attrset`).
*   Avoid context-redundant prefixes. Inside `nix/` code, use `namespace`, not `nixns`.

## Standard Library Domain Rule

Any behavior in the semantic domain of an existing `lib` function must be expressed
using that function. Do not re-implement standard library patterns for merging,
mapping, or filtering.

## Direction Encodes Action

Prefer `from*` and `to*` when construction or emission is implied. Avoid verbs
like `read`, `write`, `load`, `save` when direction already conveys meaning.

## Single Attrset In/Out Example

```nix
lib.calculatePrice = { basePrice, taxRate }:
  basePrice * (1 + taxRate);

finalPrice = lib.calculatePrice { basePrice = 100; taxRate = 0.08; };
```

## Documentation Protocol

Documentation is impersonal, timeless, and precise. Document only non-boilerplate
behavior.
