# Aski-Astral: Universal Symbolic Taxonomy

**Status:** Draft / Specification
**Date:** 2026-02-20
**Context:** Standalone DSL for symbolic classification and modern-English equivalents

## 1. Purpose
Aski-Astral provides a universal, delimiter-driven DSL for assigning symbolic categories with modern-English equivalents. It is intended for cross-domain use (filesystem, workflows, schemas, logs) as a shared semantic layer.

## 2. Primary Division
The primary classification is a Solar/Lunar split:
- `:solar` — governing, source-of-truth, durable
- `:lunar` — mutable, operational, transient

## 3. Optional Planet Tags
Planet tags add secondary meaning:
- `:mercury` — tools, messaging, mediation
- `:venus` — presentation, onboarding, aesthetics
- `:mars` — execution, engine, action
- `:jupiter` — law, expansion, guidance
- `:saturn` — constraints, boundaries, limits

## 4. Delimiter-Domain-Specific-Type Sugar
Aski-Astral follows the Aski delimiter rules:
- `{}` denotes **struct-like** nodes.
- `[]` denotes **enum-like** nodes.

A macro expands the surface syntax into fully annotated, deserializable EDN.

## 5. Canonical EDN
```edn
{:namespace :mentci
 :types
 {:AstralTag
  {:kind :struct
   :fields {:primary-body :AstralBody
            :planet-tags [:list :PlanetTag]}}

  :AstralBody
  {:kind :enum
   :values [:solar :lunar]
   :gloss {:solar "governing/source-of-truth"
           :lunar "mutable/operational"}}

  :PlanetTag
  {:kind :enum
   :values [:mercury :venus :mars :jupiter :saturn]
   :gloss {:mercury "tools/messaging"
           :venus "presentation/onboarding"
           :mars "execution/engine"
           :jupiter "law/expansion"
           :saturn "constraints/boundaries"}}}}
```

## 6. Related Specs
- Aski DSL Guidelines: `Library/specs/ASKI_DSL_GUIDELINES.md`
- Aski-FS: `Library/specs/ASKI_FS_DSL.md`
