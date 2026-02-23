# Research Artifact: Actor and Agentic Flow Library Landscape (2026)

- **Solar:** ♓︎ 6° 3' 31" | 5919 AM
- **Subject:** `Actor-Agent-Library-Research`
- **Title:** `actor-agent-library-landscape-2026`
- **Status:** `finalized`

## 1. Intent
Survey the current (2025-2026) Rust ecosystem for actor frameworks and agentic flow libraries to identify the best fit for Mentci-AI's evolution toward a Level 5/6 autonomous daemon.

## 2. Actor Frameworks (Concurrency & Supervision)

### 2.1 Ractor (The Primary Choice)
*   **Philosophy:** Modern, Tokio-native, strongly typed.
*   **Alignment:** Heavily inspired by **Erlang/OTP**. This aligns with the author's emphasis on actors and the "Russian Doll" supervision model.
*   **Key Strength:** Built-in supervision trees and location transparency (ready for distributed execution).
*   **Verdict:** Best fit for the `execute` orchestrator and the `mentci-aid` core.

### 2.2 Actix (The Performance Veteran)
*   **Philosophy:** High-throughput, local message passing.
*   **Alignment:** Synchronous-style handlers in an async system.
*   **Key Strength:** Unmatched raw speed for local services (e.g., thousands of WebSockets).
*   **Verdict:** Better suited for high-frequency data services than symbolic reasoning daemons.

### 2.3 Bastion (The Resilience Specialist)
*   **Philosophy:** "Resilience first," runtime-agnostic.
*   **Alignment:** Direct "Let it Crash" implementation.
*   **Verdict:** Powerful but has a steeper learning curve and custom primitives that may diverge from the standard Tokio-based stack.

## 3. Agentic Flow & AI Frameworks

### 3.1 Rig (The LLM Standard)
*   **Role:** Production-ready single agent abstraction and RAG pipelines.
*   **Key Strength:** Unified API for 20+ providers and WASM support.
*   **Verdict:** Strong candidate for the *interaction* layer of Mentci-AI.

### 3.2 Swarms-rs (Multi-Agent Orchestration)
*   **Role:** Coordination of specialized agent teams.
*   **Verdict:** Useful for high-level task delegation across multiple Mentci boxes.

### 3.3 GraphBit (Deterministic Execution)
*   **Role:** DAG-based execution engine.
*   **Alignment:** Directly matches the project's goal of deterministic symbolic manipulation.
*   **Verdict:** Worth investigating for the `mentci-aid` pipeline engine logic.

## 4. Rationale for `ractor` in `execute` Transition
The migration of `execute` scripts to Rust requires a framework that is:
1.  **Lightweight** (minimal startup overhead for CLI tools).
2.  **Supervised** (guards and version checks must fail gracefully).
3.  **Typed** (Sema objects must be transmissible as messages).

`ractor` provides the best balance of Erlang-style reliability and Tokio-native ergonomics.

## 5. Proposed Guideline Update
The `SEMA_RUST_GUIDELINES.md` should be updated to establish **Actor-First Concurrency**. 

### 5.1 New Rule: "Symbolic Actors"
> "All multi-step symbolic transformations and concurrent task executions must be implemented as `ractor` actors. Messages must be defined as Sema Objects, ensuring clear intent and auditable flow."
