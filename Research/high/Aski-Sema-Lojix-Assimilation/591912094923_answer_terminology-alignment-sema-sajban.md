# Research Artifact: Terminology Alignment (Sema vs Sajban)

- **Solar:** 5919.12.9.49.23
- **Subject:** `Aski-Sema-Lojix-Assimilation`
- **Title:** `terminology-alignment-sema-sajban`
- **Status:** `canonical-intent`
- **Weight:** `High` (Direct author-intent update)

## 1. Intent
To formalize the distinction between **SEMA** (machine-code) and **Sajban** (natural-language Aski) and document the deprecation of "sajban" as a synonym for "sema".

## 2. Terminology Map

### 2.1 SEMA (The Substrate)
*   **Definition:** The machine-code symbolic language that underpins computer technology forever.
*   **Format:** Specialized binary (structured trees of enumerators).
*   **Deprecation Note:** Formerly referred to as "Sajban"; this usage is now deprecated.

### 2.2 Aski-Sajban (The Human Layer)
*   **Full Name:** `aski-sajban`
*   **Shorthand:** `sajban`
*   **Definition:** The natural-language counterpart to `aski-lojix`.
*   **Technical Property:** **Self-loading**. It requires itself to render itself from SEMA binary into ASCII. It defines the mapping of keywords (objects) for human-readable linguistic structures.

## 3. Example Structure (aski-sajban)

```sajban
;; Since top-level is in-context, "English" is a keyword, which has its own object definition, and so does Chinese.
  English (
    Core { if [(strong if) (weak in-case)] true [(strong true) (strong divine) (weak probable) ...] }
    Chicago ()
    London ()
    Alchemy ()
    ...
  )

  Chinese {
    Hanzi { Radicals () LineageX () BeijingSimplification () TaiwanConservation () ... }
  }
```

## 4. Hierarchy
- **Primary:** SEMA (Binary Authority)
- **Secondary (Projections):**
    - `aski-lojix`: Technical/Structural logic.
    - `aski-sajban`: Natural-language/Semantic meaning.
