# Agentic Jujutsu MCP Server Analysis

## Context
During an investigation into why the `jj-agent` and `jj-expert` subagents were not using `agentic-jujutsu` as an MCP server (despite instructions in the repo indicating it is an MCP-ready tool), we analyzed the upstream package `agentic-jujutsu@2.3.6` installed via Nix (`Components/nix/agentic-jujutsu.nix`).

## Findings
1. **Missing Implementation in Upstream**: The `agentic-jujutsu` package (v2.3.6) falsely advertises MCP support in its `README.md`. While the documentation claims that running `npx agentic-jujutsu mcp-server` will launch the MCP server, examining the `bin/cli.js` file and the package contents reveals that:
   - The `mcp-server.js` file is completely missing from the distributed NPM package.
   - The CLI router in `cli.js` strictly limits commands to `status, log, diff, new, describe, analyze, compare-git, version, info, examples, help`. Any other command (including `mcp-server`) falls through to the default error handler or raw `jj.execute`, resulting in an "Unknown command" error.
2. **Subagent Workaround**: Because of this missing feature, the Mentci-AI agents (`jj-agent` and `jj-expert`) were historically instructed to use `agentic-jujutsu` as a bounded CLI probe (e.g., `agentic-jujutsu status`) rather than an actual MCP tool.
3. **Sema-Programmer Fork/Fix**: In accordance with the Independent Developer "Behavioral Changes Require Forks" mandate, we have implemented a local Node.js wrapper (`Components/nix/agentic-jujutsu-mcp.cjs`) that utilizes the `@modelcontextprotocol/sdk` and the native `JjWrapper` from the installed `agentic-jujutsu` package to correctly expose its capabilities (`jj_status`, `jj_log`, `jj_diff`, `jj_analyze`) as true MCP tools.

## Actions Taken
- **Created MCP Wrapper**: Implemented a local MCP server wrapper in `Components/nix/agentic-jujutsu-mcp.cjs`.
- **Configured Pi Gateway**: Registered the new MCP server in `.pi/mcp.json` under the name `agentic-jujutsu`.
- **Updated Agent Prompts**: The `jj-agent.md` and `jj-expert.md` instructions will be updated to explicitly direct agents to use the MCP tools (`mcp({ tool: "jj_status" })`, etc.) rather than the broken CLI interface.

## Next Steps
Agents should now exclusively use the `mcp` tool to interact with the `agentic-jujutsu` server instead of calling the binary directly from `bash`.
