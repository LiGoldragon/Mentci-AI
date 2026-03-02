# Research Report: The Agentic `$EDITOR` Problem

## 1. Problem Statement
When a Pi agent executes a command (e.g., `jj describe`) without providing a message via the `-m` flag, the command falls back to launching the environment's default text editor (`$EDITOR`). In our sandboxed `nix develop` environment, this launches `emacs` or `vi`, which blocks the agent indefinitely as it waits for human input that will never come.

The immediate fix applied was to set `EDITOR = "false";` in the `dev_shell.nix`, which forces any such command to fail immediately, providing an error signal to the agent. This is a robust *mitigation*, but it is not a *solution*. A truly autonomous agent should be able to handle this situation more intelligently.

## 2. Brainstorming: Avenues for a True Solution

### Strategy 1: The "Edit Intent" Sub-Agent (High-Effort, High-Reward)

This is the most "Sema" and architecturally sound approach.

- **Detection**: The parent agent's `bash` tool wrapper would detect a call that is known to be interactive (e.g., `jj describe` with no `-m`). Instead of executing it, it would intercept the call.
- **Context Packaging**: The agent would package the necessary context into a Cap'n Proto message. This "EditIntent" object would contain:
  - The original command that was blocked (`jj describe`).
  - The goal of the edit (e.g., "describe the changes made").
  - The relevant `jj diff` or `jj status` output.
  - A summary of the last few user prompts and agent actions.
- **Sub-Agent Invocation**: The parent agent would then use the `subagents` extension to spawn a new, specialized "Editor Agent". This agent's *only job* is to fulfill the `EditIntent`. Its system prompt would be something like, "You are an Editor Agent. You will be given context and a diff. Your sole task is to write a compliant commit message based on this information and output it as a string."
- **Execution & Return**: The Editor Agent formulates the message. The parent agent receives this string and re-issues the original command with the `-m` flag populated, e.g., `jj describe -m "..."`.

**Pros**:
-   **Separation of Concerns**: Editing text is a distinct cognitive task. A specialized agent can be fine-tuned for this, improving quality.
-   **Preserves Autonomy**: The agent system solves its own problem without failing.
-   **Structured & Composable**: Based on Cap'n Proto messaging, aligning with our core architecture.

**Cons**:
-   High implementation complexity. Requires new `pi` extension logic, a new sub-agent definition, and a Cap'n Proto schema for the `EditIntent`.

### Strategy 2: The "Self-Correcting" In-Context Loop (Medium-Effort)

This approach uses the existing `bash` tool but with smarter error handling within the agent's own reasoning loop.

-   **Execution & Failure**: The agent runs `jj describe`, which fails due to `EDITOR = "false"`. The `bash` tool returns an error message like "`$EDITOR` failed".
-   **Error Recognition**: The agent's prompt/skill would be trained to recognize this specific error. The `systematic-debugging` skill would be updated with a rule: "If a command fails with an editor error, it means you forgot to provide a required message."
-   **Self-Correction**: The agent, recognizing its mistake, would then look at its own history (`jj diff -s @`), formulate a message, and re-run the command with the `-m` flag.

**Pros**:
-   Requires no new infrastructure, only updates to agent skills and prompt engineering.
-   Reinforces the agent's ability to learn from and correct its own errors.

**Cons**:
-   More brittle. Depends on the agent correctly interpreting the error message every time.
-   Less efficient; requires a failed tool call and a second turn to correct.

### Strategy 3: The `EDITOR` Wrapper Script (Low-Effort, Brittle)

This is a more advanced version of the quick fix.

-   We set `EDITOR` to a custom script, e.g., `EDITOR="/path/to/editor-handler.sh"`.
-   This script would attempt to use `pi` itself (or another tool) to ask the agent to generate the content for the file path passed to it by the calling process (e.g., `.git/COMMIT_EDITMSG`).
-   The script would need a way to access the parent agent's context, which is very difficult and breaks sandboxing rules.

**Pros**:
-   Conceals the complexity from the agent.

**Cons**:
-   Extremely difficult to implement robustly.
-   Breaks process isolation and creates a complex web of dependencies.
-   High risk of creating unmanageable state and hanging processes. **(Not Recommended)**.

## 3. Recommendation
The immediate mitigation is sufficient for stability.

For the long-term, **Strategy 2 (Self-Correcting Loop)** should be implemented first as it provides immediate value by improving the agent's reasoning capabilities with minimal overhead.

**Strategy 1 (Sub-Agent)** should be the ultimate goal. It represents a mature, scalable architecture for handling complex, stateful tasks and aligns perfectly with our project's "componentized" and "actor-first" philosophy. We should begin by designing the `EditIntent.capnp` schema.
