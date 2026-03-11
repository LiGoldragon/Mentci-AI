# Sane-JJ Integration Plan for `Components/mentci-vcs`

> Research anchor: `/home/li/git/Mentci-AI--dev/Research/medium/Vcs-Wrapper-Strategy/593312100000_answer_sane-jj_wrapper_strategy.md`

## Goal
Deliver a phased extension of `Components/mentci-vcs` that wraps the `jj` CLI with a typed, JSON-evidence-driven command surface (`sane-*` commands) so that agents can reason about dangerous operations before they mutate history.

## Phase 1: Command dispatch and typed builders
1. **Refactor `src/main.rs`:** Replace the flat `match` on `args[1]` with a command registry driven by a `CommandDef` struct. Each entry will include the command name, description, CLI mode (`mutating` vs `preview`), and the handler that receives parsed arguments and the runtime bookmark context.
2. **Introduce `src/command.rs`:** Define the `SaneCommand` enum and `CommandBuilder` helper that holds strongly typed parameters (bookmark IDs, intent tokens, preview flags) and sanitizes them before invoking `std::process::Command`. Re-use the existing `Jujutsu::base_command` environment sanitizers (`JJ_CONFIG`, `PAGER=cat`, `CI=true`).
3. **Wire `CommandBuilder` into handlers:** Every command must build its `jj` calls through the builder so quoting, dry-run, and evidence hooks are shared. Add an optional `--preview` flag to the builder that stubs the mutation path and emits the same JSON evidence as the real command (but without calling the actual `jj` invocation).

## Phase 2: Implement sane-JJ command set
1. **Preflight pipeline:** Implement `sane-preflight` handler inside `main.rs`. This command runs `jj status --json`, a bounded log over `${MENTCI_TARGET_BOOKMARK:-dev}|${MENTCI_TARGET_BOOKMARK:-dev}@origin|@|@-`, runtime bookmark inspection, and returns a single JSON packet with working copy cleanliness, pending bookmarks, and the current runtime bookmark path.
2. **Intent/commit lifecycle:** Add `sane-intent-start`, `sane-commit`, and `sane-finalize` handlers. Each must call `CommandBuilder` to add metadata (intent, session) and output change IDs+timestamps so downstream agents (e.g., `mentci-launch`) can record them.
3. **Bookmark and push guards:** Implement `sane-bookmark-move` and `sane-push-verify` handlers that call the builder, verify the target bookmark classification, optionally run `jj fetch`, and emit JSON evidence highlighting whether moving/pushing would drop the runtime bookmark or expose unapproved `@` revisions.
4. **Preview and cleanup commands:** Add `sane-rebase-preview` and `sane-cleanup` so agents can inspect and prune noise commits before pushing. Each command should run the relevant `jj` sequence via the builder, produce a `--json-output` artifact, and only mutate when explicitly asked (i.e., preview mode defaults to non-mutating).

## Phase 3: Safety architecture and evidence compliance
1. **JSON evidence schema:** Add `src/evidence.rs` with `serde::Serialize` structs for command metadata, runtime booked state, and diagnostic messages. Every `sane-*` command writes its packet to `<workspace>/.mentci/vcs-evidence/<command>-<timestamp>.json` and logs the path to stdout.
2. **Fail-closed policy hooks:** Before mutating commands, run the preflight pipeline internally; if any section reports dirty files, conflict markers, or missing remote commits, abort with a descriptive error inside the JSON packet. This implements the `fail-closed` rule so tooling cannot bypass the safety checks.
3. **Dry-run enforcement:** Share a common flag parser that prohibits combining `--preview` with mutating flags unless the caller simultaneously passes `--force-preview`, ensuring command codepaths remain separated and easy to audit.

## Phase 4: Verification, packaging, and rollout
1. **Tests:** Add unit tests for `CommandBuilder` quoting, evidence serialization, and preview gating (e.g., ensure the preview path never invokes `Command::status`). Continue running `cargo test -p mentci-vcs` for regression coverage.
2. **Integration check:** Document intended usage in `/home/li/git/Mentci-AI--dev/docs/plans/2026-03-11-jj-cleanup-training-and-sane-jj-plan.md` and share the new JSON schema with `mentci-policy-engine`. Update `/home/li/git/Mentci-AI--dev/Components/nix/mentci_vcs.nix` if new CLI flags require packaging adjustments.
3. **Agent training:** Once the CLI surface stabilizes, teach `jj`-facing agents (via `.pi/agents/*`) to call `mentci-vcs sane-...` commands, emphasizing that only the JSON evidence should be trusted for safety decisions.

Each phase should be committed behind a clean working copy (`jj status` empty) and verified via the runtime bookmark target (`$MENTCI_TARGET_BOOKMARK`) before moving to the next phase.
