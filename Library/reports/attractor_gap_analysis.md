# Gap Analysis: Attractor vs. Mentci-AI

**Date:** ♓︎ 1° 28' 44" | 5919 AM (Feb 19, 2026)
**Subject:** Comparative analysis of Attractor specifications against Mentci-AI's current state.

## Executive Summary

**Mentci-AI** incorporates the **Attractor Workflow Standard** as its "Algorithm of Thought". The focus is on standard adoption and alignment, not reimplementing Attractor.

The Attractor spec is re-documented as the **Mentci Workflow Standard** (`Library/specs/WORKFLOW_STANDARD.md`) for incorporation. A reference DOT parser and basic traversal logic exists in Rust, but Mentci-AI is not an Attractor reimplementation.

---

## 1. Orchestration & Workflow (The "Brain")

| Feature | Attractor Specification | Mentci-AI Current State | Status |
| :--- | :--- | :--- | :--- |
| **Pipeline Definition** | **DOT Graph Syntax.** | **Standardized.** See `Library/specs/WORKFLOW_STANDARD.md`. Parser implemented in `Components/src/dot_loader.rs`. | ✅ **Adopted** |
| **State Management** | **Checkpoint & Resume.** | **Basic Implementation.** `CheckpointManager` in `Components/src/main.rs` saves JSON snapshots. | ⚠️ **Partial** |
| **Routing Logic** | **Edge-Based Routing.** | **Implemented.** Engine supports conditional and label-based routing. | ✅ **Done** |
| **Configuration** | **Model Stylesheets.** | Not yet implemented. | ⏳ **Backlog** |

## 2. The Agentic Loop (The "Heart")

| Feature | Attractor Specification | Mentci-AI Current State | Status |
| :--- | :--- | :--- | :--- |
| **Execution Loop** | **Interleaved LLM/Tool Cycle.** | **Basic Traversal.** `PipelineEngine` traverses nodes but uses mock handlers. | ⚠️ **Prototype** |
| **Steering** | **Mid-flight Injection.** | Not implemented. | ⏳ **Backlog** |
| **Loop Detection** | **Signature Tracking.** | Not implemented. | ⏳ **Backlog** |
| **Output Truncation** | **Head/Tail Split.** | Not implemented. | ⏳ **Backlog** |

## 3. Tooling & Execution (The "Hands")

| Feature | Attractor Specification | Mentci-AI Current State | Status |
| :--- | :--- | :--- | :--- |
| **Environment** | **`ExecutionEnvironment` Abstraction.** | **Implemented.** `LocalExecutionEnvironment` trait defined in Rust. | ✅ **Done** |
| **Tool Alignment** | **Provider-Specific Profiles.** | Generic "Codergen" handler only. | ⏳ **Backlog** |
| **Parallelism** | **Concurrent Tool Execution.** | Sequential execution only. | ⏳ **Backlog** |

## 4. LLM Interface (The "Mouth")

| Feature | Attractor Specification | Mentci-AI Current State | Status |
| :--- | :--- | :--- | :--- |
| **API Strategy** | **Native APIs.** | Mocked in `CodergenHandler`. | ⏳ **Backlog** |
| **Cost Control** | **Reasoning Token & Cache Tracking.** | Not implemented. | ⏳ **Backlog** |

## 5. Human Interaction (The "Conscience")

| Feature | Attractor Specification | Mentci-AI Current State | Status |
| :--- | :--- | :--- | :--- |
| **Human Gates** | **Explicit Graph Nodes.** | **Implemented.** `WaitHumanHandler` pauses for CLI input. | ✅ **Done** |
| **Interviewer** | **Abstracted UI.** | CLI-based interaction only. | ⚠️ **Partial** |

## Recommendations

1.  **Schema Definition:** The next major step is to define `Workflow`, `Node`, and `Edge` in `Components/schema/mentci.capnp` to make these structures portable between agents.
2.  **Standard Library:** Create a library of reusable DOT workflows (e.g., `Components/workflows/std/bugfix.dot`) to verify the validator.
3.  **Linter:** Enhance `Components/src/main.rs` to run validation checks (orphan nodes, missing goals) as defined in the spec.
