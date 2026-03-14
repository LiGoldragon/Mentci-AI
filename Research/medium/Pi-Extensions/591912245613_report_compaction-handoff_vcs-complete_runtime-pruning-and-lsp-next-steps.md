# Compaction handoff: VCS complete, runtime pruning progress, and LSP next steps

## Intent
Preserve the highest-value outcomes and next-step ideas from the completed lane before context compaction and a direction change.

## What is now complete

### 1. VCS completion happened properly
The user requirement was satisfied:
- commit before ending
- push whenever moving the bookmark

#### Top-level repo: `/home/li/git/Mentci-AI--dev`
Current bounded JJ anchor:
- working copy `@`: `a01ba56a` `(no description set)`
- parent `@-`: `6148e881`
- local bookmark `dev`: `6148e881`
- intent on `dev`:
  - `intent: remove jcodemunch runtime wiring and solar baseline output`

This lane explicitly ran a real push after bookmark movement and verified alignment.

#### Nested repo: `/home/li/git/Mentci-AI--dev/Components/CriomOS`
Current bounded JJ anchor:
- working copy `@`: `81a208d2` `(empty) (no description set)`
- parent `@-`: `bbfe7e55` `(empty) intent: increase Prometheus sanity lane context budget to 8192`
- local bookmark `dev`: `2dc0160f`
- intent on `dev`:
  - `intent: increase Prometheus sanity lane context budget to 8192`

Important nuance:
- the visible empty working-copy bookkeeping commits remain above the pushed non-empty target
- the actual pushed non-empty CriomOS target is:
  - commit `2dc0160f81ab57008d0a1afaeabcf2a4a0fbd31c`
  - change `ussppxvxkpsoqupoznwnkrlouysrzysw`

## Functional runtime/pruning progress that is now committed

### 2. `jcodemunch-mcp` was removed from active Pi runtime wiring
This is now the repo truth.

Changed runtime/build surfaces included:
- `Components/nix/pi-with-extensions.nix`
- `Components/nix/default.nix`
- `Components/nix/common_packages.nix`
- `Components/nix/pi_with_extensions_check.nix`

Important nuance:
- `Components/nix/jcodemunch-mcp.nix` still exists as a dormant derivation file
- but it is no longer part of active packaged Pi runtime startup wiring

### 3. active mutable `solar:` response requirements were removed
This is also now committed repo truth.

Affected surfaces included:
- `Core/AGENTS.md`
- `.pi/prompts/stop-if-unclear.md`
- `.pi/skills/independent-developer/SKILL.md`
- `Components/mentci-execute/src/actors/session_actor.rs`

Effect:
- the runtime no longer needs to inject a `solar:` line into session finalize messages
- active mutable prompt/skill authority no longer requires user-facing `solar:` prefix behavior

## Verification evidence already obtained in this lane
Subagent verification passed for the path-A mutation set:

- top-level flake check attr:
  - `checks.x86_64-linux.piWithExtensions`
- result:
  - built successfully

- `Components/mentci-execute` test suite:
  - `cargo test --quiet`
- result:
  - passed

Reviewer reconciliation conclusion:
- ready to hand back
- broader `session_actor.rs` concerns were classified as non-blocking hardening, not regressions introduced by this change set

## Biggest next insight: the next strongest runtime pruning candidate is `lsp-pi`
After removing `jcodemunch`, the strongest remaining always-loaded Pi runtime surface to question is:
- **`lsp-pi`**

### Why this matters
`lsp-pi` is not merely available in a shell package set. It is actually loaded by default in the packaged Pi runtime.

Evidence chain already established:
- `Components/nix/pi-with-extensions.nix`
  - symlinks `lsp-pi` into packaged runtime `node_modules`
  - passes it through `--extension` at Pi startup
- `Components/nix/pi_with_extensions_check.nix`
  - explicitly asserts `lsp-pi/src/lsp.ts` exists
  - explicitly greps for `node_modules/lsp-pi` in the packaged Pi wrapper
  - explicitly checks for `.nix` and `id: "nixd"` inside `lsp-core.ts`

