# Roadmap to Level 5 Agentic Engineering

This document tracks the technical milestones required to reach Level 5 (The Dark Factory) and beyond. 

## Level 1: Completion (Assistive)
*Focus: Speeding up manual writing.*
- [x] **Context-Aware Autocomplete:** Underlying models (Gemini/DeepSeek) provide real-time suggestions.
- [x] **Single-File Context:** Ability to reason about the current buffer.

## Level 2: Chat (Interactive)
*Focus: Implementing specific snippets from natural language.*
- [x] **Conversational Implementation:** Agent can generate boilerplate and functions.
- [x] **Explaining Logic:** Agent provides rationale for code changes.
- [x] **Multi-File Reasoning:** Ability to read and relate multiple files (e.g., `Cargo.toml` and `main.rs`).

## Level 3: Agentic (Autonomous Action)
*Focus: Operating the system and tools to achieve a result.*
- [x] **Tool-Use Integration:** Agent can read/write files and execute shell commands.
- [x] **Sandboxed Environment:** Implementation of the **Nix Jail** (`jail.nix` and `jail_launcher.py`).
- [x] **Source Control Awareness:** Native integration with Git/JJ for atomic changes.
- [x] **Machine-Level Tooling:** Access to GDB, Strace, and Valgrind within the jail.
- [x] **Handshake Logging:** Structured EDN logs documenting intent vs. action (`scripts/logger.py`).

## Level 4: Multi-Agent (Orchestration)
*Focus: Decomposition of complex tasks into specialized sub-tasks.*
- [ ] **Agent Communication Protocol:** Define Cap'n Proto RPCs for agent-to-agent talk (`schema/mentci.capnp`).
- [ ] **Task Decomposition Engine:** A "Director" agent that breaks a prompt into a DAG of sub-tasks.
- [ ] **Specialized Sub-Agents:** Dedicated roles (e.g., Codergen, Tester, Security Auditor, Documenter).
- [ ] **Conflict Resolution:** Mechanisms for agents to resolve contradictory architectural choices.

## Level 5: Dark Factory (Architectural Intent)
*Focus: Total automation of the implementation-verification loop.*
- [ ] **Commit-on-Success:** The engine automatically creates a Jujutsu commit after every successful state-altering tool call.
- [ ] **Spec-First Engine:** Implementation of `Codergen` in the Rust daemon (`src/main.rs`).
- [ ] **Self-Healing Loops:** Agent automatically runs tests and iterates on failure without human prompts.
- [ ] **Architectural Enforcement:** Linting matrix that blocks code violating **Sema Object Style**.
- [ ] **Topological Synthesis:** Generating entire modules based solely on Schema/EDN definitions.
- [ ] **Lights-Out Production:** The "Director" manages the factory; the human only reviews the Final Report.

## Level 6: Symbolic Interaction (The Vision)
*Focus: Instinctive manipulation of symbols.*
- [ ] **Direct Logic Manifestation:** Bypassing text-based coding for direct symbolic mapping.
- [ ] **Universal Schema:** A single semantic truth that generates OS, App, and Hardware configs.

---
**Current Status:** Level 3 (Operational) 
**Active Target:** Level 4 (Orchestration)
**Ecliptic Date:** ♓︎ 1° 28' 44" | 5919 AM
