# Research Report: Jujutsu (jj) MCP Servers and Integration

## 1. Goal
Evaluate available Model Context Protocol (MCP) servers for the Jujutsu (jj) version control system to enable high-authority agentic management of repository history.

## 2. Identified External MCP Servers

Research via Linkup and GitHub identifies two primary candidates:

### A. `keanemind/jj-mcp-server`
*   **Repo:** [https://github.com/keanemind/jj-mcp-server](https://github.com/keanemind/jj-mcp-server)
*   **Focus:** Exposes core `jj` operations as programmable tools (status, log, diff, rebase, commit, etc.).
*   **Style:** Direct command wrapping for automation and scripting.

### B. `jasagiri/mcp-jujutsu`
*   **Repo:** [https://github.com/jasagiri/mcp-jujutsu](https://github.com/jasagiri/mcp-jujutsu)
*   **Focus:** Includes semantic commit analysis and division.
*   **Stack:** Built with Nim (uses `nimble`).

## 3. Comparison with Current Mentci Baseline
Mentci-AI currently manages Jujutsu via:
1.  **Direct `jj` calls** in `mentci-workspace.ts` and `mentci-vcs`.
2.  **`mentci-mcp` (local Rust server)**: Currently does not expose generic `jj` tools, focusing instead on `structural_edit` and `capnp_sync`.

## 4. Implementation Fit for Mentci-AI

### Option 1: Integrate `keanemind/jj-mcp-server`
*   **Pros:** Quickest path to full `jj` tool coverage.
*   **Cons:** Introduces another external Python/Node process. Doesn't align with the **Rust Only Mandate** for core logic.

### Option 2: Build `mentci-vcs-mcp` (Rust-native)
*   **Strategy:** Implement a thin MCP wrapper in Rust that uses the `jj-lib` or simply executes the `jj` binary with structured JSON output.
*   **Pros:** High fit with Sema principles. No extra runtime overhead. Fully auditable and policy-gated by `mentci-policy-engine`.

## 5. Recommendation
We should **Build a local Rust-native VCS-MCP** within the `mentci-mcp` or a dedicated `Components/mentci-vcs-mcp` component. 

This aligns with the **Philosophy of Intent**: we don't just want to "run commands," we want to **Model the history** as Sema Objects.

### Proposed Initial Toolset (Logical VCS):
- `vcs_status`: Returns current change metadata.
- `vcs_log`: Returns semantic history graph.
- `vcs_describe`: Structural update of commit messages.
- `vcs_bookmark_set`: Idempotent bookmark management.

## 6. Next Steps
1.  Benchmark `keanemind/jj-mcp-server` to identify the most useful tool patterns.
2.  Draft the Cap'n Proto schema for `vcs_status` and `vcs_log`.
3.  Implement the first Rust-based `vcs` tool in the logical-edit plane.
