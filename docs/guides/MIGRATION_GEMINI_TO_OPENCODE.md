# Migration Draft: Gemini CLI → OpenCode CLI

## 1. Overview
This document outlines the transition from the temporary **Gemini CLI** (running `gemini-3-flash-preview`) to the target **OpenCode CLI** (integrated with **DeepSeek-V4**). This shift completes the Level 5 agentic stack as defined in `RESTART_CONTEXT.md`.

## 2. Infrastructure Changes

### 2.1. Nix Packaging for OpenCode
Currently, `opencode` is included as a raw source input (`inputs/untyped/opencode`). To make it operational:
- **Action:** Define a `pkgs.opencode` derivation in `flake.nix`.
- **Implementation:** Use `python3Packages.buildPythonApplication` to wrap the OpenCode source.
- **Exposure:** Add `pkgs.opencode` to `devShells.default` packages.

### 2.2. Standardizing Model Identification
To prevent audit discrepancies:
- **Action:** Introduce the `MENTCI_MODEL` environment variable.
- **Implementation:** 
    - Gemini CLI should set `MENTCI_MODEL="gemini-3-flash-preview"`.
    - OpenCode should set `MENTCI_MODEL="DeepSeek-V4"`.
- **Update:** Use `jj log` as the audit trail; remove logger dependencies if present.

### 2.3. OpenCode Provider Configuration
OpenCode requires a provider configuration to communicate with DeepSeek.
- **Action:** Create `~/.config/opencode/config.edn` (or project-local `.opencode.edn`).
- **Template:**
  ```edn
  {:providers 
   {:deepseek 
    {:api-key "ENV_DEEPSEEK_API_KEY"
     :model "deepseek-reasoner"}}}
  ```

## 3. Workflow Transition

### Step 1: Initialize the Jail
Run `nix develop` to ensure all inputs are symlinked via `jail_launcher.py`.

### Step 2: Build OpenCode
Verify `opencode --version` is accessible within the nix shell.

### Step 3: Switch Agents
1. Close the current Gemini CLI session.
2. Open a new terminal.
3. `cd` into `Mentci-AI`.
4. Run `opencode`.

## 4. Verification Matrix
- [x] `opencodePkg` defined in `flake.nix`.
- [x] `commonPackages` includes `opencodePkg`.
- [x] `.opencode.edn` created in project root.
- [ ] `which opencode` returns a path in the Nix store (Requires `nix develop`).
- [x] `jj log` is the audit trail for model identification.
- [ ] Agent can execute `gdb --version` (verifying Level 5 tool access).

## 5. Timeline
The transition should be completed before the **♈︎ 1.1.1** (5920 Anno Mundi) release.
