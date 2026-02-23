# Strategy: Jujutsu Remote Cloning

## Objective
Define the protocol for cloning and working on `jj` repositories across different environments.

## Protocols

### 1. Light Work (Git-Backed)
Used for standard development where only the commit graph matters.
- **Action:** `jj git clone <url>`
- **Prerequisite:** Remote must be a standard Git repository.

### 2. Deep Sync (Full State)
Used when the `jj` operation log and undo history must be preserved.
- **Action:** Synchronize the entire project directory, including `.jj/` and `.git/`.
- **Note:** Ensure no `jj` processes are actively writing during the initial sync.

## Roadmap
1. [x] Research `jj git clone` behavior.
2. [x] Document internal state transfer requirements.
3. [ ] (Optional) Explore automated sync tools for Mentci workspace distribution.
