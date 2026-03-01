# Research: Saṃskāra AST-Based References MVP

## 1. The Goal
Move beyond brittle textual patching (`oldText`/`newText`) by allowing agents to mutate files using **exact AST location references** (row, column, byte offset) provided by Tree-sitter.

## 2. The Two-Step Find-and-Edit Protocol

### Step 1: Discovery (The "Find")
The agent uses a logical query tool (e.g., `logical_run_query` or `logical_get_ast`) to explore the AST of a target file.
- **Output**: The tool returns the exact structural coordinates of the node the agent wants to edit.
- **Example Return**:
  ```json
  {
    "nodeType": "function_item",
    "name": "refine_message",
    "startPoint": {"row": 42, "column": 0},
    "endPoint": {"row": 50, "column": 1}
  }
  ```

### Step 2: Assembly (The "Edit")
Instead of the primary agent writing a complex regex or trying to calculate offsets, we delegate the assembly.
- **The Tiny Subagent**: The primary agent dispatches a specific, low-temperature subagent with a single purpose: *Given this AST range and this new logic, construct the exact Cap'n Proto `EditComponentMessage`.*
- **The Execution**: The `samskarad` daemon receives this highly specific message and performs the slice-and-replace operation on the file safely using the guaranteed AST coordinates.

## 3. Why Subagents for Assembly?
- **Context Preservation**: The primary agent (which might be holding 200k tokens of architecture context) doesn't need to waste reasoning cycles on exact coordinate math.
- **Failure Isolation**: If the structural edit coordinates are slightly misaligned, the tiny subagent fails fast and can retry without poisoning the main reasoning loop.
- **Protocol Alignment**: This maps perfectly to the "Sema Subagent Flow" (The Arrangement Pipeline), where the primary agent acts as the Orchestrator/Planner and delegates precise mutation to a specialized `task` worker.
