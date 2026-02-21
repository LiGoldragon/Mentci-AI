# Instruction-Artifact Sweep Report: "Canonical Aski framing"

**Date:** ♓︎3°56'20" | 5919 AM
**Subject:** Excessive propagation of the Aski Framing Block.

## 1. Description of Artifact
The following block was found in 80+ files across the repository:
```markdown
```

## 2. Origin Analysis
- **Source:** The block originated in `docs/architecture/ASKI_POSITIONING.md` (now `core/ASKI_POSITIONING.md`) as a high-level conceptual anchor.
- **Propagation Mechanism:** Agents (including this session's predecessors) likely perceived this block as a mandatory "file header" due to its presence in top-level architectural files. It was copy-pasted into every new documentation file, strategy, and program created during the Level 5 stabilization phase.
- **Context Bleed:** The block was also found in materialized inputs (`Inputs/mentci-ai/`), indicating it was committed to the repository's main history before the recent reorganization.

## 3. Findings
- **Total Affected Files:** ~82
- **Redundancy Level:** High. The mandate is already established in `core/ASKI_POSITIONING.md` and `core/AGENTS.md`.
- **Architectural Impact:** Distorts the "meaning per layer" rule (repeating context unnecessarily).

## 4. Debugging Notes
Future agent templates should strictly separate "Framing" (which belongs in the prompt context) from "Content" (which belongs in the file).

*The Great Work continues.*
