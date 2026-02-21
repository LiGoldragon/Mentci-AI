# mentci-aid: The Mentci-AI Daemon

## Identity
The core Rust logic of Mentci-AI is canonically identified as **mentci-aid**.

### Dual Etymology
1.  **Daemon (Computing):** A background process that handles autonomous tasks, orchestration, and system management. In Mentci-AI, `mentci-aid` is the `PipelineEngine` that traverses the `Attractor` DAGs.
2.  **Aid (Assistance):** Derived from the Latin root for "help." It signifies the system's role as a cognitive force multiplier—an agent that aids the human mind in navigating complex symbolic structures and implementation details.

## Role in the Architecture
`mentci-aid` sits at Layer 1 (Execution) of the system. It:
- Parses DOT/Aski-Flow/EDN workflow definitions.
- Manages the state machine of a running pipeline.
- Dispatches tasks to `Handlers` (Codergen, Agent, WaitHuman).
- Operates within the safety of the Nix Jail (Layer 0).
- Logs intent and outcomes to the Semantic Truth layer (Layer 2).

## Status: Not in a Running State
As of the current development phase, `mentci-aid` is **not in a running state**. 

While the structural components (`PipelineEngine`, `Handlers`, `ExecutionEnvironment`) are defined, the daemon is still an experimental prototype. It lacks the robust error handling, full schema integration, and mature handler implementations required for production use.

*The Great Work continues.*
