# Level 5 Shipping Protocol: Jailed Commits via Jujutsu

## 1. Overview
In a Level 5 "Dark Factory" environment, the agent operates within a restricted Nix Jail. To ensure the integrity of the "Stable Contract" while allowing autonomous manifestation, Mentci-AI employs a **Jujutsu (jj) Workspace** model.
Operational steps live in `docs/architecture/VERSION_CONTROL.md`.

## 2. The Multi-Workspace Architecture
The `nix develop` environment (defined in `flake.nix`) partitions the project into two distinct areas:

1.  **Project Root (`MENTCI_REPO_ROOT`)**: The main repository containing the `.jj` directory and the definitive architectural files. In the agent's view, this area represents the **Intent** and is effectively Read-Only for structural changes.
2.  **`workspace/` (`MENTCI_WORKSPACE`)**: A secondary Jujutsu workspace created via `jj workspace add`. This is a fully writable implementation area where the agent manifests code, runs tests, and iterates. It is ignored by the main repository's `.gitignore`.

## 3. The `mentci-commit` Tool
Since the agent is isolated, it cannot directly manipulate the main repository's bookmarks without coordination. The `mentci-commit` tool provides the "Hole in the Jail" for shipping code.

### 3.1. Unique Intent IDs
Every agent session generates a **Unique Intent ID** consisting of a short hash and a descriptive name (e.g., `a7b2c9d4-implement-codergen`). This ID is used as a temporary Jujutsu bookmark for the session's work.

- **Generation:** Handled by `scripts/intent.clj`.
- **Targeting:** The `MENTCI_COMMIT_TARGET` environment variable is automatically set to this unique bookmark upon `nix develop` entry.
- **Traceability:** This ensures that every implementation attempt is isolated in its own namespace before being merged into `dev`.

### 3.2. Execution Flow
1.  **Work**: Agent writes code in `workspace/`.
2.  **Trigger**: Agent executes `mentci-commit "Feature: Implement Codergen"`.
3.  **Process**:
    - The tool identifies the current change in the `workspace/`.
    - It applies the provided description (commit message) to that change.
    - It uses `jj`'s native cross-workspace capability to advance the target bookmark (default: `dev`) in the main repository to point to the newly described change.
4.  **Result**: The host repository's `dev` bookmark now includes the agent's work, ready for review or further automation.

## 4. Technical Configuration
- **Package**: `pkgs.jujutsu` (providing the `jj` command).
- **Environment Variables**:
    - `MENTCI_REPO_ROOT`: Absolute path to the main repository.
    - `MENTCI_WORKSPACE`: Absolute path to the implementation workspace.
    - `MENTCI_COMMIT_TARGET`: The bookmark to advance (usually `dev`).

## 5. Security & Purity
This protocol ensures that:
- The agent never directly touches the `.jj` or `.git` metadata of the main repository.
- Every commit is an atomic manifestation from the workspace.
- The human "Director" can audit the `jj log` to see exactly what the agent "shipped" from its jail.
