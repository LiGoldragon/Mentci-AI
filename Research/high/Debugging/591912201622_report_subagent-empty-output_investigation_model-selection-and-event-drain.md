# Report: Subagent Empty-Output Investigation — Model Selection and Event-Drain

## Intent
Diagnose why Pi subagents, especially `explore`, were returning `(no output)` even when the task tool reported success.

## Summary
The current evidence no longer supports a simple global concurrency claim like "one agent always fails." A minimal 3-agent batch using the `task` agent succeeded `3/3`, while a minimal 3-agent batch using the `explore` agent returned `(no output)` for all `3/3`.

Two concrete failure surfaces were identified:

1. **Print/JSON event-drain race in packaged Pi**
   - The subagent adapter only captures final text when it receives the child process `agent_end` event.
   - `AgentSession` processes events through an async queue.
   - Print/json mode previously did not wait for that queued event processing to drain before exit.
   - This can produce successful subprocess exits with empty captured output.

2. **Bad fuzzy model selection for the `explore` agent**
   - The `explore` agent uses a comma-separated model fallback string.
   - The task tool's `resolveModelPattern()` returns the raw pattern unchanged if it cannot obtain the available-model list.
   - With a polluted/stale model catalog, fuzzy terms like `flash` and `mini` are especially dangerous because they can match old, deprecated, or otherwise undesirable model entries.
   - The new user-supplied context strongly suggests recent Pi model-expansion work introduced too many models, including ones that no longer exist or should never be selected.

The strongest current evidence is that the `explore` lane is now failing primarily on **model-selection behavior**, while the print/json event-drain race is a **real secondary reliability bug** that can also cause empty captures.

## Reproduction Evidence

### Probe batches
- `task` agent, trivial one-line prompt, 3-way parallel batch: **3/3 succeeded with non-empty output**.
- `explore` agent, trivial one-line prompt, 3-way parallel batch: **3/3 completed with `(no output)`**.

### Model override probes for `explore`
- `model: default` → `Status: success - explore default-model probe`
- `model: haiku` → `Status: success - explore haiku probe`
- `model: flash` → `(no output)`
- `model: mini` → `(no output)`

These probes indicate the failure is not inherent to the `explore` role itself. The lane becomes healthy when forced onto a safe model selection path (`default`, `haiku`) and unhealthy on fuzzy `flash` / `mini` selections.

## Code Evidence

### 1. Subagent adapter writes `(no output)` on empty capture
- File: `/home/li/git/Mentci-AI--dev/result/lib/node_modules/pi/node_modules/@oh-my-pi/subagents/tools/index.ts`
- Behavior:
  - resolves persisted content via `result.stdout.trim() || result.stderr.trim() || '(no output)'`
  - therefore any missed final text becomes a hard `(no output)` artifact

### 2. Comma-separated model fallback is unsafe when available-model listing fails
- File: `/home/li/git/Mentci-AI--dev/result/lib/node_modules/pi/node_modules/@oh-my-pi/subagents/tools/index.ts`
- Function: `resolveModelPattern(pattern, availableModels?)`
- Behavior:
  - if `getAvailableModels()` returns an empty list, the function returns the raw pattern unchanged
  - that means a string like `claude-haiku-4-5, haiku, flash, mini` can be passed through literally instead of being resolved to a concrete model id

### 3. `explore` agent model and tools
- File: `/home/li/git/Mentci-AI--dev/.pi/agents/explore.md`
- Current relevant frontmatter after local fix:
  - `tools: read, grep, find, ls, bash`
  - `model: claude-haiku-4-5, haiku, flash, mini`

The `glob` tool declaration was stale and was corrected to `find`, but that alone does not explain the model-probe behavior above.

