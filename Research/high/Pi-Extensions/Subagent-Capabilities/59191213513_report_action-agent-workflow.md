# Research Report: Sub-Agent Architecture and Action-Agent Workflow

**Solar Time:** 59191213513

## 1. Summary

This report explores the architectural design for sub-agents within the `pi` ecosystem, specifically focusing on an "Action-Agent" pattern designed to maintain the "sobriety" and focus of the master (expensive) agent.

## 2. Core Architectural Pattern: The Action-Agent

The proposed workflow addresses the problem of context poisoning and token inefficiency in high-reasoning models.

### Workflow Mechanics:
1.  **Request:** The Master Agent issues a high-level directive (e.g., "Implement feature X", "Fix bug Y").
2.  **Delegation:** A specialized "Action-Agent" (sub-agent) is triggered to perform the physical labor (interacting with `jj`, editing code, running tests).
3.  **Isolation:** The sub-agent operates in its own context/session. The Master Agent is shielded from the voluminous intermediate outputs (bash logs, linter noise, trial-and-error).
4.  **Feedback Loop:**
    *   The sub-agent returns a **highly structured summary** to the Master Agent.
    *   **Success:** A simple success signal, potentially an S-expression/EDN/Datalog fragment.
    *   **Failure:** A structured error object containing targeted diagnostic data, with references to deeper logs stored in `samskarad`.
5.  **Perception:** The Master Agent can selectively "perceive" detailed execution history from `samskarad` only when necessary for high-level decision making.

## 3. Sub-Agent Capability Landscape (pi)

*   **Core pi:** Intentionally minimalist; no native sub-agent "tool".
*   **Extensions:**
    *   `pi-subagents`: Provides `/chain` and parallel delegation.
    *   `oh-my-pi`: Bundles specialized roles (scout, reviewer, task).
    *   `pi-parallel-agents`: Supports multi-model teams and DAG-based coordination.
*   **Physical Realization:** Often implemented via independent `jj` clones or worktrees to ensure strict state isolation, mirroring the `independent-developer` philosophy.

## 4. Synthesis & Strategy

The "Action-Agent" model aligns perfectly with the **Sema Object Style**. By treating the sub-agent's execution as a single "Object In / Object Out" transformation, we preserve the purity of the Master Agent's reasoning plane.

## 5. Questions for User/System Design

1.  **Protocol Definition:** What is the precise schema for the "success/failure" S-expression? Should we define a Cap'n Proto contract for the sub-agent handoff?
2.  **Samskarad Integration:** How does the Action-Agent "persist" its detailed logs to `samskarad`? Is there an existing API for appending execution traces to the Datalog substrate?
3.  **Recursion Depth:** Should Action-Agents be allowed to spawn their own sub-sub-agents (e.g., a "Test-Agent"), or should we enforce a flat hierarchy to prevent complexity explosion?
4.  **Model Selection:** Is the Action-Agent intended to use a "cheaper" model (e.g., Haiku/Flash) while the Master Agent uses a "heavy" model (e.g., Sonnet/Pro)?
5.  **Tooling Access:** Should the Action-Agent have access to the *exact* same toolset as the Master, or a restricted subset specialized for execution?
