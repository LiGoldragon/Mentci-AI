# Rust Components Repo Split Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Split Rust components into dedicated repos while preserving one-directory agent workflow via submodules and making `flake.lock` the authoritative last-known-good integration state.

**Architecture:** Use a declarative component manifest as the single data authority for component path/repo/input mapping. Keep the super-repo as integration orchestrator. Track component checkouts as git submodules for co-development UX, but pin integration builds via flake inputs + lockfile. Add verification tooling in Rust (`mentci-vcs`) to enforce submodule/lock alignment policy.

**Tech Stack:** Rust (`mentci-vcs`), TOML + JSON parsing (`serde`, `toml`, `serde_json`), Jujutsu/Git, Nix flakes.

---

### Task 1: Add split-authority data manifest

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Create: `Components/contracts/rust-component-repos.toml`

**Step 1: Write the failing test**
- Add parser test in `Components/mentci-vcs/src/lib.rs` expecting at least one component from manifest.

**Step 2: Run test to verify it fails**
Run: `cargo test -p mentci-vcs manifest -- --nocapture`
Expected: FAIL due to missing parser/types.

**Step 3: Write minimal implementation**
- Create manifest with all current Rust components and desired fields (`id`, `path`, `flake_input`, `repo`, `required_submodule`, `required_flake_lock`).

**Step 4: Run test to verify it passes**
Run: `cargo test -p mentci-vcs manifest -- --nocapture`
Expected: PASS.

**Step 5: Commit**
```bash
jj describe -m "intent: Add Rust component split manifest and policy scaffold"
jj bookmark set dev -r @
jj git push --bookmark dev
```

### Task 2: Implement Rust policy checker in mentci-vcs

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Modify: `Components/mentci-vcs/Cargo.toml`
- Modify: `Components/mentci-vcs/src/lib.rs`
- Modify: `Components/mentci-vcs/src/main.rs`
- Test: `Components/mentci-vcs/src/lib.rs` (unit tests)

**Step 1: Write failing tests**
- Add tests for:
  - manifest parsing
  - flake.lock input resolution
  - strict-policy violation detection

**Step 2: Run tests to verify they fail**
Run: `cargo test -p mentci-vcs -- --nocapture`
Expected: FAIL with missing symbols.

**Step 3: Implement minimal checker**
- Add object types for manifest + lock nodes.
- Add checker that inspects:
  - git submodule mode via `git ls-files --stage <path>`
  - flake lock input presence via `flake.lock` root inputs and node rev
- Add CLI subcommand: `component-split-status`.

**Step 4: Run tests to verify pass**
Run: `cargo test -p mentci-vcs -- --nocapture`
Expected: PASS.

**Step 5: Commit**
```bash
jj describe -m "intent: Add mentci-vcs split-status checker for submodule and flake-lock policy"
jj bookmark set dev -r @
jj git push --bookmark dev
```

### Task 3: Document migration + promotion protocol

**TDD scenario:** Trivial change — use judgment

**Files:**
- Create: `Research/high/Agent-Authority-Alignment/591912163000_report_rust_component_submodule_flake_lock_governance.md`

**Step 1: Draft document**
- Explain dual-truth model:
  - submodules for local co-development
  - flake.lock for LKG
- Define promotion gate: tests on dev before lock update.

**Step 2: Verify references and commands**
Run:
- `jj status`
- `cargo run -p mentci-vcs -- component-split-status`
Expected: command executes and reports actionable status.

**Step 3: Commit**
```bash
jj describe -m "intent: Document component split governance and promotion protocol"
jj bookmark set dev -r @
jj git push --bookmark dev
```

### Task 4: Workspace verification and handover

**TDD scenario:** Modifying tested code — run existing tests first

**Files:**
- No additional source files (verification only)

**Step 1: Run targeted checks**
Run:
- `cargo test -p mentci-vcs`
- `cargo check --workspace`

**Step 2: Record evidence in final commit message**
- Include command outputs summary in Validation section.

**Step 3: Push and handover**
```bash
jj bookmark set dev -r @
jj git push --bookmark dev
jj new
```
