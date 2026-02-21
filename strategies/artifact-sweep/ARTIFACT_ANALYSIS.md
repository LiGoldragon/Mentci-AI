# Artifact Analysis: Obsolete Scaffolding (Strike-2)

**Date:** ♓︎4°1'59" | 5919 AM
**Subject:** Deconstruction of Strike-2 artifacts and prevention strategy.

## 1. Artifact Deconstruction

### 1.1 `ARCHITECTURE.md` (Root)
- **Original Purpose:** A high-level entry point for humans to understand the system layers.
- **Obsolecence Factor:** Superseded by `core/ARCHITECTURAL_GUIDELINES.md` (Authoritative rules) and `core/programs/RESTART_CONTEXT.md` (Operational map). Its existence in the root creates a "Dual Truth" conflict.

### 1.2 `Work.md` (Root)
- **Original Purpose:** An early session log and todo list for the developer.
- **Obsolecence Factor:** Superseded by `Logs/RELEASE_MILESTONES.md` and the `jj log`. Tracking work in a mutable markdown file in the root is a "vibe-coding" pattern that bypasses the formal audit trail.

### 1.3 `docs/architecture/JjAutomation.md`
- **Original Purpose:** Defining the JJ workflow.
- **Obsolecence Factor:** Explicitly superseded by `core/VERSION_CONTROL.md`. It is a "Dead Specification" that remains in the filesystem only due to lack of a purge task.

## 2. Why these look obsolete
These files are **Scaffolding Artifacts**. They were necessary during the "Brainstorming" phase but became obsolete once the project's **Core Programming** (the `core/` directory) was hardened. They look obsolete because they lack the rigorous "Sema" metadata and "Aski" framing of the new standard, and they often point to outdated file paths.

## 3. Prevention Strategy: "Convergent Evolution"

To avoid the accumulation of such artifacts, the following rules are added to the **Artifact-Sweep Strategy**:

1.  **Mandatory Convergence:** Any document that defines a "Guideline," "Protocol," or "Goal" must be moved to `core/` or purged within one session of its creation.
2.  **Fragment Expiration:** Small markdown files in the root or `docs/` that are not part of a `strategies/` or `tasks/` subject are assigned Strike-1 automatically upon project-wide sweep.
3.  **SSOT Enforcement (Single Source of Truth):** New documentation must use links to `core/` files rather than summarizing them. Any file that repeats >30% of a `core/` mandate is an artifact.
4.  **Bootstrap-Cleanup Tasks:** Every major structural reorganization (like the move to `core/`) must be followed by a dedicated `PURGE` task to clean up the displaced files.

*The Great Work continues.*
