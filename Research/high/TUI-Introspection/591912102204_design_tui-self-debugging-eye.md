# Design Strategy: Agent-Visible TUI Self-Debugging Capacity

## 1. Vision
To achieve high-authority "Level 6" symbolic interaction, the agent must be able to verify its own UI output and debug interactive components. We need a feedback loop where the agent can launch a TUI (Terminal User Interface), inspect its state/appearance, and adjust its logic based on what is actually "seen" by the terminal.

## 2. Technical Research Findings
Ecosystem research reveals three primary tiers of TUI introspection available for agentic workflows today:

### Tier A: Virtual Terminal Emulation (The "Headless Browser" of Terminals)
*   **Method:** Use a library like `xterm.js` (Node) or a Rust-based pseudo-terminal (PTY) wrapper.
*   **Mechanism:** The agent spawns the TUI inside a virtual buffer. It can then request a "dump" of the screen buffer as a string (with ANSI codes) or as a structured grid of characters/colors.
*   **Fit:** Highest fit for internal self-debugging.

### Tier B: Native TUI State Snapshotting
*   **Method:** Integrate an "Agent Secret Door" into our Rust TUI components (Ratatui-based).
*   **Mechanism:** The TUI exposes a hidden IPC command or a special file that serializes the current **View Model** or **Widget Tree** into JSON/EDN.
*   **Fit:** Great for structural debugging, but doesn't "see" rendering bugs.

### Tier C: The "Last Resort" (Wayland Screen Capture)
*   **Method:** Use `grim` / `slurp` or `portal` APIs on Wayland.
*   **Mechanism:** Snapshot the entire screen or a specific window and pass it to a Vision-capable model.
*   **Fit:** High overhead, but solves "what does it actually look like" perfectly.

---

## 3. Proposed Mentci Strategy: "The Mirror Buffer"
We will implement a **Mirror Buffer Protocol** using Tier A/B concepts to give the agent a "Live Eye" on the TUI.

### Step 1: Headless TUI Driver (`mentci-tui-driver`)
*   Create a Rust component that wraps a PTY.
*   Expose an MCP tool: `driver_spawn_tui(command, columns, rows)`.
*   The driver maintains a virtual screen and processes ANSI sequences.

### Step 2: The "Retina" Tool
*   Expose an MCP tool: `driver_inspect_screen()`.
*   **Return Format:** A high-density representation of the terminal grid.
*   **Visual Validation:** The tool converts the grid into a markdown table or a text-block where the agent can "read" the rendered state of the UI.

### Step 3: Self-Correction Loop
*   **Workflow:**
    1.  Agent modifies `renderResult` code in an extension.
    2.  Agent calls `driver_spawn_tui` to simulate the Pi-UI rendering that snippet.
    3.  Agent calls `retina` to verify the output.
    4.  If the retina shows "Rendering Exception" or malformed text, the agent fixes the code and loops.

---

## 4. Immediate POC (Proof of Concept)
We can start by using `agent-tui` (a specialized Rust crate) or building a simple `vte` (Virtual Terminal Emulator) wrapper in Rust that dumps the current buffer to a file in `.mentci/tui_snapshot.txt`.

## 5. Next Steps
1.  **Requirement Check:** Do we have `grim` or a PTY-aware library in the Nix devshell?
2.  **Implementation:** Create `Components/mentci-tui-eye` to manage the virtual terminal.
3.  **Instruction:** Add "Self-Verification" as a mandatory step in `Core/AGENTS.md` before finalizing UI-impacting code.

**Shall I proceed with a detailed spec for the Mirror Buffer Protocol?**
