# Research Artifact: Code-Data Separation Protocol

- **Solar:** ♓︎ 6° 6' 15" | 5919 AM
- **Subject:** `Code-Data-Separation`
- **Title:** `code-data-separation-protocol`
- **Status:** `proposed`
- **Weight:** `High` (Author Directed)

## 1. Intent
Formalize the requirement to strictly separate "logic" (code) from "data" (variables, paths, regexes, configuration). All data must reside in external structured files, enabling logic to remain universal and data-driven.

## 2. Findings
*   **Current State:** Several components (e.g., `execute`, `mentci-fs`) still contain hardcoded paths, regexes, and configuration flags. This violates the author's intent for "Symbolic Manipulation" where types and rules are intrinsic properties of data, not procedural side effects.
*   **Author's Directive:** "All 'variables' or 'data' parts of any code to stay *out of the code*, or logic part of the system, and always use a structured data input, where default values can be kept in a file alongside the code."

## 3. Proposed Guidelines

### 3.1 Externalization Rule
*   **Forbidden:** Hardcoded string literals, paths, regexes, or numeric constants within implementation files.
*   **Required:** All such values must be loaded from an external structured data file (Config).
*   **Format Preference:**
    1.  **Cap'n Proto (`capnp`)**: For performance-critical or schema-validated binary state.
    2.  **EDN**: For text-native, LLM-friendly structured logic.
    3.  **JSON**: Only as a fallback or for interoperability with external tools.

### 3.2 Sidecar Configuration
*   Default values for any logic must be kept in a "sidecar" file alongside the source code.
*   Example: `src/bin/execute.rs` -> `src/bin/execute.edn` or `src/bin/execute.capnp`.

### 3.3 Runtime Injection
*   Logic components must accept a "Request" or "Input" object (Sema style) that contains all necessary data for the operation.
*   If no input is provided, the component should look for its sidecar configuration.

## 4. Impact on Existing Guidelines

### 4.1 `Core/ARCHITECTURAL_GUIDELINES.md`
Update Section 6 ("Ontology Resides in Data") to explicitly mandate externalization and define the format hierarchy.

### 4.2 `Core/SEMA_RUST_GUIDELINES.md`
Update Section 3 ("Schema is Sema") to enforce the sidecar configuration pattern and the use of `capnp`/`edn` for all non-logic inputs.

## 5. Next Steps
1.  Draft the specific text changes for the core guidelines.
2.  Perform a "sweep" of the codebase to identify and externalize remaining hardcoded data (low-hanging fruit).
3.  Implement a standard loader pattern in `mentci-box-lib` for these sidecar files.
