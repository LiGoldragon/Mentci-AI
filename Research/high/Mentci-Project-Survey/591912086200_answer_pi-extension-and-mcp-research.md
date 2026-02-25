# Agent Report

- Chronography: `12.8.20.00 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Mentci-Project-Survey`
- Title: `pi-extension-and-mcp-ecosystem-research`

## Prompt
Use linkup to search for features that I may be missing on with a pi extension, and mcp servers that I should know about. (User also clarified the Aski parser requirements and daemon socket topology).

## Agent Answer

### 1. Linkup Research Results
I performed a live web search using the Linkup API (via manual `curl` as the native `linkup_web_search` tool was not yet visible in the active session's toolset). 

#### 1.1. Missing Pi Features & Community Extensions
Based on current community developments (2025-2026), Mentci-AI could benefit from the following extensions:
*   **`agent-chains`**: Enables meta-agent orchestration (chains of specialized agents) which aligns perfectly with our \"Russian Doll\" sub-flow model.
*   **`interactive-shell`**: An extension that allows the agent to run interactive CLIs (like `gdb` or `jj` interactive modes) within an observable TUI overlay.
*   **`files-widget`**: A high-fidelity in-terminal file browser. While we use `ls` and `find`, a dedicated widget would improve the `@explore` subagent's spatial awareness.
*   **Context Pruning Extensions**: Advanced middleware that surgically trims oversized tool outputs before they hit the context window, which is critical for maintaining Aski's "sparse notation" goal.

#### 1.2. Notable MCP Servers for Developers
The following MCP servers are currently industry standard and should be integrated into the `mentci-mcp` ecosystem:
*   **GitHub/GitLab MCP**: Native management of PRs, issues, and remote branches.
*   **Postgres/Supabase MCP**: Allows agents to query database schemas and data directly to inform refactoring.
*   **Puppeteer/Chrome-DevTools MCP**: Enables the agent to "see" and interact with live web applications for end-to-end testing.
*   **Slack/Discord MCP**: Connects the agent to team communication channels for reporting and alert triggers.

---

### 2. Strategic Suggestions for the Author (v3)

**Suggestion 1: The "Aski Bellows" (Minifier/Beautifier)**
Following your directive for a strict two-way utility, we should implement **`mentci-fmt`** as an MCP tool. 
*   **The Bellows Logic:** When I (the agent) call `read_file`, the MCP server should automatically "minify" the Aski/Lojix data to maximize context density. When I call `write_file`, the server should "beautify" it for human review and Git/Jujutsu diffing.
*   **Impact:** This ensures the "Truth" remains homoiconic and efficient, while humans retain the ability to audit the code.

**Suggestion 2: The Multi-Socket Daemon (`mentci-aid`)**
For the socket topology across the Nix boundary:
*   **Implementation:** We should use **Unix Domain Sockets** located in a shared runtime directory (`.mentci/run/`). 
*   **Namespacing:** Each `mentci-box` (sub-jail) will have its specific socket bind-mounted. This allows the master `mentci-aid` to address child agents individually (e.g., `socket-A`, `socket-B`) without global IPC leakage.

**Suggestion 3: Native MCP Tooling for `jj`**
We should stop relying on the agent calling raw `jj` commands via bash.
*   **Why:** Raw shell calls are prone to error and difficult to parse.
*   **Action:** Implement `jj_commit`, `jj_branch_spawn`, and `jj_diff_summary` as native tools in `mentci-mcp`. This gives the agent a deterministic, schema-validated interface to the version control system.

---

### 3. Questions for the Author

1.  **Aski Delimiter Priorities:** You mentioned delimiters like `()` and `[]` having semantic meaning in Aski (e.g., File vs. Directory). Should the parser allow for *custom* delimiter mappings per project, or should we hardcode a "Mentci Standard" for these symbols?
2.  **MCP Context Injection:** Should the `mentci-mcp` server be responsible for automatically injecting the `solar:` baseline into every tool response it sends back to me?
3.  **Parallel Agent Coordination:** In the multi-socket daemon model, how should agents handle cross-dependency changes? If Agent A is working on `aski-lib` and Agent B is working on `mentci-mcp` (which depends on `aski-lib`), should the master daemon coordinate the merge order, or should they communicate directly via a peer-to-peer socket?

## Reporting Protocol Notes
- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
