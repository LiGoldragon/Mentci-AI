# Strategy: Aski Macro-Based Conversion

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Symbolic Interface and Data Purity)

## 1. Objective
Develop a robust, macro-based transformation system between **Super-Sugar Aski** (delimiter-driven typing) and **Canonical Type-System EDN** (fully-specified with explicit IDs and kinds).

## 2. Transformation Rules (The "Sugar Model")

### 2.1 Delimiter Mapping
- **Map `{}` -> `:struct`**: Keys represent field names, values represent types.
- **Vector `[]` -> `:enum`**: Items represent ordered variants.
- **List `()` -> `:instance`**: First element is the type (noun), remaining is configuration.

### 2.2 Subtype Identification (The "Delimiter-in-Value" Rule)
Subtypes are indicated by nested delimiters within values:
- `[:list :Type]` -> A vector of `Type`.
- `{:field [:maybe :Type]}` -> A struct field that is an optional `Type`.

### 2.3 Canonicalization pass (The Macro)
The macro (implemented in Clojure/Babashka) performs the following normalization:
1.  **ID Assignment:** Fields in a struct and values in an enum are assigned stable, monotonically increasing IDs (starting at 0).
2.  **Kind Injection:** Injects `:kind :struct` or `:kind :enum` based on the top-level delimiter.
3.  **Namespace Propagation:** Attaches the global `:namespace` from the spec context to every type definition.

## 3. Implementation Plan

### 3.1 Prototype Macro (`scripts/lib/aski_macro.clj`)
Develop a core library that provides `(expand-sugar <sugar-data>)` returning canonical EDN.

### 3.2 Trial: `inputs.aski-fs` Conversion
Apply the macro to `inputs.aski-fs` to generate a fully-specified `schema/inputs_schema.edn` that can be compiled to Cap'n Proto.

### 3.3 Validation Logic
Integrate with Malli to ensure the "Sugar" follows the delimiter axioms before expansion.

## 4. Why this matters
This strategy enables humans to author high-density specifications (Sugar) while ensuring the machine has a low-ambiguity, strictly-versioned truth (Canonical). It bridges the gap between **Level 5 (Structured)** and **Level 6 (Symbolic)** interaction.

*The Great Work continues.*
