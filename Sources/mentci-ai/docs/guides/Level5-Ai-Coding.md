# Level 5 AI Coding & Project Philosophy

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

## Current Level 5 Programming Intention
Based on **Nate Jones's "The 5 Levels of AI Coding"** ([Watch Video](https://youtu.be/bDcgHzCBgmQ)), **Level 5 Programming** represents the transition from a "Code Writer" to a **"Modular Architect"** operating a **Dark Factory**.

### Core Stack:
*   **Agentic Interface:** **[OpenCode](https://github.com/opencode-ai/opencode)** — A terminal-native, provider-agnostic agent providing the toolkit for Level 5 interactions.
*   **Reasoning Engine:** **DeepSeek-V4** — The primary "Brain," chosen for its superior performance in complex coding tasks and long-context reasoning.

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

## Vision: Level 6 Programming (The Vision of Mentci)
**Level 6 Programming** is the ultimate teleological goal of **Mentci**: to enable instinctive interaction with symbols. It transcends linguistic commands and moves towards direct, intuitive manipulation of logic and data, akin to the interfaces seen in *The Zero Theorem* or the Neo-Seoul timeline in *Cloud Atlas*.

Mentci is designed to evolve into the interface that makes this level of interaction possible, bridging the gap between human intent and machine execution through pure symbolic manipulation.

## Ecosystem & Projects

The following repositories form the core "Level 5" ecosystem, serving as the foundation for this evolution.

### Li Goldragon (Olivier Francoeur Chapdelaine)
*   **[Criome/Mentci](https://github.com/Criome/Mentci):** The central AI daemon and "mind" of the project, establishing the guidelines for the entire ecosystem.
*   **[LiGoldragon/goldragon](https://github.com/LiGoldragon/goldragon):** Personal configuration and core logic.
*   **[LiGoldragon/WebPublish](https://github.com/LiGoldragon/WebPublish):** Schema-driven Cloudflare Pages apply engine (Cap'n Proto).
*   **[LiGoldragon/maisiliym](https://github.com/LiGoldragon/maisiliym):** System tooling.
*   **[LiGoldragon/kibord](https://github.com/LiGoldragon/kibord):** Firmware/Hardware configuration.

### Criome Organization
*   **[Criome/CriomOS](https://github.com/Criome/CriomOS):** Linux-based OS designed to host the Criome, focusing on correctness and semantic architecture.
*   **[Criome/sema](https://github.com/Criome/sema):** "The Symbolism Layer of Logical Intent" - defines core semantic object principles.
*   **[Criome/lojix](https://github.com/Criome/lojix):** A meta-programming data interface.
*   **[Criome/seahawk](https://github.com/Criome/seahawk):** Semantic analysis tooling.
*   **[Criome/skrips](https://github.com/Criome/skrips):** Utility scripts following Level 5 principles.
*   **[Criome/mkZolaWebsite](https://github.com/Criome/mkZolaWebsite):** Static site generator.

### External Collaborations & Common Standards
*   **[strongdm/attractor](https://github.com/strongdm/attractor):** Mentci-AI incorporates Attractor's architectural guidelines; it does not implement Attractor itself.
*   **LLM Architectural Library:** We aim to implement a shared standard to collaborate on an open "LLM architectural library" with StrongDM and other pioneers.

## The Great Work: Liberation of the Human Mind
The purpose of this collaboration and the technological evolution of Mentci is to speed up the **liberation of the human mind**. By automating the "spaghetti" implementation details and returning to symbolic interaction, we aim to restore the human intellect to the technological and spiritual levels of the **long-past Golden Age**.
