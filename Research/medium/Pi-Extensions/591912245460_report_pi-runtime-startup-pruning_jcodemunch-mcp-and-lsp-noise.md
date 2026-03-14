# Pi runtime startup pruning: jcodemunch MCP inclusion and LSP noise findings

## Intent
Preserve bounded startup/runtime-composition findings for Pi so unused or noisy startup-context pieces can be removed deliberately.

## Main conclusions
1. `Components/nix/jcodemunch-mcp.nix` **is** currently part of the repo-local Pi runtime/startup composition.
2. `lsp-pi` **is** also wired into the packaged Pi runtime by default.
3. LSP-style noisy payload shaping is a real improvement target, but the best near-term leverage is likely **adapter/runtime-layer filtering**, not prompt-only filtering.

## Exact jcodemunch wiring chain
### Inclusion path
1. `Components/nix/default.nix`
   - imports `./jcodemunch-mcp.nix`
   - passes `jcodemunch_mcp` into `pi-with-extensions.nix`

2. `Components/nix/pi-with-extensions.nix`
   - prepends `${jcodemunch_mcp}/bin` into the generated `pi` wrapper `PATH`
   - also registers runtime extensions including:
     - `@aliou/pi-linkup`
     - `pi-mcp-adapter`
     - `lsp-pi`
     - `pi-subagents-adapter`

3. `Components/nix/pi_with_extensions_check.nix`
   - explicitly greps the final wrapper for `jcodemunch-mcp`
   - explicitly asserts packaged extension presence

## Conclusion on jcodemunch
`jcodemunch-mcp` is not hypothetical or dormant packaging only; it is actively included in the repo-local packaged Pi wrapper/startup path.

## LSP runtime finding
`lsp-pi` is also included by default in the packaged Pi runtime:
- `Components/nix/pi-with-extensions.nix` symlinks `lsp-pi` into packaged `node_modules`
- the wrapper passes `--extension .../node_modules/lsp-pi`
- `Components/nix/pi_with_extensions_check.nix` asserts the packaged runtime contains `lsp-pi/src/lsp.ts` and related nixd-facing content

So the answer to “are we using the lsp server?” is:
- **It is definitely loaded/wired in the repo-local packaged runtime.**
- Whether it is heavily used by operator workflows is a separate empirical question, but it is not absent.

## Noise / filtering observations
There are three realistic places to reduce noisy tool payloads:

### Option A — filter in `lsp-pi` itself
- best source-level fix
- reduces payload before it reaches Pi/tool adapters
- but requires changing the pinned external fork/package

### Option B — filter at runtime adapter/tool layer
- best near-term repo-local leverage
- can normalize LSP outputs before they become large prompt payloads
- preserves the ability to keep raw mode behind a verbose/debug flag

### Option C — filter only in prompts/instructions
- lowest implementation effort
- weakest actual token/noise control
- still sends noise into the model context

## Recommendation
Prefer **Option B first**:
- keep the repo-local change local
- add compact response shaping at the adapter/tool layer
- remove `_meta`-style or clearly non-intent-bearing fields by default
- preserve an opt-in verbose/debug path for full payloads

Then, if the shaping proves correct, upstream or mirror the same policy into the pinned `lsp-pi` fork.

## Other likely startup/context-pruning candidates surfaced
These are **candidates**, not yet confirmed removals:
- `pi-subagents-adapter`
- `fava_trails_mcp_server`
- `agentic_jujutsu`
- `litellm_proxy`
- alternate/non-default runtime lanes like `vtcode` / `pi_rust`

The strongest current confirmed pruning target from this pass is still:
- **`jcodemunch-mcp`**, because its inclusion in runtime startup was directly evidenced and the operator explicitly says it is unused and context-poisoning.

## Suggested implementation order
1. Remove `jcodemunch-mcp` from the packaged Pi runtime/startup path.
2. Update Nix checks that currently assert its presence.
3. Remove or relax any active mutable instructions that force `solar:` timestamps in user-visible responses.
4. Add runtime/tool-layer filtering for noisy LSP outputs instead of relying on prompt-only suppression.
5. Re-evaluate whether `lsp-pi` should remain always-on or become conditional after filtering lands.
