# Research Artifact: Actor Library Selection and Script Migration

- **Solar:** ♓︎ 6° 1' 30" | 5919 AM
- **Subject:** `Actor-Based-Scripts-Migration`
- **Title:** `actor-library-selection-and-script-migration`
- **Status:** `proposed`

## 1. Intent
Research the re-implementation of the `execute` component's scripts (currently Clojure) as native Rust actors. This migration serves as a vehicle to introduce actor-based programming as a core Rust guideline for the Mentci-AI ecosystem.

## 2. Actor Library Candidates

### 2.1 actix
*   **Pros:** Most mature, very fast.
*   **Cons:** Opinionated, heavy runtime, can be complex for simple tooling.

### 2.2 ractor (Selected Candidate)
*   **Pros:** Pure-Rust actor framework inspired by **Erlang's `gen_server`**. This alignment with Erlang (referenced by the author as "the importance of the actor") makes it the best fit.
*   **Cons:** Newer than `actix`.
*   **Alignment:** Fits the "Russian Doll" supervision model and "Single Owner" concept mentioned in recent transcriptions.

### 2.3 xtra
*   **Pros:** Tiny, fast, non-opinionated.
*   **Cons:** Very minimal; lacks higher-level supervision features of `ractor`.

## 3. Script Migration Strategy
The `execute` component will be refactored from a "shell wrapper" into an **Actor Orchestrator**.

### 3.1 Architecture
1.  **Orchestrator Actor:** Manages the lifecycle of specialized script actors.
2.  **Script Actors:** Each current Clojure script (e.g., `RootGuard`, `ReferenceGuard`) becomes a long-lived or task-based actor.
3.  **Messaging:** The CLI input is translated into a message sent to the relevant actor.

### 3.2 Benefits
*   **Performance:** Native Rust execution (no `bb` startup cost).
*   **Reliability:** Supervision trees can automatically restart failing guards.
*   **Consistency:** Shared memory and state across different "scripts" during a single execution.

## 4. Proposed Rust Guideline Update
The `SEMA_RUST_GUIDELINES.md` should be updated to mandate actor-based logic for any concurrent or multi-step symbolic flow.

### 4.1 Draft Rule: "Concurrency via Actors"
> "Any logic involving concurrent execution, state persistence across transformations, or supervised sub-flows must be implemented using the `ractor` actor framework. Communication must occur via typed messages, adhering to the Single Object In/Out rule."

## 5. Next Steps
1.  Prototype a `RootGuard` actor using `ractor`.
2.  Draft the formal update to `SEMA_RUST_GUIDELINES.md`.
3.  Implement the `execute` orchestrator.
