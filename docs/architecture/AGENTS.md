# Mentci-AI Agent Instructions

This document provides non-negotiable instructions for AI agents operating within the Mentci-AI ecosystem. These rules ensure architectural integrity and cryptographic provenance.

## 1. Environment & Isolation

Agents execute within a **Nix Jail**. All operations must be performed using the provided tools. Direct network access from the sandbox is forbidden.

### 1.1 Pre-Fetch Orchestrator
To acquire external inputs (tarballs, git repos), you must use the **CriomOS Pre-Fetch Orchestrator** MCP server located at `scripts/prefetch_orchestrator.py`.

*   **Transport:** Communication is via `stdio` using JSON-RPC.
*   **Command:** `python3 scripts/prefetch_orchestrator.py --mcp`
*   **Methods:**
    *   `prefetch_url(url, unpack=False)` -> Returns SRI hash.
    *   `prefetch_git(url, rev=None, submodules=False)` -> Returns SRI hash, rev, and store path.

## 2. Audit Trail

Use `jj log` as the authoritative audit trail for work performed in the repository.

## 3. Structural Rules

*   **Clojure (Babashka) Mandate:** All glue code and scripts must be written in Clojure (Babashka). No Bash logic beyond the one-line bb shim.
*   **Script Typing:** All Clojure scripts must define Malli schemas for inputs/config and validate them.
*   **EDN Authority:** Favor EDN for all data storage and state persistence. Use `jet` for transformations.
*   **Sema Object Style:** Strictly follow the ontology defined in `schema/*.capnp`.
*   **Source Control:** Atomic, concise commits to the `dev` bookmark using `jj`. Follow the per-prompt dirty-tree auto-commit rule in `docs/architecture/VERSION_CONTROL.md`.
*   **Tagging:** When creating git tags, always use the `-m` flag to provide a message directly (e.g., `git tag -a vX.Y.Z -m "release: vX.Y.Z"`) to avoid interactive editor prompts.

## 4. Admin Developer Mode

High-authority agents (like Mentci) operate in Admin Developer Mode. You are responsible for the system's evolution toward Level 6 instinctive symbolic interaction.
