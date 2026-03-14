# Pi startup surface, LSP optionalization, and home settings override

## Intent
Preserve the current operational diagnosis of Pi startup-context bloat and the smallest repo-driven surfaces for reducing it without ad-hoc host mutation.

## Terminology
The phrase the operator was reaching for is best rendered as:
- **Pi startup surface**
- or more specifically: **always-loaded startup extension set**

This is better than just "context" because the issue is not only prompt tokens. It is the combination of:
- extensions auto-loaded on every Pi startup,
- model registry/settings merge behavior,
- and project-level settings files that can override the global agent state depending on current working directory.

## Current live truth
Bounded reproduction from Ouranos established:
- raw Prometheus LiteLLM over the configured Ygg IPv6 endpoint works and returns `Pong.`
- Pi from both `/tmp` and `/home/li` reaches Prometheus
- the actual runtime blocker remains the live `4096` context budget on the serving side
- running Pi from `/home/li` also adds stale model warnings because `~/.pi/settings.json` is being treated as a project settings file and merged over the good global agent settings

## Exact config-precedence conclusion
Pi reads:
- global settings from `~/.pi/agent/settings.json`
- project settings from `<cwd>/.pi/settings.json`

So when Pi is run from `/home/li`, the file:
- `/home/li/.pi/settings.json`
acts as the project override.

This explains why:
- `~/.pi/agent/settings.json` and `~/.pi/agent/models.json` can correctly point to `main-sanity`
- while Pi still emits stale warnings from the old model patterns stored in `~/.pi/settings.json`

## Declarative ownership conclusion
Bounded repo inspection found:
- current CriomOS home generation declaratively writes:
  - `~/.pi/agent/settings.json`
- current dev-shell wiring declaratively symlinks repo-local:
  - `.pi/agent/settings.json`
- **no current declarative source in this repo writes `~/.pi/settings.json`**

Therefore the top-level home file is most likely lingering unmanaged state unless CriomOS is extended to own it too.

## LSP / startup-surface conclusion
`lsp-pi` is still always auto-loaded today.

Evidence:
- `Components/nix/pi-with-extensions.nix` always passes:
  - `--extension "''${PI_PACKAGE_DIR}/node_modules/lsp-pi"`
- the same file also symlinks `lsp-pi` into runtime `node_modules`
- `Components/nix/pi_with_extensions_check.nix` explicitly asserts `lsp-pi` presence and wrapper references

So the current Pi startup surface still includes at least:
- `@aliou/pi-linkup`
- `pi-mcp-adapter`
- `lsp-pi`
- `pi-subagents-adapter`

## Smallest optionalization design
The smallest repo-driven design to make LSP optional is:

1. remove the auto-load flag from `Components/nix/pi-with-extensions.nix`
   - stop passing `--extension .../lsp-pi` in the default Pi launcher
2. optionally keep `lsp-pi` packaged but not auto-loaded
   - this permits subagent-specific or explicit opt-in loading later
3. update `Components/nix/pi_with_extensions_check.nix`
   - remove/adjust checks that assume `lsp-pi` is always referenced by `bin/pi`

This is the minimal cut because it changes the single authoritative startup-loading point without requiring a larger redesign first.

## Recommended declarative home fix
Do **not** repair `~/.pi/settings.json` by ad-hoc symlink or manual host mutation.

The repo-driven fix should be in the CriomOS home environment generation:
- extend the same home-generation block that currently writes `~/.pi/agent/settings.json`
- also write `~/.pi/settings.json`
- make it intentionally match the desired runtime settings for home Pi invocation from `/home/li`

This keeps all mutation inside CriomOS home env, per operator correction.

## Ordered recommendation
1. fix the Prometheus-side 8192 activation gap so Pi can actually fit its prompt envelope
2. in the same CriomOS home lane, declaratively own `~/.pi/settings.json`
3. after the model path is working, make `lsp-pi` non-default / opt-in so startup surface shrinks

## Why order matters
The operator wants a much larger model test now.

Therefore:
- startup-surface cleanup is important, but it is not the first blocker to getting a real larger-model run
- the first blocker is still the live serving/context state on Prometheus
- the second blocker is the stale home override noise from `~/.pi/settings.json`
- `lsp-pi` optionalization is the right next cleanup once the larger-model lane is usable
