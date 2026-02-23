# Research Artifact: Aski Syntax Reader Strategy

- **Solar:** ♓︎ 5° 55' 26" | 5919 AM
- **Subject:** `Aski-Syntax-Research`
- **Title:** `aski-syntax-reader-strategy`
- **Status:** `proposed`

## 1. Intent
Design a sophisticated Rust-based reader for the "Aski" syntax—a Clojure/EDN-inspired structured data format that utilizes all delimiters and symbol-prefix rules for maximum type expressiveness.

## 2. Syntax Characteristics
The Aski syntax expands upon EDN with strict ontological rules based on casing and delimiters:

### 2.1 Casing Rules (Symbol Significance)
*   **`ALL_CAPS`**: Supreme Law / Universal Constants.
*   **`PascalCase`**: Durable Objects / Stable Contracts.
*   **`lowercase`**: Transient Flow / Implementation Logic.

### 2.2 Delimiter-Type Mapping
*   **`()` (Lists)**: Flow, executable calls, or ordered sequences.
*   **`[]` (Vectors)**: Sets, enums (when at root), or fixed-length tuples.
*   **`{}` (Maps)**: Structured directories or attribute sets.
*   **`#""` / `#_` / `#{}`**: Specialized extensions (standard EDN).

## 3. Reader Architecture (`aski-lib`)
The reader will be implemented as a Rust library de-structured into a **Universal Structured Data Format** (likely a custom `AskiValue` enum) that supports `serde` for seamless translation.

### 3.1 Components
1.  **Lexer/Parser**: Likely using `pest` or `nom` for high-performance, predictable parsing.
2.  **Ontology Resolver**: Maps casing and delimiters to semantic types.
3.  **Serde Integration**: Provides `Serialize` and `Deserialize` implementations for `AskiValue`.

## 4. CLI Tool (`aski-cli`)
A thin wrapper around `aski-lib` for system-level testing and translation.

### 4.1 Usage Interface
*   `aski-cli convert --input <file.aski> --to <json|edn|toml>`
*   `aski-cli validate <file.aski>`

## 5. Execution Plan
1.  Define the `aski-lib` crate structure and dependencies.
2.  Specify the `AskiValue` enum and semantic casing rules.
3.  Implement the parser.
4.  Create `aski-cli` for verification.
