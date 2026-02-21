# Obsolescence Protocol (Two-Strike Rule)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `core/ASKI_POSITIONING.md`.

**Status:** Operational
**Objective:** Maintain repository hygiene by systematically identifying and purging obsolete artifacts (leftovers from vibe-coding, earlier prototyping, or brainstorming).

## 1. The Strike System
A file enters the obsolescence pipeline when it is determined to be non-functional or redundant within the current architectural context.

1.  **Strike 1:** The file is identified as potentially obsolete during a system sweep. It is added to `Logs/OBSOLESCENCE_STRIKES.edn` with `strikes: 1`.
2.  **Strike 2:** A subsequent sweep (in a different session or by a different agent) confirms the obsolescence. The strike count is incremented to `2`.
3.  **Purge:** Once a file has **two strikes**, it is eligible for immediate removal from the filesystem in the next sweep.

## 2. Tracking
Strikes are recorded in `Logs/OBSOLESCENCE_STRIKES.edn` using the following schema:

```clojure
{
  "/path/to/file" {:strikes 1 :reason "vibe-coding leftover" :first_sweep "♓︎..." :last_sweep "♓︎..."}
}
```

## 3. Candidate Identification
Criteria for a strike:
- No references in `core/` or active `tasks/`.
- Superseded by a newer implementation (e.g., Python scripts replaced by Clojure).
- Empty or boilerplate files with no implementation logic.
- Documentation that contradicts current `core/` mandates and has not been updated.

*The Great Work continues.*
