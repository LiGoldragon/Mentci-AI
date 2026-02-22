# Aski DSL Guidelines

**Status:** Draft / Specification
**Date:** 2026-02-20
**Context:** Unified guidelines for Aski-family DSLs

## 1. Principle: Delimiter-Domain-Specific-Type Sugar
Aski DSLs use **delimiter-based context** as the primary type signal. Surface syntax is concise and must be expanded by a macro into fully annotated, deserializable EDN.

**Core rule:**
- `{}` implies **struct-like** types (fielded objects).
- `[]` implies **enum-like** types (ordered variants).

The macro is responsible for attaching explicit type metadata, field ids, and validation constraints.

## 2. Expansion Contract
Every Aski DSL must provide a macro or compiler step that transforms sugar into canonical EDN with:
1. **Explicit kinds** (`:struct`, `:enum`, etc.).
2. **Stable field identifiers** (positional or annotated).
3. **Validation constraints** (lengths, optionality, unions).
4. **Deterministic ordering** (for reproducible Capnp output).

## 3. Canonical EDN Shapes
### 3.1 Structs (Map Delimiter)
```edn
{:CommitMessage
 {:intent :IntentTag
  :scope :ScopeTag
  :notes [:short-string 160]}}
```

### 3.2 Enums (Vector Delimiter)
```edn
{:IntentTag
 [:add :fix :refactor :doc :meta]}
```

### 3.3 Expanded Form (Canonical)
```edn
{:namespace :mentci
 :types
 [{:name :CommitMessage
   :kind :struct
   :fields [{:id 0 :name :intent :type :IntentTag}
            {:id 1 :name :scope :type :ScopeTag}
            {:id 2 :name :notes :type [:short-string 160]}]}
  {:name :IntentTag
   :kind :enum
   :values [:add :fix :refactor :doc :meta]}]}
```

## 4. Constraints
- **Fixed vocabularies** are required for enums.
- **Short string notes** are bounded by explicit length annotations.
- **No implicit unions:** all union/optional behavior must be explicit in the canonical EDN.

## 5. Capnp Emission
Aski DSLs that compile to Cap'n Proto must:
1. Preserve ordering for field ids and enum ordinals.
2. Emit stable names and namespaces.
3. Enforce constraints in validation (e.g., max string length) even if Capnp lacks native limits.

## 6. Related Specs
- Aski-Flow: `Library/specs/ASKI_FLOW_DSL.md`
