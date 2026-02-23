# VCS Wrapper Strategy Research

## Subject: Non-Interactive VCS Operations

### Immediate Fix: Deep Fix for JJ
To prevent `jj` from triggering interactive editors and pagers, the following actions were taken:
1.  **Local Configuration:** Created `.mentci/jj-project-config.toml` with:
    ```toml
    [ui]
    pager = "cat"
    editor = "true"
    ```
2.  **Environment Integration:** Updated `Components/nix/dev_shell.nix` to use `shellHook` for setting `JJ_CONFIG` to the absolute path of the local config. This ensures `jj` always picks up the non-interactive overrides regardless of global user settings.
3.  **Global Defaults:** Added `PAGER=cat` and `CI=true` to the dev shell environment.

### Long-Term Strategy: `mentci-vcs` Rust Wrapper
A new Rust component, `mentci-vcs`, has been initiated to provide a sophisticated, strictly non-interactive wrapper for all VCS operations.

#### Goals:
- **Strict Non-Interactivity:** Ensure every call to the underlying VCS (initially `jj`, potentially `git` or others) is executed with flags that suppress prompts.
- **Atomic Intent Enforcement:** Provide a high-level API that aligns with the Mentci `intent: <message>` and `session: <context>` protocols.
- **Environment Isolation:** Automatically manage `JJ_CONFIG` and other environment variables within the wrapper to guarantee consistent behavior across different host configurations.
- **Audit Logging:** Record all VCS operations in a structured format for agentic reasoning and session synthesis.

#### Roadmap:
1.  **Phase 1 (Done):** Initial library structure with `VCS` trait and `Jujutsu` implementation. Basic CLI tool.
2.  **Phase 2:** Implement advanced `jj` operations (squash, describe, bookmark management) within the wrapper.
3.  **Phase 3:** Integrate with `mentci-aid` and scripts to replace direct `jj` or `git` calls.
4.  **Phase 4:** Support for multi-VCS environments (e.g., managing both `jj` and `git` synchronization).
