# User Interface Documentation Guidelines

**Status:** Active  
**Objective:** Standardize how user interfaces (TUIs, CLIs, Web UIs) and their respective wrappers are documented within the Mentci-AI ecosystem.

## 1. Core Principles

User Interfaces in the Mentci-AI project (like `pi`, `vtcode`, `gemini-cli`, and their `mentci-*` wrappers) must adhere to the **Logic-Data Separation** and **Sema Object** principles. 

When documenting any new UI, the documentation **must** describe:
1. **The Abstracted Data Layer:** How the UI receives its configuration and secrets (it must never hardcode them or require raw `.env` files).
2. **The Execution Wrapper:** How the UI is instantiated within the Nix boundary (e.g., `mentci-vtcode`).
3. **The Data Contracts:** The structured data (Cap'n Proto, EDN, TOML) the UI reads or writes.

## 2. Required Documentation Sections for UIs

Whenever a new UI or Agent harness is integrated, its documentation (typically placed in `Library/documentation/` or its `README.md`) must include the following sections:

### 2.1. Component Identity
What is the tool? (e.g., "VT Code: Rust-based semantic terminal agent").
Where is the source? (e.g., "Fetched via Nix flake from `vinhnx/vtcode`").

### 2.2. Configuration & State (The "Data")
Where does the tool store its operational data?
- Explain the structured configuration file (e.g., `.vtcode/vtcode.toml` or `.pi/settings.json`).
- Document what default policies are applied (e.g., `default_policy = "allow"`).

### 2.3. Secret & Environment Injection (Logic-Data Separation)
**Crucial:** UIs must not manage secrets directly. 
- Document which environment variables the tool expects (e.g., `GEMINI_API_KEY`).
- Document how the `mentci-user` binary supplies these variables via structured JSON injection.
- *Example:* "VTCode reads `GEMINI_API_KEY`. This is supplied dynamically via the `mentci-user export-env` layer during shell initialization."

### 2.4. Wrapper / Launch Script
Document the exact launch wrapper. 
If the tool requires an initialized environment outside the strict Nix shell, explain the wrapper script (like `mentci-vtcode` or `mentci-launch`) that ensures the dependencies and secrets are evaluated prior to execution.

## 3. Example: VTCode Integration

See `Research/high/VTCode-Inspection/591912085453_answer_vtcode-integration-and-key-abstraction.md` for the canonical example of how VTCode was integrated and documented. 

**Summary of VTCode Integration:**
- **UI Tool:** `vtcode`
- **Config:** `.vtcode/vtcode.toml` (binds `provider="gemini"` and `api_key_env="GEMINI_API_KEY"`).
- **Secret Layer:** `mentci-user export-env` parses structured data and provides the raw key.
- **Wrapper:** `mentci-vtcode` forces the secret export before invoking the binary, guaranteeing the UI never hits an uninitialized state.
