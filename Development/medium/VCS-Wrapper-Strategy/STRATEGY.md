# VCS Wrapper Strategy (Development)

## Subject: `mentci-vcs` Rust Implementation

### Objective
Provide a unified, strictly non-interactive interface for VCS operations (`jj`, `git`) to support Level 5 agentic workflows.

### Architecture
- **Trait-based:** `VCS` trait in `lib.rs` defines core operations (`commit`, `status`, `diff`).
- **Implementation:** `Jujutsu` struct handles specific `jj` command construction with environment overrides.
- **Environment Isolation:** Forces `JJ_CONFIG` to `.mentci/jj-project-config.toml` and sets `PAGER=cat`, `CI=true`.

### Roadmap
1. **Initial Scaffold (Complete):** Basic `lib.rs` and `main.rs` in `Components/mentci-vcs`.
2. **Nix Integration (Complete):** Packaged via `crane` and added to `common_packages`.
3. **Command Extension:** Implement `jj squash`, `jj describe`, and bookmark management.
4. **Context Synthesis:** Add logic to generate protocol-compliant `session:` messages from within the wrapper.
5. **Agent Handoff:** Update `mentci-aid` to use `mentci-vcs` for all repository state changes.

### Guidelines
- Every command must explicitly set non-interactive flags.
- Errors must be captured and returned via `anyhow` with high-fidelity context.
- The wrapper must never open a TUI or editor.
