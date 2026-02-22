# Roadmap: Jail Crypto Key Passing

## Milestone 1: Resolver Foundation
- Add `jail_key_resolver` script with deterministic path logic.
- Add permission hardening for `.mentci/keys` and key files.
- Add dry-run mode for validation without write.

## Milestone 2: Workflow Integration
- Call resolver from `commit` and `jj_workflow` wrappers.
- Inject `GIT_SSH_COMMAND` consistently for remote operations.
- Add workspace-local git signing config setup.

## Milestone 3: Verification
- Add unit tests for resolver logic.
- Extend `jail_push_ssh.rs` for gopass-driven key materialization path.
- Add signed-commit verification test.

## Milestone 4: Hardening
- Add guard to prevent accidental key persistence in tracked paths.
- Add optional post-session cleanup mode.
- Document operational runbook.
