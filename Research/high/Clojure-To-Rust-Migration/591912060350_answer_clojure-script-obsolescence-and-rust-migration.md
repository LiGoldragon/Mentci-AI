# Research Artifact: Clojure Script Obsolescence and Rust Migration

- **Solar:** ♓︎ 6° 3' 50" | 5919 AM
- **Subject:** `Clojure-To-Rust-Migration`
- **Title:** `clojure-script-obsolescence-and-rust-migration`
- **Status:** `proposed`

## 1. Intent
Identify obsolete functionality in the current Clojure-based script layer and define the migration path to a native Rust actor-based architecture using `ractor`.

## 2. Obsolescence Audit (Group A: Remove Immediately)

The following scripts are artifacts of earlier development phases or misunderstood instructions. They provide redundant functionality that is now natively handled by the `jj` audit trail or the `chronos` utility.

| Script | Obsolete Role | Replacement / Rationale |
| :--- | :--- | :--- |
| `logger` | Manual text logging to `Logs/`. | **JJ Audit Trail.** The repository uses atomic `intent:` and `session:` commits as the authoritative history. |
| `session_metadata` | Storing session state in `.mentci/`. | **ContextualSessionProtocol.** Prompt context and agent intent are now stored in the `jj` commit description. |
| `solar_prefix` | Calling `chronos` for headers. | **Chronos Actor.** The new `execute` orchestrator will call the `chronos` library logic directly. |

## 3. High-Authority Migration (Group B: Port to Actors)

The core repository guards and flow controllers are the primary targets for the `ractor` migration.

1.  **`root_guard`**: Migration to `RootGuardActor`. Checks FS integrity against `AskiFsRootContract.edn`.
2.  **`reference_guard`**: Migration to `LinkGuardActor`. Scans for cross-tier link violations.
3.  **`session_guard`**: Migration to `SessionGuardActor`. Verifies the commit graph state.
4.  **`session_finalize`**: Migration to `SessionOrchestrator`. This is the central actor managing the end-of-session synthesis.

## 4. Proposed Migration Path

The transition will be executed through the `execute` component, evolving it from a subprocess runner to a **Symbolic Orchestrator**.

### 4.1 Phase 1: The Unified Supervisor
- Implement a `RootActor` in `execute.rs`.
- The `RootActor` will supervise child actors for each migrated script.
- CLI arguments will be parsed into `SymbolicMessages`.

### 4.2 Phase 2: Parallel Verification
- Actors for `RootGuard` and `ReferenceGuard` will be implemented.
- The `RootActor` will execute these in parallel, significantly improving performance compared to serial `bb` calls.

### 4.3 Phase 3: Script Removal
- Once a Rust actor achieves parity with its Clojure counterpart, the corresponding `.clj` directory will be deleted.

## 5. Decision Log
*   **Obsolete Scripts:** `logger`, `session_metadata`, and `solar_prefix` are marked for removal in the next implementation slice.
*   **Target Framework:** `ractor` (Confirmed for Erlang-style supervision and single-owner flow).
