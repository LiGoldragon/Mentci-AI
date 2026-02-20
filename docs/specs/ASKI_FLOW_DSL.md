# Aski-Flow: The Noun-Sequence DSL

**Status:** Draft / Specification
**Date:** 2026-02-20
**Context:** Standard for defining Agentic Workflows in Mentci-AI

## 1. Philosophy
**Aski-Flow** is a minimalist EDN-based Domain Specific Language (DSL) for defining Directed Acyclic Graphs (DAGs) and State Machines. It prioritizes **Declarative Intent** over Imperative Control Flow.

It replaces the verbosity of DOT (`A -> B`) with the structural density of Lisp Vectors (`[A B]`).

**File Extension:** `.aski-flow` (EDN-compatible)

## 2. The Core Axioms

1.  **The Vector is the Flow.**
    *   `[A B C]` implies `Start -> A -> B -> C -> Exit`.
    *   No explicit `Start` or `Exit` nodes are required. The container defines the boundary.

2.  **Nouns, not Verbs.**
    *   Nodes are defined by **What They Are** (Result/State), not **What They Do** (Action).
    *   Use `(ImplementationPlan)`, not `(CreatePlan)`. The execution is implicit.

3.  **Structure is Logic.**
    *   **Vector `[]`**: Sequence / Block.
    *   **List `()`**: Instantiation (Node with config).
    *   **Symbol `Name`**: Reference (Simple Node).
    *   **Map `{}`**: Control Flow (Branching logic for the *preceding* node).

## 3. Syntax Reference

### 3.1 The Linear Chain
A simple sequence of steps.
```edn
[ Analyze
  Plan
  Execute
  Verify ]
```

### 3.2 Configuration ( instantiation)
Passing parameters to a step.
```edn
[ (Analyze {:depth :deep})
  (Plan    {:model :reasoning}) ]
```

### 3.3 Branching & Loops (The Map)
A Map immediately following a Node defines the transitions from that Node. Keys are **Conditions**, Values are **Targets**.

**Targets can be:**
*   `:next` (Default): Continue to the next index.
*   `Symbol`: Jump to the Node with this name (backward or forward).
*   `[ ... ]`: Enter a sub-flow (nested vector).

**Example: The TOTE Loop (Test-Operate-Test-Exit)**
```edn
[ (Test)
  {:pass :next   ;; Continue (to Exit)
   :fail Operate} ;; Loop
  
  (Operate)
  {:next Test}   ;; Unconditional jump back
]
```

### 3.4 Nested Flows (Sub-Graphs)
A Vector can be a target, allowing for hierarchical workflow definition.

```edn
[ (Analyze)
  {:ok [Plan Execute]  ;; Sub-sequence
   :no Abort} ]
```

## 4. Comparison to DOT

| Feature | DOT | Aski-Flow |
| :--- | :--- | :--- |
| **Topology** | Explicit Edges (`A->B`) | Implicit Sequence (`[A B]`) |
| **Data** | String Attributes | Rich EDN Data |
| **Start/End**| Explicit Nodes | Contextual (Index 0 / End) |
| **Looping** | `B->A` | `{:fail A}` |
| **Semantics**| Imperative | Declarative (Noun-based) |

## 5. Implementation Strategy
*   **Parser:** Standard EDN parser (Clojure `edn/read-string` or Rust `edn-rs`).
*   **Compiler:** A "Flattener" pass that converts the Vector into an Adjacency List (Graph) for the runtime engine.
    *   Index `i` connects to `i+1` by default.
    *   Map entries override default connections.
*   **Runtime:** The existing `src/main.rs` engine can be adapted to run this Graph structure easily.

## 6. DOT Structured-Data Conversion Standard

Canonical EDN conversion model:
- `schema/dot_graph_conversion.edn`

This schema defines:
- DOT graph object structure (`:DotGraph`, `:DotNode`, `:DotEdge`)
- Aski-Flow routing conversion semantics (`:AskiFlowConversion`)
- Deterministic rules for default sequential edges and routing-map overrides

---
*Created for the Mentci-AI Level 5 Stack.*
