# Non-Interactive Execution Strategy (Development)

## Subject: Automation Integrity

### Mandatory Environment (Nix Shell)
The following variables must be maintained in `Components/nix/dev_shell.nix`:
- `CI=true`
- `DEBIAN_FRONTEND=noninteractive`
- `GIT_TERMINAL_PROMPT=0`
- `GH_PROMPT_DISABLED=1`
- `TERM=dumb`
- `PAGER=cat`

### JJ Configuration
- A project-local `.mentci/jj-project-config.toml` must exist with `ui.pager = "cat"` and `ui.editor = "true"`.
- `dev_shell.nix` must use `shellHook` to export `JJ_CONFIG` with an absolute path to this file.

### Implementation Patterns
1. **Always `-m`:** Use message flags for all CLI calls that might trigger an editor.
2. **Batch Mode:** Prefer `--batch` or `--non-interactive` flags where available.
3. **Redirection:** Pipe `yes` into interactive-only legacy tools.
4. **Shell Invariant:** Never allow a command to hang; if a command is known to be interactive, wrap it in a timeout or provide a mock TTY.
