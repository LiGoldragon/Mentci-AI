# Mentci-AI Tool Stack

This file documents the underlying stack of all custom tools introduced to the project, adhering to Section 0.3 of the Architectural Guidelines.

## 1. Nix Jail Launcher
*   **Language/Runtime:** Python 3.11 / Nix (Pure Derivations)
*   **Origin:** Custom (`jail.nix`, `jail_launcher.py`)
*   **Rationale:** Core environment orchestration for Level 5 isolation. Replaces Bash logic with robust Python data-driven mapping.

## 2. MachineLog Logger
*   **Language/Runtime:** Python 3.11
*   **Dependencies:** `edn_format` (vendored from `swaroopch/edn_format`)
*   **Origin:** Custom (`scripts/logger.py`)
*   **Rationale:** Enforces the Handshake Logging Protocol using EDN for historical authority and conciseness.

## 3. CriomOS Pre-Fetch Orchestrator
*   **Language/Runtime:** Python 3.11 / Nix CLI
*   **Origin:** Custom (`scripts/prefetch_orchestrator.py`)
*   **Rationale:** Provides a deterministic, cryptographic bridge between isolated agents and the Nix daemon for URL and Git prefetching.

## 4. Log Converter (Jet)
*   **Language/Runtime:** Clojure / GraalVM (Native Binary)
*   **Origin:** `nixpkgs#jet`
*   **Rationale:** Industry standard CLI for high-performance JSON/EDN/YAML transformations. Used to convert all project data structures to and from EDN, enforcing EDN as the authoritative and favored data-storage language for Mentci-AI.