### 4. Model matching prefers partial matches and aliases
- File: `/home/li/git/Mentci-AI--dev/.pi/pi-source/dist/core/model-resolver.js`
- Important behavior:
  - fuzzy matching is allowed via `tryMatchModel()`
  - if multiple matches exist, aliases are preferred, then the lexicographically latest dated version
  - broad terms like `flash` and `mini` are therefore risky when the catalog contains stale or low-priority models

### 5. Project and user settings show divergent model preferences
- File: `/home/li/.pi/agent/settings.json`
  - default provider/model: `openai-codex` / `gpt-5.3-codex`
  - enabled models include several Codex entries, including `gpt-5.1-codex-mini`
- File: `/home/li/git/Mentci-AI--dev/.pi/settings.json`
  - default provider/model: `google` / `gemini-3-flash-preview`
  - also contains `"extensions": ["!**"]`, which remains a separate extension-loading hazard

### 6. Project-local packaging explicitly injected additional Google models
- File: `/home/li/git/Mentci-AI--dev/Components/nix/pi-dev.nix`
- The build patch injects additional Google model definitions directly into `packages/ai/src/models.generated.ts`, including:
  - `gemini-2.5-flash`
  - `gemini-2.5-pro`
  - `gemini-1.5-flash`
  - `gemini-1.5-pro`

This aligns with the user report that recent model-expansion work introduced too many models, including stale or undesirable ones.

## Implemented Mitigations

### Packaged runtime reliability patch
The packaged Pi runtime was patched in:
- `/home/li/git/Mentci-AI--dev/Components/nix/pi.nix`
- `/home/li/git/Mentci-AI--dev/Components/nix/pi-dev.nix`

Patch behavior:
- adds `waitForEventProcessing()` to `AgentSession`
- makes print/json mode await queued event processing before exit

Verification support added in:
- `/home/li/git/Mentci-AI--dev/Components/nix/pi_with_extensions_check.nix`

### Local `explore` tool declaration cleanup
- File: `/home/li/git/Mentci-AI--dev/.pi/agents/explore.md`
- Change:
  - replaced unsupported `glob` with supported `find`

## Verification
A bounded package check succeeded after the runtime patch:

```bash
cd /home/li/git/Mentci-AI--dev && system=$(nix eval --impure --raw --expr builtins.currentSystem) && nix build .#checks.${system}.piWithExtensions
```

Observed result:
- built successfully:
  - `pi-0.57.1.drv`
  - `pi-with-extensions-upstream.drv`
  - `pi-with-extensions-check.drv`

## Current Hypothesis
The current best explanation is:

- **Primary live failure:** `explore` default model selection is landing on bad fuzzy matches from an over-broad/stale model catalog, especially for terms like `flash` and `mini`.
- **Secondary reliability bug:** even when the underlying child run succeeds, the print/json mode race can still convert a valid run into `(no output)` by dropping the final `agent_end`-derived text capture.

## Recommended Next Checks
1. **Harden subagent model declarations**
   - Replace fuzzy fallback strings like `claude-haiku-4-5, haiku, flash, mini` with a single explicit known-good model or a narrower fallback set.
2. **Trim the model catalog**
   - Remove deprecated, non-existent, or never-should-be-used models from the injected Pi model set.
3. **Bias toward current high-quality aliases only**
   - Avoid generic fuzzy terms (`flash`, `mini`) in agent frontmatter unless the catalog is tightly curated.
4. **Fix project `.pi/settings.json` extension policy**
   - Remove the deny-all extension setting if it is still affecting wrapper/extension behavior.
5. **Re-run subagent probes in a fresh environment using the rebuilt package**
   - The current running harness may still be using the pre-patch runtime.

## Remaining Unknowns
- Whether `getAvailableModels()` is failing outright inside the subagent tool in some runs, or succeeding but returning an undesirable catalog.
- Which exact concrete model `flash` and `mini` are resolving to in the broken `explore` lane.
- Whether project `.pi/settings.json` extension denial is affecting subagent behavior in the current live harness, or only in some launch paths.
