# VT Code: Programmatic and Headless Execution

- **Solar:** 5919.12.9.12.0
- **Subject:** `VTCode-Inspection`
- **Status:** `active-research`
- **Weight:** `High`

## 1. Headless Execution Modes

VT Code supports full automation through dedicated CLI subcommands and flags, allowing it to be used in Nix tests, CI/CD pipelines, and background agent loops.

### 1.1 `vtcode exec` (Primary Headless Entry)
Used to execute a single task without human interaction.
- **Usage:** `vtcode exec "Your prompt here"`
- **Stdin support:** Use `-` to read the prompt from a pipe: `cat prompt.txt | vtcode exec -`
- **JSON Output:** `--json` emits structured event logs to `stdout`.

### 1.2 `vtcode --full-auto`
An alias/flag to enable non-interactive mode for interactive commands.
- Forces automatic approval of tool calls (if permitted by policy).
- Prevents the TUI from launching.

## 2. Nix Integration & Testing

To use `vtcode` in a Nix test or derivation:
1.  **Inject Secrets:** Use `mentci-user export-env` to hydrate the environment with required API keys.
2.  **Redirect Output:** Send reasoning traces to `stderr` and final artifacts to `stdout` or files using `--last-message-file <PATH>`.

### Example Nix Test Stub
```nix
pkgs.runCommand "vtcode-test" {
  nativeBuildInputs = [ mentci-user vtcode ];
} ''
  # Setup environment via mentci-user
  eval "$(mentci-user export-env $MENTCI_USER_SETUP_BIN)"
  
  # Run headless execution
  vtcode exec "Verify the structure of $(pwd)" --json > results.json
  
  # Assertions
  grep -q "SUCCESS" results.json
  touch $out
''
```

## 3. Tool Policy Gating (Headless)
When running headlessly, policy enforcement shifts from "prompt" to "allow/deny".
- Configure `.vtcode/vtcode.toml` with `human_in_the_loop = false`.
- Use `allowed-tools` flag to restrict execution boundaries in headless environments.
