# Obsolescence Protocol (Two-Strike Rule)

**Status:** Operational
**Objective:** Maintain repository hygiene by systematically identifying and purging obsolete artifacts (leftovers from vibe-coding, earlier prototyping, or brainstorming).

## 1. The Strike System
A file enters the obsolescence pipeline when it is determined to be non-functional or redundant within the current architectural context.

1.  **Strike 1:** The file is identified as potentially obsolete during a system sweep. It is added to `Outputs/Logs/ObsolescenceStrikes.edn` with `strikes: 1`.
2.  **Strike 2:** A subsequent sweep confirms the obsolescence. The strike count is incremented to `2`.
3.  **Strike 3:** A final confirmation sweep. Once a file has **three strikes**, it is eligible for immediate removal from the filesystem in the next sweep.

## 2. Tracking
Strikes are recorded in `Outputs/Logs/ObsolescenceStrikes.edn` using the following schema:

```clojure
{
  "/path/to/file" {:strikes 3 :reason "vibe-coding leftover" :first_sweep "♓︎..." :last_sweep "♓︎..."}
}
```

## 3. Candidate Identification
Criteria for a strike:
- No references in `Core/` or active `tasks/`.
- Superseded by a newer implementation (e.g., Python scripts replaced by Clojure).
- **Instructional Artifacts:** Redundant framing blocks, leaked prompt context, or boilerplate that repeats information already established in `Core/`.
- Empty or boilerplate files with no implementation logic.
- Documentation that contradicts current `Core/` mandates and has not been updated.

## 4. Prevention: The "Purity Gate"
Agents must not replicate high-level context into individual files. 
- **Rule:** If a piece of information is true for the whole directory, it belongs in a `RestartContext.md` or a `Core/` mandate, not in the file headers of every child.
- **Detection:** A sweep should flag any string pattern that appears in >10% of non-code files.

*The Great Work continues.*
