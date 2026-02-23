# Mentci Workflow Standard (Attractor Specification)

**Version:** 1.0 (Draft)
**Based on:** Attractor Specification (incorporated, not reimplemented)
**Context:** Guidelines for Agentic Thought Processes

This document defines the standard for constructing **Agentic Workflows** using the Graphviz DOT language. These workflows represent the "Algorithm of Thought" for autonomous agents within the Mentci ecosystem.

---

## 1. Overview and Goals

### 1.1 Concept: The Thought Graph
Complex engineering tasks cannot be solved by a single LLM prompt. They require a structured **process**: planning, execution, review, and iteration.
We define this process as a **Directed Acyclic Graph (DAG)** where:
*   **Nodes** represent distinct cognitive steps (e.g., "Plan", "Code", "Review").
*   **Edges** represent logical transitions (e.g., "Success", "Failure", "Needs Info").

By formalizing these graphs in DOT, we make agent behavior **inspectable**, **version-controllable**, and **collaborative**.

### 1.2 Why DOT?
*   **Visual:** Graphs are naturally visual. DOT renders directly to diagrams.
*   **Declarative:** We define *what* happens, not *how* the runtime executes it.
*   **Universal:** Simple text format, easily parsed and generated.

---

## 2. The Mentci DOT Schema

### 2.1 Supported Subset
We use a strict subset of DOT for predictability:
*   **Directed Graphs Only:** `digraph { ... }`
*   **One Graph Per File.**
*   **Typed Attributes:** String, Integer, Float, Boolean, Duration.

### 2.2 Standard Node Shapes (Semantics)

| Shape           | Concept             | Description |
|-----------------|---------------------|-------------|
| `Mdiamond`      | **Start**           | The entry point of the thought process. |
| `Msquare`       | **Goal / Exit**     | The definition of done. |
| `box`           | **Cognition**       | LLM Task (Planning, Coding, Analysis). The default "Thinking" step. |
| `hexagon`       | **Human Gate**      | A pause for human approval or input. Essential for Level 5 safety. |
| `diamond`       | **Decision**        | Automated routing logic (based on previous outcomes). |
| `component`     | **Parallel**        | Fan-out for concurrent tasks (e.g., "Write Tests" AND "Write Code"). |
| `parallelogram` | **Action**          | External Tool Execution (Shell, API). |

### 2.3 Attributes

**Node Attributes:**
*   `prompt`: The instruction for the LLM. Supports `$goal` expansion.
*   `goal_gate`: `true` if this step MUST succeed for the workflow to be valid.
*   `max_retries`: How many times to attempt this step before failing.
*   `fidelity`: Context window strategy (`full`, `compact`, `truncate`).

**Edge Attributes:**
*   `condition`: Logic expression (`outcome=success`, `outcome=fail`).
*   `label`: Human-readable description of the transition.
*   `weight`: Priority for edge selection.

---

## 3. Execution Model (The "Engine" Concept)

While Mentci does not yet mandate a specific runtime, any compliant executor must follow these rules:

1.  **State Persistence:** The state of the workflow (completed nodes, context) must be saved after every step.
2.  **Deterministic Routing:** Edge selection is deterministic based on `condition`, `label`, and `weight`.
3.  **Context Management:** Context (variables, history) is passed between nodes, respecting `fidelity` settings.

### 3.1 Routing Logic
1.  **Condition Match:** Evaluate edge `condition`.
2.  **Preferred Label:** Match the node's `preferred_label` outcome.
3.  **Suggested ID:** Match `suggested_next_ids`.
4.  **Weight:** Highest `weight`.
5.  **Lexical:** Node ID.

### 3.2 Recursive Execution (Russian Doll)
Workflows can invoke other workflows as sub-processes.
*   **Sub-Flow Nodes:** A `box` (Cognition) or `parallelogram` (Action) node can trigger a child workflow.
*   **Isolation:** The child workflow executes in its own process context (potentially a Nix Jail) and manages its own internal state.
*   **Bookmarking:** The child workflow may push to a unique, internal Jujutsu bookmark, merging back only upon success.

---

## 4. Standard Workflow Patterns

### 4.1 Linear Chain (The "Waterfall")
`Start -> Plan -> Act -> Verify -> Exit`
Best for simple, well-defined tasks.

### 4.2 The "TOTE" Loop (Test-Operate-Test-Exit)
`Start -> Test (Fail) -> Operate -> Test (Pass) -> Exit`
Standard for bug fixing.

### 4.3 Review Gate
`... -> Code -> Review (Human) -> [Approve] -> Exit`
                               `-> [Reject] -> Fix -> Code`
Essential for high-stakes changes.

---

## 5. Definition of Done (Validation)

A valid Mentci Workflow must:
*   Have exactly one `Start` (`Mdiamond`) and at least one `Exit` (`Msquare`).
*   Have no unreachable nodes.
*   Have defined `prompt` attributes for all `box` (Cognition) nodes.
*   Have clear success/failure paths from every decision point.
