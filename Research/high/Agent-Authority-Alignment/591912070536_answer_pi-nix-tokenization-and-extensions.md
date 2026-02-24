# Agent Report

- Chronography: `12.7.5.36 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Agent-Authority-Alignment`
- Title: `pi-nix-tokenization-and-extensions`

## Prompt

are there pi (the ai coding tui packaged in this repo) extensions to let the agent do live web searches? what are all the extensions to give pi all the latest agent features? [...] has our packaged version of pi fixed the nix path problem described in this response[1] from gemini pro about using nix with extension (does the problem apply to pi installed from nix as well?) - can extensions be nix packaged now using the fix mentionned, or any other subsequent or additional fixes?


## Agent Answer

Conducted an investigation into the `pi-mono` extension ecosystem and its compatibility with Nix-based installations, specifically focusing on the "Nix Tokenization Problem."

### 1. Investigation: Pi Extensions & Skills
The `pi` agent (v0.54.2) leverages a dual system of **Extensions** (TypeScript modules) and **Skills** (CLI tools the agent learns via documentation).
- **Web Search:** Provided via `@aliou/pi-linkup` (Linkup API), `@juanibiapina/pi-search` (Brave Search), or the `agent-tools` collection.
- **Latest Features (oh-my-pi ecosystem):**
    - **Plan Mode:** `@juanibiapina/pi-plan` provides a read-only strategy phase (`--plan`).
    - **Task Tool (Subagents):** `@oh-my-pi/subagents` provides parallel execution and specialized roles (`explore`, `plan`, `reviewer`).
    - **LSP Integration:** `@oh-my-pi/lsp` enables real-time diagnostics and code intelligence.
    - **MCP Support:** `mcporter` enables use of any Model Context Protocol servers.
    - **Batteries-Included:** The `oh-my-pi` fork integrates these by default.

### 2. Investigation: Nix Tokenization Problem
The "Nix Tokenization Problem" occurs when absolute Nix store paths (containing long, random hashes) leak into the system prompt (e.g., via docs/examples paths), wasting context tokens.
- **Fix Status:** The `pi` version used (**0.54.2**) includes the `PI_PACKAGE_DIR` fix. This variable allows overriding the internal package path to a stable, non-hashed symlink (e.g., `~/.pi/pi-source`).
- **Nix Packaging:** Updated `Components/nix/pi.nix` to include `PI_PACKAGE_DIR` and `PI_AI_ANTIGRAVITY_VERSION` in the wrapper.
- **Implementation Note:** The `devShell` now includes an automated `shellHook` that establishes the stable symlink (`~/.pi/pi-source`) and exports `PI_PACKAGE_DIR` automatically upon entry, ensuring the tokenization fix is active by default in the development environment.
- **Environment Variables:** Added `PI_AI_ANTIGRAVITY_VERSION = "1.23.0"` to the `devShell` and the `pi` wrapper for consistent identification.
- **Extension Compatibility:** Extensions packaged in Nix still suffer from token bloat if their absolute store paths are included in the prompt. Symlinking Nix-managed extensions into the stable `~/.pi/agent/extensions/` directory is the recommended workaround.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
