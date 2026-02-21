# Strategy: Instruction-Artifact Sweep

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Hygiene and Semantic Purity)

## 1. Goal
Surgically remove redundant framing blocks and instruction-artifacts from the repository while preserving them in their canonical authority files.

## 2. Methodology
- **Registry of Artifacts:** Maintain a list of regex patterns representing "instruction-artifacts".
- **Surgical Removal:** Use `sed` or a Babashka script to remove matching blocks.
- **Authority Preservation:** Whitelist `core/ASKI_POSITIONING.md` and `core/AGENTS.md` to prevent removal of the original definitions.
- **Reporting:** Update `Logs/ARTIFACT_SWEEP_REPORT.md` with every sweep.

## 3. Tooling
Develop `strategies/artifact-sweep/src/sweep.clj` to:
1.  Iterate through all `.md` files.
2.  Detect the "Canonical Aski framing" block.
3.  Remove it if the file is NOT a whitelisted authority file.

## 4. Roadmap
- [ ] Implement `sweep.clj`.
- [ ] Conduct trial run on `strategies/` directory.
- [ ] Final project-wide sweep.

*The Great Work continues.*
