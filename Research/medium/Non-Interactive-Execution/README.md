# Non-Interactive Execution Research

## Subject: Suppressing Interactive CLI Prompts

### Findings
To prevent agents from getting stuck on interactive terminals, a set of standard environment variables and CLI flags should be used.

### Global Environment Variables (Added to `dev_shell.nix`)
The following variables have been added to the Nix development shell:
- `CI=true`: Signals to most modern tools (npm, yarn, jest) that they are running in an automated environment.
- `DEBIAN_FRONTEND=noninteractive`: Prevents `apt-get` and other Debian-based tools from prompting for input.
- `GIT_TERMINAL_PROMPT=0`: Disables Git's terminal-based password/credential prompts.
- `GH_PROMPT_DISABLED=1`: Disables interactive prompts for the GitHub CLI.
- `TERM=dumb`: Sets the terminal type to one without advanced features, often disabling interactive UI and colored output.
- `PAGER=cat`: Ensures that commands like `git log` or `man` do not open an interactive pager.

### Tool-Specific Recommendations

#### Git
Always use the `-m` flag for commits and tags to avoid opening an editor.
```bash
git commit -m "intent: message"
git tag -a v1.0.0 -m "release: message"
```

#### Nix
When using `nix build` or `nix shell`, use the `--no-link` or non-interactive flags where possible.

#### Node.js / NPM
Use `--yes` or `-y` with `npm init` or `npx` commands.
```bash
npx create-react-app my-app --yes
```

#### General Fallback
If a tool does not support a non-interactive mode, use `yes` to pipe default answers:
```bash
yes | command-that-prompts
```