So the high-value distinction is:

### Always-loaded at Pi startup
- `@aliou/pi-linkup`
- `pi-mcp-adapter`
- `lsp-pi`
- `pi-subagents-adapter`

### Merely packaged / shell-available
- `fava_trails`
- `fava_trails_mcp_server`
- `agentic_jujutsu`
- `litellm_proxy`
- `vtcode`
- `execute`
- various `mentci_*` utilities
- and other common/dev packages

Therefore, if the goal is reducing active startup context rather than just slimming dev package breadth, `lsp-pi` is the highest-leverage next target.

## LSP-specific conclusions

### 4. LSP is definitely loaded by default
There is no ambiguity here anymore.

### 5. Agents are also instructed to use it
The skill layer explicitly encourages LSP usage:
- `.pi/skills/subagent-driven-development/SKILL.md`
  - use `lsp symbols` / `definition`
  - run `lsp diagnostics`
  - use bounded `workspace-diagnostics`

So current repo truth is:
- runtime loads LSP by default
- mutable skills encourage agents to use LSP actively

### 6. Best filtering insertion point for noisy `_meta` / verbose payloads
Best next implementation target:
- **`lsp-pi` runtime adapter, specifically `lsp-core.ts`**

Reason:
- filtering there removes noise before it enters agent/tool prompt context
- prompt-only filtering is too late and still wastes tokens
- per-agent filtering is inconsistent and duplicative

### Recommended filtering behavior
Inside `lsp-core.ts`, implement canonical normalization that:
- strips `_meta`-style transport/detail fields by default
- caps or summarizes large arrays / diagnostics blobs
- preserves only intent-bearing fields by default:
  - file
  - range/position
  - severity/kind
  - symbol name
  - concise message
- optionally preserves a verbose/debug mode for full raw payloads

## Best next actions, ordered

### Option 1 — strongest next implementation target
Implement canonical payload filtering in `lsp-pi` / `lsp-core.ts`.

Why this is the best next step:
- directly addresses the operator complaint that `_meta` is noise
- directly reduces context poisoning while preserving LSP utility
- gives a fair test of whether LSP is useful after shaping its outputs around intent

### Option 2 — if LSP remains low-value after filtering
Make `lsp-pi` opt-in instead of always loaded.

That would require changing at least:
- `Components/nix/pi-with-extensions.nix`
- `Components/nix/pi_with_extensions_check.nix`

### Option 3 — further startup pruning after LSP decision
Revisit whether other always-on runtime surfaces should remain loaded by default.
At the moment, after `jcodemunch` removal, `lsp-pi` is the clearest next high-impact candidate.

## Non-blocking hardening concerns worth preserving
These were surfaced by review but are not blockers for the already-verified current change set:
- `Components/mentci-execute/src/actors/session_actor.rs`
  - brittle `resolve_rev` logic
  - blocking subprocess usage inside async actor code
  - better structured error reporting desired
  - stronger input validation desirable
  - better test coverage for failure/reply paths desirable

These should be treated as follow-up hardening, not as evidence that the current path-A change set was incorrect.

## Minimal restart truth
If only one paragraph survives compaction, it should be this:

> The `jcodemunch` runtime-wiring removal and `solar:` response-requirement removal are now committed and pushed. Top-level `dev` points to `6148e881` (`intent: remove jcodemunch runtime wiring and solar baseline output`), and nested `Components/CriomOS` `dev` points to `2dc0160f` (`intent: increase Prometheus sanity lane context budget to 8192`). The strongest next startup-context candidate is `lsp-pi`, because it is always loaded in the packaged Pi runtime, not merely shell-available. LSP is definitely loaded and the skills actively encourage its use. The best next implementation target is filtering noisy `_meta` / verbose payloads in `lsp-pi` `lsp-core.ts`; if LSP still feels low-value after that, make it opt-in instead of always loaded.
