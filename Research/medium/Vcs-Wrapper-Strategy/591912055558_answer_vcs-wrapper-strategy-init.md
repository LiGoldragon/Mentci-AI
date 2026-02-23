# Research Artifact: VCS Wrapper Strategy Initiation

- **Solar:** ♓︎ 5° 55' 58" | 5919 AM
- **Subject:** `Vcs-Wrapper-Strategy`
- **Title:** `vcs-wrapper-strategy-init`
- **Status:** `in-progress`

## 1. Intent
Ensure non-interactive and protocol-compliant VCS operations (primarily `jj`) within the Mentci-AI ecosystem.

## 2. Findings
*   **JJ Interactivity:** To prevent `jj` from triggering interactive editors and pagers, a local `.mentci/jj-project-config.toml` was established and linked via `JJ_CONFIG` in the dev shell.
*   **Environment Defaults:** `PAGER=cat` and `CI=true` are now enforced in the development environment.

## 3. Long-Term Strategy: `mentci-vcs`
A dedicated Rust component `mentci-vcs` is being developed to wrap all VCS calls.
*   **Strict Non-Interactivity:** High-level API that suppresses all prompts.
*   **Atomic Intent:** Enforcement of `intent:` and `session:` commit protocols.
*   **Auditability:** Structured logging of all VCS actions.

## 4. Roadmap
1.  **Phase 1 (Done):** Initial library with `VCS` trait and `Jujutsu` implementation.
2.  **Phase 2:** Advanced `jj` operations (squash, describe, bookmarks).
3.  **Phase 3:** Integration with `mentci-aid` and orchestration scripts.
