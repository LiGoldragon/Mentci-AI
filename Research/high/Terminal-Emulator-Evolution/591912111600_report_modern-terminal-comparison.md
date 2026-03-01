# Research Report: Modern Terminal Emulators & Hot-Reloading

- **Solar:** 5919.12.11.16.00
- **Subject:** `Terminal-Emulator-Evolution`
- **Priority:** High

## 1. The "Foot" Bottleneck
While `foot` is highly efficient and low-latency, it follows a "minimalist" philosophy that often lacks the dynamic reconfiguration features required for advanced agentic workflows (e.g., hot-reloading fonts/themes without a full process restart).

## 2. Competitive Landscape (2025-2026)

Based on external validation via Linkup, four primary candidates emerge as replacements for `foot`:

| Feature | WezTerm | Ghostty | Kitty | Rio |
| :--- | :--- | :--- | :--- | :--- |
| **Hot-Reload** | **Automatic (Lua-based)** | Partial (Auto-config watch) | Manual (`SIGUSR1`) | Partial |
| **GPU Accel** | WebGPU (Vulkan/Metal) | Native (Metal/GTK) | OpenGL | WebGPU |
| **Config Lang** | Lua (Highly Scriptable) | Key=Value (Native UI) | Python-like `.conf` | TOML |
| **Graphics** | Sixel, iTerm2, Kitty | Kitty Graphics Protocol | Kitty Graphics Protocol | Sixel |
| **Nix Maturity** | High (Home Manager) | Emerging (Flake-only) | High (Home Manager) | Moderate |

### A. WezTerm (The "Intelligent" Choice)
- **Status**: The most programmable terminal available. 
- **Hot-Reloading**: Since the config is a Lua script, WezTerm watches the file and re-executes it on every save. This allows for **instant font and theme swaps** while the session is live.
- **Agent Integration**: Its Lua interface allows the terminal to be controlled programmatically, aligning with the Saṃskāra vision of "intercepting everything."

### B. Ghostty (The "Native" Speed)
- **Status**: Built with Zig, focusing on native performance and platform-specific UI (Mitchell Hashimoto).
- **Hot-Reloading**: Supports config watching, but some changes (like certain font properties) may still require a new window.
- **Advantage**: Extreme speed (<100ms startup) and native GTK/Metal integration.

### C. Kitty (The "Platform" Terminal)
- **Status**: Stable, powerful, and includes "kittens" (terminal extensions).
- **Hot-Reloading**: Requires sending a `SIGUSR1` signal to the process to reload `kitty.conf`. Not as seamless as WezTerm's auto-watch.

## 3. Recommendation for Saṃskāra Alignment
For a Level 5/6 "Dark Factory" environment, **WezTerm** is the superior architectural fit. 

**Rationale:**
1. **Scriptable State**: WezTerm's Lua configuration allows us to build logic that changes the UI based on the **Agent's active subflow** (e.g., changing the background color when the agent is in "Impure/Destructive" mode).
2. **Nix-Native**: Home Manager support for WezTerm is mature, allowing us to manage the terminal's symbolic state directly in our Nix flakes.
3. **Graphics Mastery**: Supports nearly every image protocol, essential for our future "Human-Information-Rich" UI displays.

---
*Verified via Linkup Research | 5919.12.11.16.00*
