# Mentci Intelligence Layer (MIL): Portability & Prompt Injection Design

- **Solar:** 5919.12.9.17.50
- **Subject:** `Intelligence-Portability`
- **Status:** `design-draft`
- **Weight:** `High`

## 1. Intent
To create a system that decouples the user's "intelligence" (instructions, guidelines, project history, and persona) from the `mentci-ai` repository, allowing it to be injected into any workspace or agent harness (Pi, VTCode, Gemini-CLI) without requiring the agent to load files manually from the filesystem.

## 2. Motivation
Currently, agents spend significant context window tokens loading `Core/AGENTS.md` and other guidance files. This process is repetitive and workspace-bound. By migrating these instructions to a centralized "Intelligence Layer," we can:
1.  **Inject the right prompt/skills automatically** based on the current task.
2.  **Port intelligence across workspaces** seamlessly.
3.  **Work in parallel** with multiple agents across different trees without duplicating the whole `mentci-ai` infrastructure.

## 3. Architecture

### 3.1. Intelligence Provider (Rust Actor)
A core service (potentially part of `mentci-aid`) that manages the "Intelligence Database" (Sajban-based).
- **Function:** Serves specific prompt fragments and skill instructions via Cap'n Proto.
- **Trigger:** When a session starts or a task is dispatched, the provider analyzes the intent and returns a synthesized "Guidance Envelope."

### 3.2. Guidance Envelope (Cap'n Proto)
A structured message containing:
- `system_prompt_overrides`: The core "how to act" instructions.
- `relevant_skills`: List of skill definitions (standardized).
- `project_context`: High-level mission and snapshot.
- `solar_baseline`: Automatic time synchronization.

### 3.3. Injection Mechanisms
- **Pi-Extension:** A global Pi extension that calls the Intelligence Provider at `session_start` and uses `pi.appendSystemPrompt()` or `ctx.ui.inject()`.
- **VTCode Lifecycle Hook:** A `pre_session` hook in `vtcode.toml` that runs a Rust binary to hydrate the local workspace instructions from the provider.
- **MCP Tool:** An MCP tool `get_guidance` that agents can call explicitly if they feel unaligned.

## 4. Portability Flow (The "Intelligence Carrier")
1.  **Carrier Artifact:** A single, content-addressed Cap'n Proto binary (e.g., `mentci_intel_♓︎.bin`) that contains the entire guidance tree.
2.  **Bootstrap:** In a new workspace, the user simply runs `mentci-user bootstrap <hash>`.
3.  **Hydration:** `mentci-user` (acting as the carrier client) fetches the artifact and sets up the local `devShell` and agent hooks to point to this intelligence.

## 5. Implementation Roadmap
1.  **Phase 1:** Refactor `Core/` guidelines into a structured Cap'n Proto schema.
2.  **Phase 2:** Implement the `GuidanceProvider` actor in Rust.
3.  **Phase 3:** Build the Pi-Extension and VTCode hooks for automatic injection.
4.  **Phase 4:** Standardize the "Carrier" format for workspace-to-workspace portability.

## 6. Goal
The agent should "just know" how to act because the harness (Pi/VTCode) was pre-loaded with the user's psyche-extension before the first prompt was even sent.
