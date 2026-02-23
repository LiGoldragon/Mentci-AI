# Research Artifact: Ergonomic Data Access in Rust

- **Solar:** ♓︎ 6° 6' 34" | 5919 AM
- **Subject:** `Rust-Data-Ergonomics`
- **Title:** `ergonomic-data-access-survey`
- **Status:** `finalized`

## 1. Intent
Survey the Rust ecosystem for the most ergonomic ways to access and manipulate structured data (JSON, EDN, TOML) at runtime, comparing "Value" enums vs. typed structs vs. dynamic wrappers.

## 2. Landscape Analysis

### 2.1 The Standard: Serde enums (`Value`)
The most reliable way to handle dynamic data.
*   **Syntax:** `v["key"]["nested"]`
*   **Pros:** Native Serde support, high performance, zero-copy.
*   **Cons:** Verbose `.as_type()` and `.unwrap()` calls for deep nesting.

### 2.2 JavaScript-like Dot Access (`dyn_path`)
Crates like `dyn_path` allow for macro-based property access.
*   **Syntax:** `dyn_access!(v.key.nested)`
*   **Pros:** Extremely ergonomic for developers coming from Python/JS.
*   **Cons:** Higher compile-time cost due to complex macros.

### 2.3 Runtime Dynamic Types (`rust_dynamic`)
Provides a truly dynamic type system (Duck Typing).
*   **Pros:** Full runtime flexibility (Integers, Lists, Lambdas).
*   **Cons:** Significant performance penalty compared to static types.

## 3. Findings for Mentci-AI
The current implementation using `edn-rs` provides a `Map` structure that maps to `BTreeMap<String, Edn>`. 

### 3.1 Established Pattern
I have established an "Actor-Ergonomic" pattern using helper methods:
```rust
fn get_edn_vector(&self, map: &BTreeMap<String, Edn>, key: &str) -> Result<Vec<String>, Vec<String>> {
    // Logic to handle keywords, quotes, and vector conversion
}
```

## 4. Recommendations
1.  **Prefer Structs for Stability**: Whenever a data schema is stable (e.g., `FsIndex`), use `#[derive(Deserialize)]` via `serde_edn` or `edn-rs` to eliminate manual traversal.
2.  **Use Helpers for Discovery**: For exploratory actors (e.g., `LinkGuard`), continue using the manual traversal helpers until the schema matures.
3.  **Investigate `edn-derive`**: Evaluate if `edn-rs` macros can be improved to support "Sidecar Loading" with zero boilerplate.

## 5. Decision
The project will continue to favor **`edn-rs` with typed structs** where possible, using the **Sidecar Pattern** to ensure logic-data separation is both strict and ergonomic.
