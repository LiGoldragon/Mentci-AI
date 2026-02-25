# High-Level Goals

This file defines durable, repository-level goals that guide orchestration and implementation priorities.

## Goal 0: mentci-aid Stabilization

Establish **mentci-aid** as a functional, Level 5 autonomous pipeline daemon.

Required outcome:
- `mentci-aid` reaches a continuously "Running State", listening on multiple sockets for Cap'n Proto messages from supervised child `mentci-aid` instances inside Mentci-Boxes.
- It successfully orchestrates complex DAGs within these nested Nix Jails.
- **[DONE]** Native Rust actor-based orchestration layer (`execute`) implemented.
- **[DONE]** Logic-Data Separation enforced via EDN sidecars.
- **[DONE]** Full Repository Sweep complete (Sections 1-10).
- **Assimilation:** Attractor is recognized purely as an inspiration for agent-flow. We do not fork it. Mentci-AI is implemented from first principles in Rust.
- **R&D Mirror Integrity:** `Development/` and `Research/` remain mirrored by subject so execution guidance and traceable findings stay aligned.
- **Control-Gate Integrity:** session and root guard health are maintained as release-blocking gates.
- **Init Envelope Purity:** runtime initialization state is transmitted as Cap'n Proto init message objects, not env-scattered domain config.
- **Terminalized Box Launch Path:** a dedicated launcher component strategy exists for systemd-managed Mentci-Box terminal sessions.

## Goal 1: Lojix/Aski Sub-Flow Handoff

Pass a subagent job intent as a homoiconic EDN/Lojix data artifact to a nested `mentci-box`.

Required outcome:
- A job intent is represented as a structured, advanced ASCII (EDN/Lojix) document, maximizing LLM context density.
- The legacy concept of DOT graph visualization is removed from the execution path.
- A custom Rust parser with advanced reader logic is implemented to natively read and write Aski state.
- A strict two-way utility is established: agents read/write "minified" Aski, which is deterministically expanded to "whitespace-beautiful" formats for human readability and VCS diffs.

## Goal 2: Rust-Only Scripting Standard

Eliminate all Python, Clojure, and ad-hoc shell scripts from the repository.

Required outcome:
- All orchestration, transcription, and tooling logic is implemented in native Rust.
- The `gemini` API is integrated via a native Rust client (or direct HTTP calls in Rust), not Python wrappers.
- The repository enforces a strict binary split: Nix for environment reproduction, Rust for all logic.

## Goal 3: Structural Code Editing Pipeline

Eliminate ad-hoc Python/regex file patching by agents.

Required outcome:
- Develop programmatic tools (`mentci-mcp`) that utilize Abstract Syntax Trees (AST) or Concrete Syntax Trees (CST) to edit code natively.
- Agents perform structural edits on Rust, EDN, and Cap'n Proto files rather than brittle text replacements.
- Pi is equipped with extensions or tools to leverage these capabilities.

## Goal 4: Core Logic-Data Separation Policy Enforcement

Eradicate all configuration data from logic files.

Required outcome:
- No hardcoded API keys, model names (e.g., "Gemini 2.5 Flash"), endpoints, or prompt text in Rust code.
- All such data is abstracted into Cap'n Proto specifications.
- Rust code is strictly generated from these Cap'n Proto schemas.
