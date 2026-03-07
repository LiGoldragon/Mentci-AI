# jCodeMunch Pi Extension Support Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Add Nix-pinned jCodeMunch support to the project’s `piWithExtensions` runtime so Pi can load MCP adapter tooling and execute `jcodemunch-mcp` deterministically.

**Architecture:** Package two independent artifacts in Nix: (1) `jcodemunch-mcp` Python application, (2) `pi-mcp-adapter` Pi extension package. Inject both into `pi-with-extensions` wrapper: register the extension via `--extension` and prepend PATH so adapter-managed MCP servers can execute `jcodemunch-mcp`. Keep Linkup extension behavior intact.

**Tech Stack:** Nix derivations (`buildPythonApplication`, `buildNpmPackage`), Pi extension loading flags, Nix flake checks.

---

### Task 1: Add failing runtime expectation check

**TDD scenario:** New feature — full TDD cycle.

**Files:**
- Modify: `Components/nix/pi_with_extensions_check.nix`

**Step 1:** Add assertions that fail before implementation:
- `pi-mcp-adapter` exists in `piWithExtensions` node_modules
- `jcodemunch-mcp` binary exists in `piWithExtensions` runtime PATH wrapper surface

**Step 2:** Run failing check:
- `nix build .#checks.x86_64-linux.piWithExtensions`

**Step 3:** Confirm expected failure

**Step 4:** Commit intent checkpoint

### Task 2: Package jcodemunch + pi-mcp-adapter in Nix

**TDD scenario:** New feature — full TDD cycle.

**Files:**
- Create: `Components/nix/jcodemunch-mcp.nix`
- Create: `Components/nix/pi-mcp-adapter-extension.nix`
- Modify: `Components/nix/default.nix`

**Step 1:** Define `jcodemunch-mcp` Python package from PyPI with explicit dependency wiring.

**Step 2:** Define `pi-mcp-adapter` extension package from npm tarball via `buildNpmPackage`.

**Step 3:** Wire both into nix namespace exports in `default.nix`.

**Step 4:** Build package-level targets to validate derivations.

### Task 3: Integrate into pi-with-extensions runtime

**TDD scenario:** Modifying tested code — run updated checks before/after.

**Files:**
- Modify: `Components/nix/pi-with-extensions.nix`
- Modify: `Components/nix/default.nix`
- Modify: `Components/nix/common_packages.nix`

**Step 1:** Inject extension symlink for `pi-mcp-adapter` alongside Linkup.

**Step 2:** Add wrapper `--extension` flag for `pi-mcp-adapter`.

**Step 3:** Prepend PATH with packaged `jcodemunch-mcp` binary in wrapper.

**Step 4:** Include `jcodemunch-mcp` in dev shell package set.

### Task 4: Project-level MCP server config for jcodemunch

**TDD scenario:** New file config — verify runtime consumption.

**Files:**
- Create: `.pi/mcp.json`

**Step 1:** Add deterministic MCP server entry using `command: "jcodemunch-mcp"` and sane defaults.

**Step 2:** Verify JSON validity and existence in tree.

### Task 5: Validate and ship on main

**TDD scenario:** Verification phase.

**Files:**
- Modify as needed from prior tasks.

**Step 1:** Run validation:
- `nix build .#piWithExtensions`
- `nix build .#checks.x86_64-linux.piWithExtensions`
- `nix build .#pi`

**Step 2:** Runtime smoke:
- `${result}/bin/pi --help` (or equivalent built output check)

**Step 3:** Commit with protocol-complete message and push `main`.

**Step 4:** End session with clean empty commit (`jj new main`).
