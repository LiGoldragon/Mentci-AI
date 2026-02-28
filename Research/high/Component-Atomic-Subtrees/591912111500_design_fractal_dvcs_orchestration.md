# Design: Fractal DVCS Orchestration & The Fork-Intent Database

## 1. Vision
Transform Mentci-AI into a fractal network of independent `jj` repositories coordinated by a central "Orchestrator" (likely `Saṃskāra`). Every operation is tracked not just by commit hashes, but by **Structured Intent Bookmarks** stored in a shadow SQLite database.

## 2. Bookmark Taxonomy: `UidHash--TypeIntent--Subflow`
Bookmarks follow a strict, machine-parsable schema to enable automated reasoning and merging.

*   **Prefix (UID Hash):** A 4-8 character stable hash representing the specific agent or session context.
*   **Separator:** `--`
*   **Type/Intent:** The semantic category of the work (e.g., `logic`, `research`, `schema`, `docs`).
*   **Suffix (Optional Sub-flow):** Further refinement for nested tasks (e.g., `--jjResearch`, `--fixLoop`).

**Examples:**
- `a8f2--jjResearch` (Initial research into jj)
- `a8f2--jjResearch--scaling` (Sub-flow exploring scaling limits)
- `b1c4--logicSamskara--bridge` (Implementation of the Saṃskāra bridge)

## 3. The Orchestration Database (`Shadow Index`)
A SQLite database acts as the global state observer across the thousands of `jj` repos.

### Table: `FlowRegistry`
| Field | Type | Description |
| :--- | :--- | :--- |
| `uid` | TEXT | The UidHash prefix |
| `component` | TEXT | Which Component/ repo this flows through |
| `intent` | TEXT | The primary intent (e.g., jjResearch) |
| `status` | ENUM | `in-progress`, `success`, `failed`, `merged` |
| `change_id` | TEXT | The stable `jj` Change ID |
| `parent_flow` | TEXT | Link to a parent UID for hierarchical merging |

## 4. The "Auto-Merge" Protocol
When a sub-flow (e.g., `a8f2--jjResearch--scaling`) is marked as `success` via verification tools:
1.  **Selection:** The Orchestrator identifies the parent flow (`a8f2--jjResearch`).
2.  **Rebase:** `jj rebase -s <subflow_change_id> -d <parent_change_id>`.
3.  **Conflict Handling:** If `jj` reports a first-class conflict, it is projected via `Saṃskāra` to a conflict-resolution agent.
4.  **Advancement:** Upon clean rebase, the parent bookmark is moved forward, and the sub-flow entry in SQLite is updated to `merged`.

## 5. Implementation Roadmap
1.  **Saṃskāra Projection:** Implement the tool that converts `jj log --template 'json(self)'` into the SQLite `FlowRegistry`.
2.  **Component Splitting:** Migrate `Components/*` into independent `jj` repositories (fractalization).
3.  **Intent Injection:** Update the `independent-developer` skill to automatically name bookmarks according to this schema.
