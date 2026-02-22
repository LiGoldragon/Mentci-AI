# Research: Sema Guideline Wisdom Reincorporation

## Objective
Compare current guideline set with CriomOS source guidelines, identify why semantic coverage was reduced, and map how to reincorporate missing concepts across Rust/Clojure/Nix plus universal architectural guidance.

## Source Corpus
### CriomOS Source
1. `Inputs/criomos/docs/GUIDELINES.md`
- Universal Sema architecture/naming/documentation rules (Rust examples).

2. `Inputs/criomos/docs/NIX_GUIDELINES.md`
- Nix-specific adaptation with additional practical module/attrset grouping guidance.

3. `Inputs/criomos/docs/AGENT_RULES.md`
- Filesystem-capitalization durability authority system (ALL_CAPS/PascalCase/lowercase).

4. `Inputs/criomos/docs/AGENTS.md`
- Operational framing linking universal + Nix-specific guidance.

### Current Mentci Targets
- `core/SEMA_RUST_GUIDELINES.md`
- `core/SEMA_CLOJURE_GUIDELINES.md`
- `core/SEMA_NIX_GUIDELINES.md`
- `core/ARCHITECTURAL_GUIDELINES.md` (universal anchor)

## History Finding (Why It Was Truncated)
The reduced Rust guideline was introduced at file creation time in:
- `vnkltomunyst` / `unowpkwnpvrx` (`docs: add rust and nix sema guidelines`)

This indicates an intentional language-specific condensation, not an accidental later truncation. Later commits mostly relocated files (`docs/architecture/` -> `core/`) and stripped framing artifacts.

## Concept Coverage Gap (CriomOS Universal vs Current Rust)
Missing or weakened in current Rust guideline:
1. **Naming Is a Semantic Layer**
- Long-name-as-smell rule and “name is a commitment” framing are absent.

2. **Objects Exist; Flows Occur**
- Explicit noun/verb ontology distinction is absent.

3. **Construction Resolves to Receiving Type**
- Strong rule that constructors/parsers live on target type is absent.

4. **Trait-Domain Deep Constraint**
- Current text keeps trait-domain rule but omits “no composite role noun” warning and stronger anti-bypass framing.

5. **Schema Is Sema; Encoding Is Incidental (Cap’n Proto caveat)**
- Current text states encoding is incidental, but explicit “Cap’n Proto is temporary wire representation; keep out of domain naming/docs” is missing.

6. **Filesystem as Semantic Layer**
- Missing from current language-specific Rust guideline.

7. **Documentation Protocol Depth**
- Current summary is minimal; original includes strict anti-persona and anti-evaluative constraints.

## Concept Coverage Gap (CriomOS Nix vs Current Nix)
Missing or weakened in current Nix guideline:
1. **Naming Is a Semantic Layer** depth
- Current Nix doc has naming bullets but lacks full “long-name indicates missing abstraction” framing.

2. **Group Related Functions in Attrset Namespaces**
- Present in CriomOS Nix guideline; not explicit in current `core/SEMA_NIX_GUIDELINES.md`.

3. **Construction Resolves to Defining Attrset**
- Missing explicit section and examples.

4. **Filesystem as Semantic Layer**
- Missing explicit section in current Nix guideline.

5. **Documentation Clarity Dual-Path**
- CriomOS Nix explicitly states self-documenting vs mandatory comment path with examples; current doc is shortened.

## Universal Principles Extracted for Cross-Language Translation
These should exist once as universal mandates and then be mapped per-language:
1. Naming Is a Semantic Layer
2. Capitalization Declares Ontology (code) / Durability (filesystem)
3. Objects Exist; Flows Occur
4. Domain-Protocol/Trait rule (reuse existing abstraction domains)
5. Direction Encodes Action
6. Construction Resolves to Receiving/Defining Object
7. Single Object In/Out boundary contract
8. Schema Is Sema; wire/encoding is incidental
9. Filesystem as Semantic Layer
10. Documentation Protocol (impersonal, timeless, precise; clarity requirements)

## Cross-Language Reintegration Targets
### Rust (`core/SEMA_RUST_GUIDELINES.md`)
- Reintroduce all seven missing concepts as first-class sections.

### Clojure (`core/SEMA_CLOJURE_GUIDELINES.md`)
Add Rust-analog structural rules:
1. **Protocol-Domain Rule** (Trait-domain analog):
- If behavior fits an existing protocol/interface domain, implement/extend protocol rather than creating ad-hoc verb function families.

2. **Objects Exist; Flows Occur**:
- Namespace/data object nouns vs function-flow verbs made explicit.

3. **Construction Resolves to Receiving Object**:
- Prefer construction/coercion via protocol-based constructors (`from-*`) anchored to receiving object identity.

4. **Filesystem/Naming Semantic Layer**:
- Reinforce context-layered naming and long-name-as-smell guidance.

5. **Documentation Protocol parity**:
- Import stronger anti-personal/anti-evaluative rules from original.

### Nix (`core/SEMA_NIX_GUIDELINES.md`)
- Reintroduce “Naming Is a Semantic Layer” depth and filesystem-layer framing from original.
- Keep Nix-specific adaptations from `Inputs/criomos/docs/NIX_GUIDELINES.md`.

### Universal (`core/ARCHITECTURAL_GUIDELINES.md` + optional dedicated core/program)
- Add an explicit universal translation table:
- each universal principle
- Rust realization
- Clojure realization
- Nix realization
- This prevents drift where one language guideline silently loses principles.

## Clojure Extension Direction (Method + Lite Type Syntax)
1. **Method-Oriented Clojure Layer**
- Use `defprotocol` + `extend-type`/`extend-protocol` for domain behaviors where protocol domains exist.
- Reserve plain `defn` orchestration shells for boundary/control flow only.

2. **Lite Schema Authoring Layer**
- Introduce local macro layer (e.g., `defobj`) that expands map-literal syntax to canonical Malli vectors.
- Keep canonical expanded schema semantics unchanged.
- Do not remove map keys (field keys are semantic contract).

3. **Function Signature Compression (without weakening type contracts)**
- Input-first macro forms are allowed only if output type remains explicit or deterministically inferred with compile-time failure on unresolved output.

## Risks
1. Over-copying original text can create duplication with `core/ARCHITECTURAL_GUIDELINES.md`.
2. Clojure protocol push can be over-applied to simple scripts; need clear threshold.
3. Lite syntax macros can hide schema shape if expansions are not documented.
4. Universal + language docs can diverge without a shared principle-to-language mapping table.
