# Strategy: Guideline Wisdom Reincorporation

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Architectural Integrity)

## 1. Goal
Reincorporate the semantic depth of the human-reviewed original guideline (`Inputs/criomos/docs/GUIDELINES.md`) into current language-specific core guidelines without losing language-local clarity.

Expansion subaspect:
- `strategies/guidelines-wisdom-reincorporation/EXPANSION.md`

## 2. Scope
- `core/SEMA_RUST_GUIDELINES.md`
- `core/SEMA_CLOJURE_GUIDELINES.md`
- `core/SEMA_NIX_GUIDELINES.md`
- Universal anchor and translation map in `core/ARCHITECTURAL_GUIDELINES.md` (or `core/SEMA_UNIVERSAL_GUIDELINES.md`)
- Cross-check against CriomOS source set:
  - `Inputs/criomos/docs/GUIDELINES.md`
  - `Inputs/criomos/docs/NIX_GUIDELINES.md`
  - `Inputs/criomos/docs/AGENT_RULES.md`
  - `Inputs/criomos/docs/AGENTS.md`

## 3. Strategy Phases
### Phase 1: Build Universal Principle Map
Define a canonical principle set and per-language realizations:
1. Naming Is a Semantic Layer
2. Objects Exist; Flows Occur
3. Domain abstraction rule (trait/protocol/lib-domain)
4. Direction Encodes Action
5. Construction Resolves to receiver/definer
6. Single object-in/object-out boundary rule
7. Schema Is Sema
8. Filesystem as semantic/durability layer
9. Documentation protocol and clarity obligations

### Phase 2: Canonical Gap Integration (Rust First)
Rebuild `core/SEMA_RUST_GUIDELINES.md` with the missing sections:
1. Naming Is a Semantic Layer
2. Objects Exist; Flows Occur
3. Construction Resolves to Receiving Type
4. Trait-Domain deep constraints
5. Schema Is Sema (explicit Cap’n Proto caveat)
6. Filesystem as Semantic Layer
7. Documentation Protocol depth

### Phase 3: Cross-Language Harmonization
Translate equivalent principles into language-native forms:
- Clojure: Protocol-Domain Rule, receiving-object construction rule, ontology language.
- Nix: semantic naming layer, grouped attrset namespaces, construction-at-defining-attrset rule, filesystem-layer identity rules.

### Phase 4: Clojure Method and Lite-Schema Extension
1. Add protocol-first guidance:
- `defprotocol`/`extend-type` when behavior belongs to stable domain.
- `defn` orchestration shells for coordination.

2. Add lite-schema authoring guidance:
- macro sugar for map schema authoring (`defobj` style) that expands to canonical Malli schema.
- preserve key-level semantic contracts.

3. Add signature compression guidance:
- input-first forms allowed only with explicit output type or deterministic inferred output.
- prohibit fallback to `:any` as default output.

### Phase 5: Consistency + Tooling
- Add a guideline consistency checker (pattern-based) to flag missing mandated section headers.
- Add cross-reference table in one location to prevent divergent wording.
- Add source-trace tags in guideline headers indicating mapped CriomOS sources.

## 4. Deliverables
1. Revised `core/SEMA_RUST_GUIDELINES.md` with restored conceptual depth.
2. Revised `core/SEMA_CLOJURE_GUIDELINES.md` with protocol-domain and lite-schema method.
3. Revised `core/SEMA_NIX_GUIDELINES.md` with restored CriomOS Nix-specific depth.
4. Universal principle translation table (Rust/Clojure/Nix mappings).
5. Short compatibility note documenting why prior condensed form existed and what changed.

## 5. Acceptance Criteria
1. All major concepts from original `GUIDELINES.md` appear in Rust guideline with clear language mapping.
2. Clojure guideline includes protocol-domain + lite-schema + typed signature compression constraints.
3. Nix guideline includes grouped-namespace and defining-attrset construction principles from `NIX_GUIDELINES.md`.
4. No guideline recommends weakening output typing (`:any`) as default.
5. No contradiction with `core/ARCHITECTURAL_GUIDELINES.md` authority hierarchy.

## 6. Rollout Order
1. Universal principle map.
2. Rust guideline restoration.
3. Nix guideline restoration.
4. Clojure extension and method/lite-schema translation.
5. Validation pass and cross-doc conflict check.

*The Great Work continues.*
