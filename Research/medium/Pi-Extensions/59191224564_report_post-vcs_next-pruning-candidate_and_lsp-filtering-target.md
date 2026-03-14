# Post-VCS continuation: next Pi pruning candidate and LSP filtering target

## Intent
Record the immediate post-VCS continuation findings after:
- removing `jcodemunch-mcp` from active Pi runtime wiring,
- removing active mutable `solar:` response requirements,
- and finishing the required commit/push flow.

## VCS completion summary
The preceding lane finalized and pushed both affected repos:

### Nested `Components/CriomOS`
- final intent pushed on `dev`:
  - `intent: increase Prometheus sanity lane context budget to 8192`
- final non-empty change id:
  - `ussppxvxkpsoqupoznwnkrlouysrzysw`
- final commit id reported by JJ expert:
  - `2dc0160f81ab57008d0a1afaeabcf2a4a0fbd31c`
- `dev == dev@origin` verified by the bounded JJ lane

### Top-level `Mentci-AI--dev`
- final intent anchored/pushed on `dev`:
  - `intent: remove jcodemunch runtime wiring and solar baseline output`
- non-empty target revision identified/pushed:
  - change `lntrltkm`
  - commit `6148e881`
- explicit push command was run after bookmark move
- `dev == dev@origin` verified by the bounded JJ lane

## Next-item findings

### 1. Strongest next startup/runtime pruning candidate: `lsp-pi`
After removing `jcodemunch`, the strongest remaining always-loaded Pi runtime candidate for pruning/deferral is now:
- **`lsp-pi`**

Why:
- it is not merely packaged in the dev environment; it is explicitly loaded by the packaged Pi runtime on every startup
- it is symlinked into the runtime `node_modules`
- the generated `pi` wrapper passes it via `--extension`
- the runtime check explicitly expects it to exist and even checks for `.nix` / `nixd` references in `lsp-core.ts`

### 2. Exact always-loaded vs merely-packaged distinction
#### Always-loaded in packaged Pi runtime
From `Components/nix/pi-with-extensions.nix`, the following are explicitly passed to the Pi CLI at startup:
- `@aliou/pi-linkup`
- `pi-mcp-adapter`
- `lsp-pi`
- `pi-subagents-adapter`

#### Merely packaged / shell-available, not automatically injected into Pi runtime
From `Components/nix/common_packages.nix`, these are present in the developer environment but not automatically loaded by the Pi binary at startup:
- `fava_trails`
- `fava_trails_mcp_server`
- `agentic_jujutsu`
- `litellm_proxy`
- `vtcode`
- `unified_llm`
- `execute`
- `mentci_stt`
- `mentci_user`
- `mentci_mcp`
- and other shell/runtime support packages

So if the goal is to reduce active startup context rather than just package list breadth, `lsp-pi` is the next highest-leverage candidate.

## LSP answer
### Is LSP loaded by default?
Yes.

Evidence chain:
- `Components/nix/pi-with-extensions.nix` links `lsp-pi` into runtime `node_modules`
- the same file adds:
  - `--extension "''${PI_PACKAGE_DIR}/node_modules/lsp-pi"`
- `Components/nix/pi_with_extensions_check.nix` asserts the packaged runtime contains:
  - `lsp-pi/src/lsp.ts`
  - `lsp-pi/src/lsp-core.ts`
  - `.nix` references
  - `id: "nixd"`

### Are agents instructed to use it effectively?
Yes, at least at the skill layer.

Evidence:
- `.pi/skills/subagent-driven-development/SKILL.md` instructs use of:
  - `lsp symbols`
  - `definition`
  - `diagnostics`
  - bounded `workspace-diagnostics`

So the current state is:
- LSP is definitely loaded,
- and the repo’s mutable skills do instruct agents to use it.

## Best filtering insertion point for noisy results
### Recommended target
- **`lsp-pi` runtime adapter / `lsp-core.ts`**

### Why this is the best target
Filtering in `lsp-pi` itself is the cleanest place because it is the shared runtime adapter that receives and shapes LSP payloads before they become agent/tool prompt context.

This is better than:
- prompt-only filtering, which still sends noise into context,
- or per-agent filtering, which is inconsistent and duplicative.

### Recommended behavior
Inside `lsp-core.ts`, add a deterministic normalization step that:
- removes `_meta`-style fields by default,
- caps large arrays / verbose diagnostics payloads,
- preserves core intent-bearing fields only:
  - file
  - range/position
  - severity/kind
  - symbol name
  - concise message
- optionally exposes a verbose/debug mode for full raw payloads when needed

## Practical next implementation order
1. Decide whether `lsp-pi` should remain always-loaded or become opt-in.
2. If it remains loaded, implement filtering first in `lsp-core.ts`.
3. If it becomes opt-in, update:
   - `Components/nix/pi-with-extensions.nix`
   - `Components/nix/pi_with_extensions_check.nix`
   and then re-verify startup/runtime behavior.
4. Re-check whether skill guidance should still recommend routine LSP usage once the runtime surface is reduced.

## Recommended next action
The single best next implementation target is:
- **add canonical payload filtering in `lsp-pi` / `lsp-core.ts`**

Reason:
- it directly addresses the operator’s complaint about `_meta` and high-noise result structure,
- while preserving the possibility that LSP remains useful once the results are shaped around intent rather than raw transport detail.

If later evidence shows LSP is still low-value even after filtering, then making `lsp-pi` opt-in becomes the next pruning step.
