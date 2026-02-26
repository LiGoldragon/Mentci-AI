# VTCode Integration Architecture

**Status:** Active  
**Component:** `vtcode` (Terminal UI / Coding Agent)

This document follows the `UserInterfaceDocumentation.md` guidelines to describe the integration of the VTCode agent harness into the Mentci-AI ecosystem.

## 1. Component Identity
- **Tool:** `vtcode` (Rust-based semantic terminal coding agent).
- **Source:** Pulled directly from `github:vinhnx/vtcode` via the `flake.nix` input `vtcode-src`.
- **Derivation:** `Components/nix/vtcode.nix` uses `pkgs.makeRustPlatform` to build the agent from source.
- **Availability:** Exported as a package (`vtcode`) inside the `mentci-ai-dev` Nix shell.

## 2. Configuration & State
VTCode's configuration is managed through structured TOML, maintaining deterministic project behavior.
- **File Location:** `.vtcode/vtcode.toml`
- **Provider Settings:** Configured to use the fallback/default model (e.g., `gemini-2.5-flash`).
- **Tool Policies:** The policy engine is explicitly configured (`default_policy = "allow"`) for safe testing without interrupting the workflow loop constantly.
- **Data Boundary:** Logs, cache, and state are isolated to `.vtcode/` to prevent working copy pollution.

## 3. Secret & Environment Injection (Logic-Data Separation)
VTCode requires an API key to function, but we *strictly forbid* committing `.env` files or hardcoding secrets.
- **Expected Variable:** `GEMINI_API_KEY`
- **Injection Path:** The `mentci-user` Rust binary handles secret material natively. It reads `~/.mentci/user.json` and exposes it via `mentci-user export-env`.
- **Shell Hook:** When `nix develop` is run, the shell evaluates `$(mentci-user export-env)`, cleanly hydrating the environment with the required API key for `vtcode` to consume.

## 4. Wrapper / Launch Script
If VTCode is launched outside the fully instantiated `nix develop` shell or needs a forced refresh of the secrets layer, a wrapper script is provided.

- **Wrapper:** `mentci-vtcode` (defined in `Components/nix/common_packages.nix`)
- **Execution Flow:**
  ```bash
  #!/usr/bin/env bash
  if command -v mentci-user >/dev/null 2>&1; then
    eval "$(mentci-user export-env 2>/dev/null)"
  fi
  exec vtcode "$@"
  ```
This guarantees VTCode always launches with the correct environment abstractions without needing manual setup from the user.
