# VTCode Integration and API Key Abstraction

- **Solar:** 5919.12.8.54.53
- **Subject:** `VTCode-Inspection`
- **Status:** `implemented`
- **Weight:** `High`

## 1. Intent
To provide an implementation for trialing `vtcode` as a potential replacement/supplement for `pi` within the Mentci-AI development shell. This includes incorporating `vtcode` into Nix packaging, defining structured configurations, and standardizing the API key abstraction layer across all agents (`pi`, `vtcode`, `gemini-cli`, `codex`).

## 2. API Key Abstraction in Mentci-AI (Logic-Data Separation)
Mentci already implements a strict logic-data separation for API keys.
*   **Storage:** Keys are not stored in `.env` files or hardcoded shell scripts. They reside in a structured JSON configuration managed by the `mentci-user` Rust binary (`~/.mentci/user.json` by default).
*   **Abstraction Layer:** The `mentci-user export-env` command translates the structured `user.json` data into environment variables required by various LLM CLI tools.
*   **Injection:** The Nix `dev_shell.nix` automatically invokes `eval "$(mentci-user export-env)"` during shell initialization. This guarantees that *any* agent or CLI tool executed inside the `mentci-ai-dev` shell inherits the correct API keys (like `GEMINI_API_KEY`) without directly parsing configuration files themselves.

## 3. VTCode Nix Packaging
VTCode does not natively provide a Nix flake, so it was manually incorporated directly into the Mentci-AI `flake.nix` inputs:
1.  **Source Fetching:** `github:vinhnx/vtcode/v0.83.0` is pulled declaratively.
2.  **Derivation:** `Components/nix/vtcode.nix` builds the Rust workspace using `rustPlatform.buildRustPackage`.
3.  **Shell Availability:** The compiled `vtcode` binary is exposed to `common_packages` and the `devShell`.

## 4. VTCode Configuration (Structured Data)
VTCode consumes its primary configuration through a `.vtcode/vtcode.toml` file. I have created a project-local `vtcode.toml` in the repository root to apply Mentci's safety principles and link to our abstracted API keys:
*   **Provider Binding:** Configured `provider = "gemini"` and `model = "gemini-2.5-flash"`.
*   **Key Mapping:** Instructed VTCode to read the key via `api_key_env = "GEMINI_API_KEY"` (which `mentci-user` successfully exports).
*   **Tool Policies:** Explicitly set baseline tool policies (`run_terminal_cmd = "allow"`, `write_file = "allow"`, etc.) for seamless trial usage without repetitive `human_in_the_loop` interruptions.

## 5. Wrapper Execution (`mentci-vtcode`)
To guarantee that the structured key data is available even if VTCode is invoked outside the fully initialized Nix dev shell, a `mentci-vtcode` bash wrapper has been added to `common_packages.nix`.
It executes:
```bash
if command -v mentci-user >/dev/null 2>&1; then
  eval "$(mentci-user export-env 2>/dev/null)"
fi
exec vtcode "$@"
```
This serves as the universal abstraction glue, ensuring `vtcode` runs correctly in any context while maintaining the "Mentci-User handles secrets" security boundary.