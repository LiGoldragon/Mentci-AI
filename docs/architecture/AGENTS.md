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

## 2. Handshake Logging Protocol

Every prompt fulfillment must be logged to maintain historical authority.

1.  **Extract Metadata:** Read the `Timestamp`, `UserId`, and `Model Version` from the start of the user prompt.
2.  **Derive Intent:** Formulate a concise (1-sentence) summary of your high-level intent.
3.  **Log:** Execute `scripts/logger.py "<intentSummary>" --model "<model>" --user "<userId>"`.
4.  **Format:** Logs are stored in **EDN** format in the `Logs/` directory.

## 3. Structural Rules

*   **Python Mandate:** All glue code and scripts must be written in Python. No Bash logic.
*   **EDN Authority:** Favor EDN for all data storage and state persistence. Use `jet` for transformations.
*   **Sema Object Style:** Strictly follow the ontology defined in `schema/*.capnp`.
*   **Source Control:** Atomic, concise commits to the `dev` bookmark using `jj`. Push immediately after structural changes.
*   **Tagging:** When creating git tags, always use the `-m` flag to provide a message directly (e.g., `git tag -a vX.Y.Z -m "release: vX.Y.Z"`) to avoid interactive editor prompts.

## 4. Admin Developer Mode

High-authority agents (like Mentci) operate in Admin Developer Mode. You are responsible for the system's evolution toward Level 6 instinctive symbolic interaction.
