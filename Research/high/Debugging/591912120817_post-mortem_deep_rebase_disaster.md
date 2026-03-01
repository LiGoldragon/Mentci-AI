# Post-Mortem: The Deep Rebase Disaster and the Limits of Text-Based VCS

- **Chronography:** ♓︎12°8'17" | 5919 AM
- **Subject:** VCS Operations, Saṃskāra, and Jujutsu Protocol
- **Type:** Post-Mortem / Guidelines

## 1. What Happened (The Autopsy)

During an attempt to "trim the tree and recover valuable dangling commits," a cascading failure occurred that severely polluted the repository's history. The root causes were:

1. **The Ghost Ancestry Trap:** I identified "dangling" commits (e.g., the VTCode OAuth research) that had been abandoned in the past. To recover them, I used `jj rebase -r <old_commit> -d dev`. 
2. **The `.gitignore` Time-Machine Effect:** By rebasing ancient commits onto the modern `dev` branch, I didn't just bring the valuable markdown/code changes; I brought *the entire state of the repository at that exact moment in time*. At the time those old commits were created, directories like `.pi/npm/node_modules/` were likely not properly ignored. Rebasing them forced `jj` to track thousands of vendored files, bloating the snapshot and throwing massive size warnings.
3. **Chronological Scrambling:** Commits from mid-February were suddenly stacked on top of commits from March. This breaks the human-readable chronological timeline, making it impossible to understand the actual evolution of the codebase.
4. **The `jj undo` Spiral:** When the tree became messy, I attempted to use `jj undo` repeatedly. Because `jj undo` simply moves the state pointer backward one operation at a time, it quickly became a confusing labyrinth of divergent paths, rather than a clean rollback to a known good state.

## 2. Why Jujutsu Wasn't Enough (The Saṃskāra Mandate)

This failure perfectly illustrates the exact problem **Saṃskāra** is designed to solve. 

* **Git and JJ are Blind to Semantic Intent:** `jj` is a massive upgrade over Git for *manipulating* history, but it still treats a commit as a dumb snapshot of a file system (a raw Unix text/file blob). It doesn't know *why* a commit was abandoned, or that I only wanted the "idea" of the OAuth framework, not the exact file state of the repository from three weeks ago.
* **The Unspecified File Mess:** Because files aren't strictly typed objects (like in an EDN database or a Cap'n Proto struct), a missing line in a `.gitignore` file three weeks ago can resurrect megabytes of garbage dependencies today.

If Saṃskāra were fully operational, I wouldn't have rebased a commit object. I would have queried the Saṃskāra Intent Graph for the *structured data* of that research and injected it into the new state. 

## 3. New Operational Guidelines: "Content Extraction over Rebasing"

To prevent this from ever happening again, the agent must adhere to the following rules when dealing with historical/dangling commits:

### A. The Extraction Rule (Never Rebase the Past)
**NEVER** use `jj rebase` to bring an old, abandoned, or dangling commit into the modern trunk. 
Instead, extract the specific content:
- **Method 1 (Surgical):** `jj restore --from <old_commit> <specific_file_path>`
- **Method 2 (Semantic):** Read the file from the old commit (`jj diff -r <old_commit>`), understand its intent, and rewrite/apply it in a fresh commit on the current `dev` head.

### B. The Op-Log Rollback Rule
If an operation goes wrong, do **not** spam `jj undo`. 
1. Run `jj op log -n 10` to view the operation history.
2. Identify the exact operation ID *before* the mistake occurred.
3. Run `jj op restore <operation_id>` to surgically return to that exact clean state.

### C. The Visual verification Mandate
Before pushing or finalizing a session involving structural changes, run:
`jj log -n 10 --no-graph -T 'commit_id.short(8) ++ " " ++ committer.timestamp() ++ " " ++ description.first_line() ++ "\n"'`
If the timestamps are out of order, you have polluted the timeline. Abort and restore.

## 4. Suggestions and Questions

### Suggestions for immediate hygiene:
1. We need to do a hard `git gc` and `jj gc` (if/when safely configured) to ensure the massive `node_modules` blobs aren't permanently bloating the local `.jj` store. 
2. We should add an explicit `.gitignore` check script to the `session-guard` to ensure we never commit `node_modules` or `.voice-recordings` again, even by accident.

### Questions for Li (The Admin):
1. **Repository Reset:** Since the history is "salvaged but messy," do you want to keep the current `dev` trunk, or would you prefer I use `jj op restore` to jump all the way back to the last pristine release tag (`v0.12.11.60.55`) and manually extract the 2-3 research docs we actually care about?
2. **Saṃskāra Acceleration:** Should we pause standard feature development and put 100% effort into finishing the Saṃskāra Editor Bridge? If we force all commits through the structured bridge, it physically prevents this kind of filesystem-level pollution.
