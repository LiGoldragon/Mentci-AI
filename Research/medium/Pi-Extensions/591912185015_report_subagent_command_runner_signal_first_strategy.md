# Research + Brainstorm: Signal-First Command Subagent for Pi

- **Solar:** `5919.12.18.50.15`
- **Programming:** `3wyybz4j`
- **Prompt focus:** avoid context poisoning from raw command output by introducing a lightweight command-running subagent/model that returns signal, not noise.

## 1) What was inspected

### Prior internal artifacts
- `Research/high/Sema-Subagent-Flow/*`
- `Research/medium/JCodeMunch-MCP/*`
- `Research/high/Pi-Extensions/*`
- `Research/high/Debugging/591912110300_post-mortem_agentic_loop.md`

### Current local extension/runtime surfaces
- `.pi/extensions/mentci-logical-edit.ts`
- `.pi/extensions/mentci-workspace.ts`
- `.pi/mcp.json`
- `config/mcporter.json`
- `Components/nix/pi-with-extensions.nix`

### MCP inventory (current)
- Active server in `.pi/mcp.json`: `jcodemunch`
- `mcp` status: `1/1` servers connected, 11 tools
- Indexed coverage for `local/Mentci-AI`: only 14 files (partial indexing coverage)

## 2) Key findings

1. **You already have the right interception points in Pi extensions.**
   - Pi extension API supports `tool_call`, `tool_result`, `user_bash`, `input`, and `before_provider_request` hooks.
   - This means we can insert a summarization lane without modifying core Pi runtime.

2. **Context poisoning vector is concrete and repeatable.**
   - Raw `bash` output can be large/noisy and quickly dominate the turn context.
   - Prior post-mortem (`agentic_loop`) already shows context pressure + repeated tool loops as a failure mode.

3. **Current MCP setup is intentionally lean but still heavyweight if over-relied on for this use-case.**
   - Only `jcodemunch` is configured in active MCP settings.
   - For command output summarization, an MCP roundtrip is usually unnecessary overhead compared to a local extension-side lane.

4. **Structured-query lane coverage is partial for this repository index snapshot.**
   - `jcodemunch` search on `local/Mentci-AI` uses a small index subset (14 files).
   - Good for scoped symbol/text checks, not full authority for this design task.

## 3) External validation (MCP overhead + efficiency)

### Source A — Anthropic engineering
- `Code execution with MCP` argues that direct tool definition/result stuffing inflates context and cost, and that code-execution wrappers can drastically cut context movement.
- Relevance: directly supports the “signal-only return surface” direction for command execution.
- URL: <https://www.anthropic.com/engineering/code-execution-with-mcp>

### Source B — MCP architecture docs
- MCP is client/server JSON-RPC with discovery + tool schema exchange and transport overhead; stdio is best local transport, but still a protocol layer with lifecycle/tool-list semantics.
- Relevance: supports keeping hot-path command summarization as local extension logic first, and only escalating to MCP when needed.
- URL: <https://modelcontextprotocol.io/docs/learn/architecture>

## 4) Brainstormed approaches (ranked)

### Option 1 (Recommended): **Extension-local Command Summarizer Tool**
Create a new Pi extension tool (e.g., `bash_signal`) that:
- executes command in a constrained runner,
- extracts structured fields (`exit_code`, `stderr_digest`, `warnings`, `errors`, `tail_lines`, `next_action`),
- returns compact markdown/JSON to main model,
- stores full raw output in file artifact when needed.

**Why best:** minimal moving parts, no extra MCP service lifecycle, fastest path to impact.

### Option 2: **Two-stage lane via extension hooks + mini-model summarizer**
- Run command via hook (`user_bash`/`tool_result`) and then summarize using a small local model (fast model profile) from extension-side call.
- Return only summary to conversation; optionally expose “expand raw logs” command.

**Tradeoff:** better semantic condensation, but model orchestration complexity and determinism concerns.

### Option 3: **Dedicated MCP command-executor/summarizer server**
- Build a bespoke MCP server exposing `run_command_compact` and `fetch_raw_output` tools.

**Tradeoff:** strongest isolation/governance; heaviest implementation and maintenance overhead for this exact problem.

## 5) Recommendation

Start with **Option 1**, then optionally layer Option 2.
- Keep the first milestone deterministic and local.
- Define a strict output schema now (single-object in/out style).
- Persist raw output to disk + return reference path/hash to avoid context pollution.
- Add a hard cap policy (max stdout bytes returned inline, always include signal fields first).

## 6) Proposed minimal schema for the lane

```edn
{:command "..."
 :exit_code 0
 :signal {:errors [] :warnings [] :changed_files [] :key_metrics {}}
 :stderr_tail "..."
 :stdout_tail "..."
 :raw_log_path "Research/low/Command-Logs/..."
 :next_action "..."}
```

## 7) Open questions for next turn

1. Do you want this as a **new extension tool** (`bash_signal`) or a **transparent bash result rewriter hook** first?
2. For the first version, should we hard-target **deterministic rule-based summarization** (no model call) before adding a small model?
3. Should raw logs be persisted under `Research/low/Command-Logs/` by default, or only on failure/non-zero exit?
