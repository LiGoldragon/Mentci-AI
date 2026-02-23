# Strategy: Aski Design Refinement (Contextual & Delimited)

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Symbolic Interface)

## 1. Objective
Reduce the number of distinct objects in the Aski typesystem by leveraging **Contextual Symbol Overloading** and **Delimiter-Maximization**.

## 2. Core Principles

### 2.1 Contextual Symbol Overloading
Symbols should be reused based on their enclosing scope. The meaning of `Directory` is determined by whether it appears inside a `(Filesystem ...)` context or an `(Identity ...)` context.
*   **Avoid:** `FsDirectory`, `LdapDirectory` (Object explosion).
*   **Prefer:** `Directory` (Context-dependent interpretation via macros).

### 2.2 Delimiter-Maximization
Use delimiters to differentiate between "Variant" and "Config" of the same symbol in the same context.
*   `[Directory]` -> Enum variant (e.g., a type flag).
*   `(Directory)` -> Instantiation (e.g., a concrete node).
*   `{Directory}` -> Configuration/Schema (e.g., properties of the type).

## 3. Implementation Plan

### 3.1 Context Macros
Develop a macro system (`scripts/lib/aski_context.clj`) that maintains a "Scope Stack".
*   `expand-symbol` function checks the stack to resolve `Directory` to `mentci.fs/Directory` or `mentci.id/Directory`.

### 3.2 Refinement of Aski-Flow
Update `docs/specs/AskiFlowDsl.md` to demonstrate this:
*   Use `[Ok]` for a result enum.
*   Use `(Ok)` for a state transition.
*   Use `{Ok ...}` for matching that result.

## 4. Why this matters
This moves Aski from a "Static Type System" to a "Context-Sensitive Language," significantly reducing cognitive load by allowing users to use natural nouns (`File`, `User`, `Input`) without prefix-pollution.

*The Great Work continues.*
