# Project Survey: Mentci-AI (♓︎.9.47.48 | 5919 AM)

## 1. Executive Summary
Mentci-AI has reached a critical structural stability point. The repository has successfully transitioned to a **Rust-Only logic mandate**, established a robust **Logic-Data separation protocol** using Cap'n Proto and `mentci-user`, and integrated a high-performance Rust agent harness (`vtcode`) alongside the standard `pi` interface. The "Local Machine of Fit" philosophy is deeply embedded in the core guidelines, and the R&D tree is highly active and mirrored.

## 2. Component Health & Status

| Component | Status | Role |
| :--- | :--- | :--- |
| `mentci-user` | **Stable** | Authoritative secret injection via Cap'n Proto. |
| `mentci-vcs` | **Stable** | Generic Jujutsu/Git wrapper for session commits. |
| `chronos` | **Stable** | Solar baseline and ecliptic time authority. |
| `vtcode` | **Trial** | Rust-native semantic agent; patched for Gemini compatibility. |
| `pi` | **Active** | Primary TypeScript agent harness. |
| `mentci-mcp` | **Active** | AST-grep/Tree-sitter refactoring server. |
| `mentci-dig` | **Scaffolded** | Structural data extraction (replacing ad-hoc scripts). |
| `mentci-aid` | **Prototype** | Background daemon (experimental/not running). |
| `mentci-stt` | **Prototype** | Gemini-backed high-fidelity transcription. |

## 3. Strengths
- **Protocol Discipline:** High adherence to `intent:` scoping and `session:` synthesis.
- **Packaging Integrity:** Nix environment is highly optimized (Crane implementation for VTCode reduced rebuilds by ~90%).
- **Architectural Clarity:** The move to **Component Locality** for schemas and **Hash-Synced Binary Messages** solves a major technical debt regarding spec-logic coupling.
- **Lineage Recovery:** Mined chat history successfully consolidated the Sajban -> Sema -> Aski evolution.

## 4. Gaps & Technical Debt
- **The "Running State" Gap:** The project has many high-quality tools but lacks the `mentci-aid` daemon reaching a continuously running state to coordinate them autonomously.
- **Aski Implementation Lag:** Research notes describe a sophisticated Sajban IR (SIR) and Aski surface, but the current `Components/aski-lib` is minimal compared to the vision.
- **Redundant Parsing Logic:** Multiple components (`mcp`, `dig`, `fs`) are beginning to implement overlapping Tree-sitter and JSON/EDN parsing logic.
- **Safety Boundary Fragility:** The recent `GEMINI_API_KEY` leak (via `.vtcode/`) highlights that even with strict protocols, "environment pollution" remains a risk until `mentci-box` isolation is the mandatory entry point for *all* runs.

## 5. Actionable Suggestions

1.  **Consolidate `mentci-parse`:** Extract a shared Rust library for structural parsing (Tree-sitter, JSONPath, EDN query) to be used by `dig`, `mcp`, and `fs`.
2.  **Harden "Safe Path" Enforcement:** Update `root-guard` or `jj` hooks to automatically block commits containing common secret patterns or directories not in a project-wide `ALLOW_TRACK` list.
3.  **Prototype `mentci-aid` Loop:** Prioritize a "Hello World" background loop for the daemon that uses an `Attractor` DAG to perform a simple recurring task (e.g., auto-indexing new Research entries).
4.  **Implement `DigRequest` logic:** Use the newly created `mentci-dig` component to implement a `JsonPath` strategy, officially replacing the Python scripts used for ChatGPT exports.
5.  **Standardize "Trial" Workflows:** Create a `Library/guides/Trial_VTCode.md` to help future sessions switch between `pi` and `vtcode` without breaking environment state.

## 6. Questions for Author (Intent Guidance)

1.  **Daemon Priority:** Do we prioritize making `mentci-aid` reach a "Running State" next, or do we continue refining the individual tools (`dig`, `stt`, `mcp`) first?
2.  **Aski vs. EDN:** Should we start migrating Research/Development metadata from EDN to the new Aski grammar now, or wait for `aski-lib` to reach a stable parser/renderer stage?
3.  **Mentci-Box Maturity:** Is the goal for the next milestone to make *all* agent interactions occur inside a `mentci-box` by default, or keep the host `nix develop` shell as the primary workspace?
4.  **VTCode Direction:** Now that it's patched, should we attempt to port the Antigravity OAuth discovery logic into the `LiGoldragon/vtcode` fork, or is API key injection via `mentci-user` sufficient for your needs?
5.  **Data Extraction Goal:** What is the next major dataset you want to "Dig" through? (e.g., more chat exports, local repo history, or external documentation sources?)
