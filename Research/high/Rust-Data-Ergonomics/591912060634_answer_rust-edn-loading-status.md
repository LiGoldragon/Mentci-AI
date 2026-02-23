# Research Artifact: Rust EDN Loading Status

- **Solar:** ♓︎ 6° 6' 34" | 5919 AM
- **Subject:** `Rust-Data-Ergonomics`
- **Title:** `rust-edn-loading-status`
- **Status:** `operational`

## 1. Summary
The project has successfully transitioned from hardcoded logic to **Logic-Data Separation** using EDN as the primary text-native data substrate.

## 2. Current Implementation
The `edn-rs` library is the current engine for loading structured data in Rust.

### 2.1 Areas of Usage
*   **`mentci-fs`**: Uses `edn-rs` to parse `index.edn` maps for localized filesystem indexing.
*   **`mentci-aid` Actors**: 
    *   `LinkGuard`: Loads roots, rules, and allowlists from `link_guard.edn`.
    *   `RootGuard`: Loads allowed directory patterns from `root_guard.edn`.
    *   `SubjectUnifier`: Loads tier definitions from `subject_unifier.edn`.
    *   `ProgramVersion`: Loads alphabet and path configurations from `program_version.edn`.

## 3. Achievements
*   **Sidecar Pattern**: Implementation files now have corresponding `.edn` files, making the logic universal and the configuration explicit.
*   **Universal Value Mapping**: A helper pattern has been established to convert `edn_rs::Edn` types into native Rust strings and vectors, handling colon-prefix keywords and quotes.

## 4. Identified Friction
*   **Boilerplate**: The conversion from `edn_rs::Edn` to target types (e.g., `BTreeMap<String, Edn>` -> `Vec<String>`) requires manual matching and string cleaning.
*   **Ownership**: `edn-rs` types often require cloning to avoid moving out of shared references during traversal.
*   **Lack of Schema**: While EDN is structured, it is not schema-validated at the reader level (unlike Cap'n Proto).

## 5. Next Steps
Research a more ergonomic "Value" abstraction or a macro-based loader to reduce boilerplate in actor initialization.
