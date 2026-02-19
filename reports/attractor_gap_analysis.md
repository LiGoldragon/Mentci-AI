# Gap Analysis: Attractor vs. Mentci-AI

**Date:** ♓︎ 1° 28' 44" | 5919 AM (Feb 19, 2026)
**Subject:** Comparative analysis of Attractor specifications against Mentci-AI's current state.

## Executive Summary

**Mentci-AI** currently establishes a **philosophical and structural foundation** (Level 5 intent, Sema Object Style, Filesystem Ontology, Pure Nix Jail). However, it lacks a **runtime execution model**.

**Attractor** provides exactly what Mentci-AI is missing: a rigorous, language-agnostic specification for the **"Engine"**—the runtime behavior of autonomous agents. While Mentci defines *what* an Atom is, Attractor defines *how* it thinks and acts.

---

## 1. Orchestration & Workflow (The "Brain")

| Feature | Attractor Specification | Mentci-AI Current State | Gap / Action Item |
| :--- | :--- | :--- | :--- |
| **Pipeline Definition** | **DOT Graph Syntax.** Workflows are defined as visualizable Directed Acyclic Graphs (DAGs). Nodes are tasks; edges are transitions. | undefined. No concept of a "job" or "workflow" exists yet. | **Critical.** Adopt DOT as the standard for defining Mentci "Thoughts" or "Workflows". |
| **State Management** | **Checkpoint & Resume.** State is serialized to JSON after every node execution. Crash-proof. | undefined. No runtime state persistence. | Implement a checkpointing system for long-running agent tasks. |
| **Routing Logic** | **Edge-Based Routing.** Decisions (success/fail/retry) are logic gates on the graph edges. | undefined. | Define routing logic within the Cap'n Proto schema for workflows. |
| **Configuration** | **Model Stylesheets.** CSS-like selectors (`#critical_review { model: "gpt-5.2" }`) to decouple logic from model selection. | undefined. | Adopt this pattern to allow swapping "brains" without rewriting code. |

## 2. The Agentic Loop (The "Heart")

| Feature | Attractor Specification | Mentci-AI Current State | Gap / Action Item |
| :--- | :--- | :--- | :--- |
| **Execution Loop** | **Low-level `Client.complete()` loop.** Manages the interleaved cycle of LLM generation -> Tool Execution -> Output Truncation -> Steering. | undefined. | Mentci needs a "Main Loop" implementation (in Rust) that orchestrates this cycle. |
| **Steering** | **Mid-flight Injection.** Allows the user/host to inject messages ("actually, do X") *between* tool calls without restarting. | Read-only logs. | Implement a mechanism to inject "SteeringTurns" into the context. |
| **Loop Detection** | **Signature Tracking.** Detects repetitive tool call patterns (e.g., cycling between 3 files) and warns the model. | undefined. | Essential for autonomous reliability. |
| **Output Truncation** | **Head/Tail Split.** Explicit logic to handle massive outputs (e.g., 10MB CSVs) by keeping start/end and inserting markers. | undefined. | **High Priority.** Essential to prevent context window overflows. |

## 3. Tooling & Execution (The "Hands")

| Feature | Attractor Specification | Mentci-AI Current State | Gap / Action Item |
| :--- | :--- | :--- | :--- |
| **Environment** | **`ExecutionEnvironment` Abstraction.** Decouples *logic* from *runtime* (Local, Docker, K8s, WASM, SSH). | Nix Jail (Local/Shell-based). | Formalize the "Jail" as an implementation of Attractor's `ExecutionEnvironment` interface. |
| **Tool Alignment** | **Provider-Specific Profiles.** Uses *exact* native formats (e.g., `apply_patch` for OpenAI, `edit_file` for Anthropic) rather than a generic abstraction. | Generic references. | Define separate Tool Registries for different "brains" (models) within Mentci. |
| **Parallelism** | **Concurrent Tool Execution.** Handles multiple tool calls in a single turn correctly (execute all -> batch results). | undefined. | Ensure the Rust daemon handles async/concurrent tool dispatch. |

## 4. LLM Interface (The "Mouth")

| Feature | Attractor Specification | Mentci-AI Current State | Gap / Action Item |
| :--- | :--- | :--- | :--- |
| **API Strategy** | **Native APIs.** explicitly *avoids* "OpenAI-compatible" shims to preserve features like Thinking Blocks, Caching, and Grounding. | undefined. | Mentci must not rely on generic wrappers; it needs native adapters for high-fidelity control. |
| **Cost Control** | **Reasoning Token & Cache Tracking.** Explicit handling of "hidden" tokens (reasoning) and prompt caching logic. | Basic logging fields. | Expand `MachineLog` schema to capture granular token usage (reasoning vs. output). |

## 5. Human Interaction (The "Conscience")

| Feature | Attractor Specification | Mentci-AI Current State | Gap / Action Item |
| :--- | :--- | :--- | :--- |
| **Human Gates** | **Explicit Graph Nodes.** `wait.human` nodes block execution until input is received via a defined Interface. | Interactive Shell. | Define a `HumanGate` struct in Cap'n Proto for formal approval steps. |
| **Interviewer** | **Abstracted UI.** `ask(question)` interface that can be plugged into CLI, Web, or Slack. | undefined. | Mentci needs an API/RPC for the daemon to request user input. |

## Recommendations

1.  **Import the Specs:** Formally adopt `attractor-spec` (DOT pipelines) and `coding-agent-loop-spec` as the **functional specification** for the Mentci Rust Daemon.
2.  **Schema Expansion:** Port the data structures from Attractor (Outcome, Checkpoint, ToolDefinition) into `mentci.capnp`.
3.  **Rust Implementation:** Begin implementing the `Session` and `ExecutionEnvironment` traits in the `src/` directory, treating the Nix Jail as the concrete environment.
