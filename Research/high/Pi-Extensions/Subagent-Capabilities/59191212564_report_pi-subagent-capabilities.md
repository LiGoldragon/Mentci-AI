
# Research Report: Pi's Sub-Agent Capabilities

**Solar Time:** 59191212564

## 1. Summary

This report investigates the `pi` coding agent's native and extension-based capabilities for using sub-agents and parallel task execution. The findings indicate that the core `pi` agent is intentionally minimalist and does not include built-in support for sub-agents. This functionality is provided by a rich ecosystem of extensions.

## 2. Key Findings

*   **Core `pi` is Minimalist:** The standard `@mariozechner/pi-coding-agent` deliberately omits complex features like sub-agents and planning modes to maintain a lean, adaptable core.
*   **Extensions Provide Sub-Agent Functionality:** A variety of third-party extensions enable sophisticated multi-agent workflows:
    *   **`oh-my-pi`:** A fork that bundles a parallel execution framework with several pre-defined agents (e.g., `explore`, `plan`, `designer`, `reviewer`).
    *   **`pi-subagents`:** A dedicated extension for asynchronous sub-agent delegation, featuring agent chains, parallel execution, and a recursion depth guard.
    *   **`pi-parallel-agents`:** An extension for dynamic, parallel execution that supports using different models for different agents simultaneously and includes a DAG-based team coordination mode.
*   **Alternative Strategies & Philosophies:**
    *   **Manual Parallelism:** Some developers advocate against automated sub-agents, preferring to manually manage parallelism by running multiple `pi` sessions in isolated `git worktrees` or separate clones. This approach is seen as a way to prevent codebase degradation.
    *   **The `independent-developer` Skill:** This project's own high-level skill, `independent-developer`, promotes a similar isolation strategy, preferring independent `jj` clones over shared worktrees to avoid state conflicts.
*   **Comparison with Other Ecosystems:** Frameworks like LangChain, Claude Code, and Google's ADK have mature, built-in concepts for parallel and sequential agent execution, highlighting a common pattern in advanced agentic systems.

## 3. Conclusion

`pi`'s architecture delegates sub-agent capabilities to its extension layer. Users can choose from several powerful extensions to enable parallel and sequential workflows, or they can adopt a manual, session-based approach for task isolation. The "right" approach depends on the desired level of automation versus control.
