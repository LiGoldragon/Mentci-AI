# Aski-FS Astral Mapping

**Status:** Draft / Specification
**Date:** 2026-02-20
**Context:** Solar/Lunar primary typing for filesystem nodes with optional planetary overlays

## 1. Purpose
Aski-FS Astral mapping assigns **primary Solar/Lunar classification** to filesystem nodes, with optional **planet tags** for secondary meaning. This is a cross-domain semantic layer defined by Aski-Astral.

## 2. Primary Mapping Rules
- `:solar` — stable, governing, source-of-truth directories and files.
- `:lunar` — mutable, operational, transient directories and files.

## 3. Optional Planet Tags
Planet tags provide quick semantic hints:
- `:mercury` — tools, messaging, mediation
- `:venus` — presentation, onboarding, aesthetics
- `:mars` — execution, engine, action
- `:jupiter` — law, expansion, guidance
- `:saturn` — constraints, boundaries, limits

## 4. Aski-FS Annotation Pattern
Use `:astral` on any node:

```edn
{"schema" {:kind :dir
           :astral {:primary-body :solar
                    :planet-tags [:saturn]}}}
```

## 5. Example: Project Slice
```edn
{:root
 {:path "/home/li/git/Mentci-AI"
  :kind :dir
  :astral {:primary-body :solar :planet-tags [:jupiter]}
  :children
  {"schema" {:kind :dir
             :astral {:primary-body :solar :planet-tags [:saturn]}}
   "docs" {:kind :dir
           :astral {:primary-body :solar :planet-tags [:jupiter :venus]}}
   "src" {:kind :dir
          :astral {:primary-body :lunar :planet-tags [:mars]}}
   "scripts" {:kind :dir
              :astral {:primary-body :lunar :planet-tags [:mercury]}}
   "Logs" {:kind :dir
           :astral {:primary-body :lunar :planet-tags [:mercury]}}
   "target" {:kind :dir
             :astral {:primary-body :lunar :planet-tags [:mars]}}}}}
```

## 6. Related Specs
- Aski-Astral DSL: `Library/specs/ASKI_ASTRAL_DSL.md`
- Aski-FS DSL: `Library/specs/ASKI_FS_DSL.md`
