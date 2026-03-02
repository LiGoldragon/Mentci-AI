# Strategy: Instruction-Artifact Sweep (Universal)

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Purity and Token Economy)

## 1. Goal
Surgically remove redundant context-leakage and instruction-artifacts from the repository. Maintain a "High-Signal" filesystem.

## 2. Methodology: Pattern Discovery
- **Frequency Analysis:** Use `grep` and `uniq -c` to find repetitive blocks across documentation and implementation files.
- **Context Mapping:** Compare discovered blocks against `Core/` mandates. If the information is already in `Core/`, it is an artifact.
- **Strike System:** Flag these patterns in `Logs/ObsolescenceStrikes.edn`.

## 3. Implementation trials
- [x] Trial 1: Purge the "Aski framing" block (Completed session ♓︎3°60'7").
- [ ] Trial 2: Develop a script `scripts/artifact_finder/main.clj` to find any non-code string > 100 chars that appears in > 5 files.

*The Great Work continues.*
