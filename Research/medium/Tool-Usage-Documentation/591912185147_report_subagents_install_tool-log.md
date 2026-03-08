# Tool Usage Log — Subagents Install + Usage Research

- **Solar:** `5919.12.18.51.47`
- **Programming:** `3wyybz4j`
- **Scope:** install `@oh-my-pi/subagents` into Nix `pi-with-extensions` and document practical usage.

## Tool usage timeline

1. **State and workflow checks (`bash`)**
   - `git status`, `git log -5`, `jj status`
   - Outcome: JJ worktree clean; non-authoritative git dirt exists outside scoped files.

2. **Config/source inspection (`read`)**
   - Read Nix composition files:
     - `Components/nix/default.nix`
     - `Components/nix/pi-with-extensions.nix`
     - `Components/nix/pi_with_extensions_check.nix`
     - `flake.nix`
   - Purpose: identify extension wiring points.

3. **Package discovery (`bash`)**
   - `npm view @oh-my-pi/subagents version`
   - `nix-prefetch-url` for tarball hash
   - Purpose: pin reproducible source.

4. **Mutation (`write`/`edit`)**
   - Added new package file: `Components/nix/pi-subagents-extension.nix`
   - Wired through default namespace, wrapper loading, checks, and flake package outputs.

5. **Build verification (`bash`)**
   - `nix build .#checks.x86_64-linux.piWithExtensions`
   - `nix build .#packages.x86_64-linux.piWithExtensions`
   - Outcome: successful after resolving packaging approach.

6. **Installed extension introspection (`read`)**
   - Read installed extension files from built output (`result/.../@oh-my-pi/subagents/*`), including:
     - `README.md`, `package.json`, `tools/index.ts`, command/agent markdown.
   - Purpose: document real shipped behavior and usage shape.

## Shortcomings encountered

1. **Initial `buildNpmPackage` lane failed**
   - package has no lock/deps payload suitable for that flow.
   - Resolved by switching to `stdenvNoCC` tarball unpack packaging (same strategy used by simple extension artifacts).

2. **Legacy sema-flow prompt drift**
   - Existing `.pi/prompts/sema-flow.md` is historical and not treated as current authority without review.
   - Research report explicitly marks it for refresh.

## Result

- Subagents extension is now available in Nix-wrapped `pi` runtime.
- Research artifact documents function-scoped usage and practical workflow recommendations.
