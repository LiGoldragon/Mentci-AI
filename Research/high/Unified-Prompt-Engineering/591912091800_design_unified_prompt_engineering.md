# Unified Prompt Engineering: Mentci Intelligence Layer (MIL)

- **Solar:** 5919.12.9.18.0
- **Subject:** `Unified-Prompt-Engineering`
- **Status:** `design-draft`
- **Weight:** `High`

## 1. Intent
To design a harness-independent prompt engineering system that uses a structured, binary-backed format (Sajban SIR) to manage agent intelligence, reducing context overhead and enabling seamless portability between `pi`, `vtcode`, and other agent environments.

## 2. Research: Current Harness Patterns

### 2.1 Pi (BADLOGIC/PI)
- **Model:** File-based Markdown concatenations.
- **Loading:** Loads `SYSTEM.md`, `APPEND_SYSTEM.md`, and hierarchical `AGENTS.md`.
- **Injection:** Via extension hooks (`pi.on("session_start")`) and `pi.appendSystemPrompt()`.

### 2.2 VT Code (VINHNX/VTCODE)
- **Model:** Rust-orchestrated system prompts (variants: minimal, lightweight, specialized).
- **Injection:** Uses `IncrementalSystemPrompt` to cache and build strings from configuration, workspace context, and hierarchy.
- **Skill Model:** Tiered disclosure (Discovery -> Instruction -> Deep Resource).

## 3. The MIL Solution: Structured Intelligence

### 3.1 Efficient Format (Sajban SIR)
Instead of large Markdown files, we use **Cap'n Proto** messages for the "Intelligence Carrier."
- **Why?** Binary format allows for granular pruning (only inject what's needed), deterministic hashing, and cross-language portability.
- **Synchronization:** Every carrier binary (`intel_<hash>.bin`) is paired with a text-version (`intel.txt`). The hash ensures the logic and data remain synchronized.

### 3.2 Injection Architecture
1.  **GuidanceProvider (Rust):** A service that reads the `IntelligenceCarrier` and projects it into the required surface (Markdown for Pi, Rust strings for VTCode).
2.  **Pi Adapter:** A global extension that fetches the guidance envelope from the provider at startup.
3.  **VTCode Adapter:** A lifecycle hook that hydrates the local instructions from the provider.

## 4. Portability Implementation (Parallel Workspaces)
To "port intelligence" to another workspace:
1.  Generate the `mentci_intel_♓︎.bin` carrier from the core repo.
2.  Bootstrap the target workspace using `mentci-user bootstrap <hash>`.
3.  The agent harness (Pi/VTCode) automatically receives the user's psyche-extension via the provider, without needing the full `mentci-ai` tree on the filesystem.

## 5. Next Steps
1.  Refactor `Core/AGENTS.md` and `Core/PHILOSOPHY_OF_INTENT.md` into the `IntelligenceCarrier` schema.
2.  Implement the projection logic (SIR -> Markdown).
3.  Build the automated injection hooks.
