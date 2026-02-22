# Attractor Study: Agentic Workflow Guidelines

**Linked Goals:** 
- `Goal 0: mentci-aid Stabilization` (Assimilation)
- `Goal 1: Attractor DOT Job Handoff` (Standardization)

**Status:** Draft
**Date:** 2026-02-20
**Goal:** Establish a standard for defining, validating, and sharing "Agentic Thought Processes" using Graphviz DOT syntax.

## 1. Vision
The Attractor Study formalizes **how agents think and act** when solving complex engineering tasks. Mentci-AI does not implement Attractor; it incorporates the Attractor standard and aligns its workflows to it.

These graphs are not just for execution; they are **shared artifacts** that describe the "Algorithm of Thought" for a specific task (e.g., "Fix a Bug", "Implement a Feature", "Review a PR").

## 2. Core Components

### A. The Schema (Truth)
We will define the formal structure of a Workflow in `Components/schema/mentci.capnp`. This allows agents to:
*   Reason about their own process.
*   Exchange workflow definitions with other agents.
*   Validate the structural integrity of a plan before execution.

**Proposed Structures:**
*   `Workflow`: The full graph (id, goal, nodes, edges).
*   `Node`: A step in the process (type, prompt, retry logic).
*   `Edge`: A transition (condition, target).
*   `Context`: The state passed between nodes.

### B. The Standard (Guidelines)
We adapt `Sources/attractor/attractor-spec.md` (transitional alias: `Sources/attractor/attractor-spec.md`) into `Library/specs/WORKFLOW_STANDARD.md`. This document serves as the "RFC" for how to write valid Mentci workflows that incorporate Attractor.

**Key Concepts:**
*   **Declarative vs. Imperative:** Define *what* to do, not just *how*.
*   **Checkpointing:** Every significant step must be durable.
*   **Human-in-the-Loop:** Explicit gates for human approval are first-class citizens.

### C. The Tooling (Implementation)
Instead of a full "Execution Engine" (which is a larger undertaking), we will build a **Reference Validator & Linter** in Rust (`Components/mentci-aid/src/`).

**Features:**
*   **DOT Parser:** Robust parsing of the DOT subset defined in the standard.
*   **Semantic Validation:**
    *   "Does this graph have a start and an exit?"
    *   "Are all nodes reachable?"
    *   "Do the prompts make sense for the node type?"
*   **Conversion:** `dot -> capnp` compiler to turn visual graphs into machine-readable specs.

## 3. Roadmap

1.  **Re-Document:** Port and refine the Attractor Spec into `Library/specs/` for incorporation, not reimplementation.
2.  **Define Schema:** Update `Components/schema/mentci.capnp` with `Workflow` types.
3.  **Implement Parser:** Upgrade `Components/mentci-aid/src/dot_loader.rs` to a proper parser (using `graphviz-rust` or similar, or a robust custom parser).
4.  **Implement Linter:** Add validation logic to `Components/mentci-aid/src/main.rs`.
5.  **Create Reference Workflows:** Author standard `.dot` files in `Components/workflows/` for common tasks.

## 5. High-Priority Remediation: Goal Path Consistency (Implemented)
Issue addressed: Goal 1 task path drift between `Core/HIGH_LEVEL_GOALS.md` and actual repository location.

Implementation:
- Canonical Goal 1 task reference set to:
  - `Components/tasks/high_level_goals/goal_1_attractor_dot_job_handoff.md`

Follow-up:
- Sweep for stale `tasks/high_level_goals/...` references outside `Components/tasks/`.

## 4. Back-Burner (Future)
*   **Full Execution Engine:** A daemon that actually *runs* these graphs. For now, we focus on *defining* and *validating* them. The "running" can be done manually or by simple scripts initially.
