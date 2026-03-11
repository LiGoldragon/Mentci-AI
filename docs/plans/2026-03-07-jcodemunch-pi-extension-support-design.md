# jCodeMunch Pi Extension Support Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Add Nix-pinned jCodeMunch support to the project’s `piWithExtensions` runtime so Pi can load MCP adapter tooling and execute `jcodemunch-mcp` deterministically.

**Architecture:** Package two independent artifacts in Nix: (1) `jcodemunch-mcp` Python application, (2) `pi-mcp-adapter` Pi extension package. Inject both into `pi-with-extensions` wrapper: register the extension via `--extension` and prepend PATH so adapter-managed MCP servers can execute `jcodemunch-mcp`. Keep Linkup extension behavior intact.

**Tech Stack:** Nix derivations (`buildPythonApplication`, `buildNpmPackage`), Pi extension loading flags, Nix flake checks.

---

### JJ Mechanics Reminder

- Target the resolved runtime bookmark in `$MENTCI_TARGET_BOOKMARK` for asserts, commits, and pushes; `main` is reserved for release-only flows and should not be used in routine development steps. When a side history is required, name it explicitly (for example `side/jcodemunch-design`) so its intent is clear.
- Reference change IDs as the durable work identifier; commit IDs flow from them. Seeing duplicate visible change IDs usually indicates intentional divergence or history exposure, not corruption, so describe them accurately instead of assuming a broken graph.
- Anonymous empty working-copy commits are the normal handoff state for the next prompt. Avoid publishing described empty commits or bookmarks anchored on empty revisions, and never move the runtime bookmark to `@` or another empty commit unless there is a compelling reason.
- Do not finalize a clean tree without an explicit reason. Always verify the runtime bookmark points to a meaningful change and avoid `jj describe`/`jj push` commands that would leave the bookmark on an empty commit.

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

### Task 5: Validate and ship on the runtime bookmark

**TDD scenario:** Verification phase.

**Files:**
- Modify as needed from prior tasks.

**Step 1:** Run validation:
- `nix build .#piWithExtensions`
- `nix build .#checks.x86_64-linux.piWithExtensions`
- `nix build .#pi`

**Step 2:** Runtime smoke:
- `${result}/bin/pi --help` (or equivalent built output check)

**Step 3:** Commit with a protocol-complete message and push `"$MENTCI_TARGET_BOOKMARK"` (only lift `main` directly if this iteration becomes an explicit release).

**Step 4:** Leave the working copy clean, note the validation evidence, and prepare the next handoff by running `jj new "$MENTCI_TARGET_BOOKMARK"` so the runtime bookmark remains on the verified change instead of an empty commit.
