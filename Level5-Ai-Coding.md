# Level 5 AI Coding & Project Philosophy

## Current Level 5 Programming Intention
Based on the "Vibecoding" methodology and the "Dev Career Change" discourse, **Level 5 Programming** represents the transition from a "Code Writer" to a **"Modular Architect."**

### Key Characteristics:
*   **Architectural Direction:** The developer's primary role shifts from writing syntax to directing AI agents to build software in well-defined modules.
*   **High-Level Abstraction:** Focus is placed on system design, data flow, and component interaction rather than implementation details.
*   **AI as Workforce:** AI is treated not just as a copilot but as a capable junior developer or agent that executes "level 5" instructions to generate, refine, and debug code.
*   **Clean & Modular:** The output is strictly organized into cleaner, decoupled components and services, avoiding the "spaghetti code" often associated with lower-level AI assistance.

## Project Pillars

### Sema Object Style
*   *Interpretation:* A strict coding paradigm focusing on **Semantic Objects**. Code should be organized around data structures that carry semantic meaning and "intent" rather than purely functional procedures.
*   *Goal:* To ensure that the AI (and human maintainers) can infer the purpose and behavior of a component strictly from its object definition and interface.

### Capitalization-Based Durability Rules
*   *Interpretation:* A naming convention where the **capitalization** of an identifier dictates its **durability** (lifespan) and scope within the system.
    *   **Capitalized (e.g., `UserData`, `GlobalConfig`):** Indicates high durability, persistence, or global scope. These objects are meant to last.
    *   **Lowercase (e.g., `temp_index`, `loop_counter`):** Indicates low durability, transient state, or local scope. These are ephemeral and safe to discard.
