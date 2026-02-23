# Lojix Syntax Specification

- **Status:** Research / Canonical Intent
- **Weight:** High (Author Directed)

## 1. Objective
Define the `lojix` syntax rules for rendering structured Sema data into a human-readable but whitespace-independent text format.

## 2. Dialect: EDN Next
Logics (`lojix`) is an extended dialect of EDN/Clojure.

### 2.1 The Genius of Lisp (Whitespace Independence)
Like Lisps, `lojix` does not rely on newlines or indentation for semantic parsing. This maximizes context density for LLMs and allows for extremely compact representations without losing structural clarity.

### 2.2 Transpilation
*   `lojix` serves as the text-based intermediate representation for the SEMA binary format.
*   It is designed to be easily pre-read and converted into Rust-based machine structures.

## 3. Reader Rules
*   **Keywords as Objects:** The reader recognizes keywords directly as objects in the symbolic space.
*   **Single Owner Model:** Syntax supports explicit single-owner concepts to align with the actor-based flow.
