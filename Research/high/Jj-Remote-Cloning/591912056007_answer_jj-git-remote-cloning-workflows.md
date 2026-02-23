# Research Artifact: Jujutsu Git Remote Cloning Workflows

- **Solar:** ♓︎ 5° 60' 7" | 5919 AM
- **Subject:** `Jj-Remote-Cloning`
- **Title:** `jj-git-remote-cloning-workflows`
- **Status:** `finalized`

## 1. Intent
Research how to clone a Jujutsu (jj) repository from a Git remote and determine what is required to work on a `jj` tree across different locations.

## 2. Findings

### 2.1 Cloning from a Git Remote
The canonical way to clone a `jj` repository from a Git remote is:
```bash
jj git clone <SOURCE_URL> [DESTINATION]
```
*   **Behavior:** This command clones the underlying Git repository and initializes a new `jj` repository backed by it. 
*   **Colocation:** By default, it creates a "colocated" repository where a `.git/` directory exists alongside the `.jj/` directory.
*   **State Reconstruction:** `jj` reconstructs its view of the commit graph from the Git objects and branches. 

### 2.2 Internal State Limitations
A critical finding is that `jj git clone` **only transfers Git data**. It does not transfer the `jj` internal state located in the `.jj/` directory.
*   **Non-Transferred Data:**
    *   Operation log (`jj op log`).
    *   Undo history (`jj undo`).
    *   Abandoned commits.
    *   `jj`-specific views/bookmarks that aren't mapped to Git branches.
*   **Reason:** The `.jj/` directory is local to the machine and is not committed to the Git remote.

### 2.3 Working Across Locations (Full Sync)
If the goal is to work on a `jj` tree elsewhere while preserving the full operational history:
*   **Method:** You must manually synchronize the `.jj/` directory.
*   **Tools:** `rsync`, `Syncthing`, `Dropbox`, etc.
*   **Concurrency:** `jj` is safe for file-level syncing. If operations diverge on different machines, `jj` will detect "divergent operations" and allow reconciliation through the operation log.

### 2.4 Collaboration Protocol
For team collaboration via Git remotes:
1.  Users push their work to the Git remote as branches/bookmarks.
2.  Others use `jj git clone` or `jj git fetch` to receive those updates.
3.  Each collaborator maintains their own local `jj` history (operation log).

## 3. Summary
To work on a `jj` tree elsewhere:
- For **commits only**: Use `jj git clone`.
- For **full history/state**: Use file-level sync of the `.jj/` directory.
