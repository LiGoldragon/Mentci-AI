# Research Artifact: Nix store paths and Pi Extensions

- Chronography: `12.8.31.00 | 5919 AM`
- Subject: `Agent-Authority-Alignment`
- Title: `nix-store-paths-and-pi-extensions`
- Status: `active`

## 1. Problem Statement

The project uses a Nix-installed `pi` agent, while extensions are currently installed statefully via `npm` into the repository root (`.pi/npm/node_modules`). There is a concern that Nix store paths (containing long random hashes) consume excessive context window tokens and confuse the LLM ("Nix Tokenization Problem"). 

Furthermore, the `linkup` extension recently failed to register because it performs an API key check at load time, and the environment variable was not yet set when the session started.

## 2. Findings

### 2.1. The Nix Tokenization Problem
Absolute paths like `/nix/store/sbdmbpvdl18v2c3sp3rsyjzrssn4zbwy-pi-dev-0.55.0/lib/node_modules/pi/` are problematic in system prompts. We have already mitigated this for `pi`'s own source code by using the `PI_PACKAGE_DIR` environment variable pointing to a stable symlink (`~/.pi/pi-source`).

### 2.2. Extension Path Sensitivity
Currently, extensions are located in the repository under `.pi/npm/node_modules/`. Their paths are reported as absolute local paths (e.g., `/home/li/git/Mentci-AI/.pi/npm/node_modules/@aliou/pi-linkup`). While these are long, they do not contain the random Nix hashes that cause the "Tokenization Problem."

However, if we were to package extensions in Nix (for better reproducibility), they would each receive a hashed store path, re-introducing the problem.

### 2.3. Registration Failures
The `pi-linkup` extension (and others) checks for required secrets (like `LINKUP_API_KEY`) at the moment the module is loaded. Because the agent session begins before the `mentci-user` secret injection has completed or because the environment is inherited at startup, the extension may disable its tools if the key is missing.

## 3. Best Step Forward

### 3.1. Relinking out of the Nix Store
We should adopt a "Universal Stable Path" strategy for both the `pi` package and all extensions. 
- **Action:** Modify the `pi` Nix wrapper and `dev_shell.nix` to maintain a stable extensions directory (e.g., `~/.pi/extensions`).
- **Action:** If an extension is Nix-packaged, it should be symlinked into this stable directory.
- **Action:** If an extension is statefully installed, its path should be aliased or shortened in the agent's context.

### 3.2. Improving the Pi Package
The `pi` package should be updated to:
1.  **Favor the stable symlink paths** when reporting tool/extension locations to the model.
2.  **Lazy-load secrets:** Tools should check for their API keys at execution time rather than just at startup, or `pi` should be integrated with `mentci-user` to refresh the environment.

### 3.3. Corrective Implementation (Immediate)
1.  **Update `dev_shell.nix`:** Ensure `mentci-user export-env` is run at the very beginning of the `shellHook`.
2.  **Shorten Paths:** Update the `pi` system prompt instructions to treat paths relative to the stable symlinks.

## 4. Suggestions for the Author

1.  **Nix-Package Popular Extensions:** We should move the most common extensions (`linkup`, `subagents`, `lsp`) into the Nix flake. This ensures they are version-controlled and immutable, just like `pi`.
2.  **Automated Extension Relinking:** We can write a Rust utility (or add a command to `mentci-user`) that automatically scans the Nix store for `pi` extensions and maintains a stable symlink tree in `~/.pi/extensions`.

## 5. Questions for the Author

1.  **Extension Purity:** Do you prefer keeping extensions in the git repo (`.pi/npm/`) for easy "self-extension" by the agent, or should we prioritize Nix purity and move them to the flake?
2.  **Context Shortening:** Should I implement a `pi` extension that specifically filters the system prompt to replace any occurrence of a Nix store path with a shortened alias?
