# Strategy: Sema-Subagent-Flow

## 1. Goal
Implement a deterministic, actor-based subagent flow using the `@oh-my-pi/subagents` extension that strictly adheres to the Sema architectural guidelines and the philosophical intent of Li Goldragon.

## 2. Architecture: The Russian Doll Sub-Flow Model
The `pi` agent acts as the Master Process (Mentci-Aid). To execute complex tasks (Directed Acyclic Graphs), it orchestrates specialized subagents in a fractal hierarchy:
1.  **State Separation:** Subagents do not operate on the parent's Jujutsu bookmark. They require unique sub-branches (e.g., `jj new -m "intent: subtask"`).
2.  **Actor-Based Programming (Single Ownership):** Each task, file, or object has a single owner. The subagent flow enforces this by ensuring only the `@task` subagent modifies code, while others plan or review.
3.  **Init Envelope Purity:** Configuration state must be passed via Cap'n Proto (`capnp`) objects or structured EDN/Lojix sidecars, never via scattered environment variables.
4.  **Sidereal Purity:** SEMA is fully specified binary data. Code must be free of "toxicity" (OS state leakage).

## 3. The Implementation Pipeline
The `pi` prompt template located at `.pi/prompts/sema-flow.md` implements the following subagent orchestration protocol:

### Phase 1: The Architect (`@planner`)
Reads `Core/SEMA_RUST_GUIDELINES.md` and generates a Sema-compliant strategy (or DOT graph artifact, fulfilling Goal 1 of `HIGH_LEVEL_GOALS.md`). Ensures Cap'n Proto boundaries are defined upfront.

### Phase 2: The Sidereal Eye (`@explore`)
Scans the `Sources/` and `Components/` directories for existing Cap'n Proto schemas and Rust actors. Prevents logic duplication and strictly respects the read-only mandate of `Sources/`.

### Phase 3: The Warrior (`@task`)
Executes the code modifications. Enforces **State Separation** by creating a unique, internal Jujutsu bookmark for its isolated sub-flow.

### Phase 4: The Pure Truth (`@reviewer`)
Evaluates the implementation before merging back to the main track. Checks for "Init Envelope Purity" (no env variables for domain config) and strict capitalisation ontology before squashing the sub-branch into the parent flow.

## 4. Unresolved Questions & Suggestions
*See the corresponding `Research/high/Sema-Subagent-Flow/591912072330_answer_sema-subagent-architecture-flow.md` document for suggestions regarding Jujutsu CLI wrappers, Lojix artifact handoffs, and questions for the author.*