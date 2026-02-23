# Research Artifact: StrongDM Input Realignment

- **Solar:** ♓︎ 5° 55' 44" | 5919 AM
- **Subject:** `Attractor`
- **Title:** `strongdm-input-realignment`
- **Status:** `finalized`

## 1. Intent
Align the project's StrongDM dependencies with current "Level 5 agentic coding" priorities by removing obsolete inputs and adding modern sandbox tooling.

## 2. Findings
*   **Obsolete Inputs:** `github:strongdm/comply` (compliance automation) and `github:strongdm/cxdb` (connectivity infrastructure) were identified as irrelevant to the core Mentci-AI mission and obsolete relative to StrongDM's recent focus.
*   **Modern Alternatives:** `github:strongdm/leash` was identified as a key recently-updated project focused on security sandboxing for AI agents (e.g., Claude Code, Codex CLI), which is directly relevant to Mentci-AI's isolation goals.
*   **Stable Inputs:** `github:strongdm/attractor` remains a core non-interactive coding agent engine and is retained.

## 3. Actions Taken
*   **`flake.nix`**:
    *   Removed `comply` and `cxdb` from `inputs`.
    *   Added `leash` to `inputs`.
    *   Updated `outputs` and `inputs` passing logic.
*   **`Components/nix/jail_sources.nix`**:
    *   Removed `comply` and `cxdb`.
    *   Added `leash`.
*   **`Components/nix/jail.nix`**:
    *   Updated `sourceManifest` to include `leash` and remove `comply`/`cxdb`.
*   **`Library/specs/ASKI_FS_SPEC.md`**:
    *   Updated symbolic mapping to include `leash`.
*   **`flake.lock`**:
    *   Refreshed lock file to reflect input changes.

## 4. Verification
*   `nix flake check --no-build` passed.
*   Input `leash` successfully added and locked.
