# Research Artifact: Mentci-Box Migration Initiation

- **Solar:** ♓︎ 5° 53' 22" | 5919 AM
- **Subject:** `Mentci-Box-Migration`
- **Title:** `mentci-box-migration-init`
- **Status:** `in-progress`

## 1. Intent
Migrate and rename the sandbox logic from `mentci-aid` to a standalone set of Rust components named `mentci-box`.
The goal is to provide a library for sandbox logic and a dedicated executable for bootstrapping and reliable execution.

## 2. Approach
*   Create `Components/mentci-box-lib` for the core sandbox logic.
*   Create `Components/mentci-box` for the command-line interface.
*   Refactor the existing `sandbox.rs` from `mentci-aid` into Sema-compliant Rust objects.
*   Maintain `mentci-aid` support by optionally depending on the new library or keeping a thin wrapper.

## 3. Structural Plan
*   `Components/mentci-box-lib/`:
    *   `src/lib.rs`: Sema Objects (`Sandbox`, `SandboxConfig`, `BindMapping`).
*   `Components/mentci-box/`:
    *   `src/main.rs`: CLI entry point using `mentci-box-lib`.
