# Prometheus Phase 1 sanity lane: deploy results, home-Pi context limit, and dirty repo state

## Intent
Preserve the exact state of the Prometheus runtime repair lane before context compaction.

This report records:
- the successful Phase 1 sanity-lane deployment,
- the runtime result that proves the light-model stack is sane,
- the current blocker for the real home Pi test,
- the current dirty JJ state in the active repos,
- and the latest operator instructions that still need durable follow-through.

## New operator instructions captured in this turn
1. `jj-agent`-style agents should use timeouts and should block quickly rather than spending long periods in analysis-heavy behavior; their role is operational, not deep analysis.
2. When rebuilding the home environment:
   - use the **light** profile in daytime,
   - use the **dark** profile from sunset to sunrise,
   - location: **Malaga**.
3. Save the current state to a file immediately because context compaction is imminent.

## Most important current truth
> The Prometheus Phase 1 light-model sanity lane is now proven healthy at the raw HTTP/runtime layer. The remaining blocker for the real home Pi test is no longer runaway llama behavior on the light lane — it is that Pi’s actual prompt envelope exceeds the current 4096-token sanity-lane context budget. A follow-up 8192-token fix was prepared but is not yet committed/pushed/deployed in nested CriomOS.

## What was successfully accomplished in this turn

### 1. Phase 1 sanity-lane implementation was created and deployed
Nested CriomOS gained a store-backed light-model lane using:
- model: `Llama-3.2-1B-Instruct`
- quant: `Q4_K_M`
- artifact URL:
  `https://huggingface.co/hugging-quants/Llama-3.2-1B-Instruct-Q4_K_M-GGUF/resolve/main/llama-3.2-1b-instruct-q4_k_m.gguf`
- pinned hash:
  `1qrc2p1749qgbxybr5ja6i44rs834w9gskyg7kvsw4jfxhcr83hx`

Primary added/changed nested files:
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
- `Components/CriomOS/nix/homeModule/min/default.nix`
- `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`

Pushed nested CriomOS commit already on `dev`:
- `ecf3af24`
- `intent: add store-backed Prometheus sanity model lane`

Deployment succeeded via the manifest-driven CriomOS lane:
- manifest path:
  `/nix/store/4lc0zfdd0nbawv5snvx8hq02viapzqwn-criomos-deploy-maisiliym-prometheus.json`
- deploy result:
  `deployed prometheus via ygg:202:68bc:1221:1b13:5397:2a56:4aea:d4a9`

Important deploy footnote:
- `execute deploy-manifest` initially failed when run from the **wrong working directory** (top-level repo), because it tried to resolve `.os` from the wrong flake.
- It succeeded when run from inside `Components/CriomOS`.

### 2. The light-model runtime stack is healthy
After deploy, remote evidence showed:
- services active:
  - `prometheus-litellm.service`
  - `prometheus-llama-backup.service`
- listeners:
  - LiteLLM on `11434`
  - llama-server on `11436`
- store-backed model in use by llama-server:
  - `/nix/store/hcnvqndhma2v439j6vma8pxaqdy9ia3v-llama-3.2-1b-instruct-q4_k_m.gguf`
- direct llama model list showed:
  - `prometheus-main-sanity`
- LiteLLM model list (via IPv6 loopback) showed:
  - `main-sanity`
  - `llama-3.2-1b-instruct`

Direct raw probes succeeded:
- direct llama `11436` returned `Pong.`
- LiteLLM `11434` returned `Pong.`

Important nuance discovered:
- probing LiteLLM locally with `127.0.0.1:11434` failed because the service was bound on IPv6 (`[::]:11434`)
- local probe must use `[::1]:11434`

### 3. The light-model lane does **not** show the old runaway symptom
Observed post-test CPU snapshots on Prometheus after the successful light-model probes:
- about 10s after the LiteLLM `pong`:
  - `llama-server` ~`2.6%`
  - `.litellm-wrapped` ~`1.5%`
- about 45s later:
  - `llama-server` ~`0.8%`
  - `.litellm-wrapped` ~`4.0%`

Interpretation:
- the light-model stack settles back to idle,
- so the old runaway/no-spindown pathology is **not reproduced** on this light lane.

This is the strongest evidence so far that the earlier runtime problem is at least partly tied to the previous heavy lane and/or prompt/model pressure, not the whole Prometheus serving stack in the abstract.

## What was updated in the top-level repo
Top-level work was pushed for:
- repo-local Prometheus sanity-lane guidance/defaults
- updated skills/prompts for withdrawal-on-ambiguity and runtime safety
- removal of the stale mutable-authority `programming:` footer requirement from `Core/AGENTS.md`
- new research and implementation-plan artifacts

Pushed top-level commit(s) now on `dev` include:
- `ea85a859` — `intent: persist Prometheus sanity-lane guidance and repo-local defaults`
- `81af98a4` — `intent: align repo-local sanity model metadata with 8192 context`

However, see the dirty-state section below: the top-level repo is **not clean right now** despite the pushed follow-up commit.

## The real home-environment Pi result
### First apparent home test was misleading
A first so-called home test still inherited repo/devshell Pi environment variables:
- `PI_PACKAGE_DIR=/home/li/git/Mentci-AI--dev/.pi/pi-source`
- `PI_CODING_AGENT_DIR=/home/li/git/Mentci-AI--dev/.pi/agent`

That meant it was not a true home-environment Pi test.

### Actual home-managed Pi config was rebuilt and activated
Built attr:
- `.#crioZones.maisiliym.ouranos.hom.li.dark`

Built generation path:
- `/nix/store/wxa379gcnkykanl238b57hz4kvcjab8y-home-manager-generation`

Activated successfully via its `activate` script.

Verified new home Pi config contents:
- `/home/li/.pi/agent/models.json` now includes:
  - `llama-3.2-1b-instruct`
  - `main-sanity`
- `/home/li/.pi/agent/settings.json` now sets:
  - `defaultModel: main-sanity`

### True home-env Pi test result
Ran from `/home/li` with repo/devshell Pi overrides explicitly removed:
- `env -u PI_CODING_AGENT_DIR -u PI_PACKAGE_DIR timeout 20s pi --provider prometheus --model main-sanity --thinking off --no-session --no-tools -p 'Reply with exactly pong.'`

Result:
- not a timeout this time,
- instead a **real provider error**:
  - `400 litellm.BadRequestError: ... request (5106 tokens) exceeds the available context size (4096 tokens) ... Model Group=main-sanity`

Interpretation:
- the request path from the real home Pi environment **works**,
- Pi is reaching LiteLLM correctly,
- the blocker is now the **sanity lane context size**, not connectivity and not the old runaway runtime bug.

## Follow-up fix that was prepared but not finished
After proving the real blocker, a minimal follow-up fix was prepared:
- raise sanity-lane context budget from `4096` to `8192`
- keep `maxTokens = 2048`

Prepared nested changes currently modify:
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`

Prepared top-level change currently modifies:
- `config/pi/prometheus-agent-settings.json`

The top-level follow-up was pushed as `81af98a4`, but the nested follow-up did **not** finish because the `jj-agent` got stuck/interrupted.

## Current dirty JJ state at compaction time
### Nested `Components/CriomOS`
Current status:
- dirty
- uncommitted modified files:
  - `data/config/pi/prometheus-model-catalog.json`
  - `data/config/pi/prometheus-model-lock.json`

Current bounded state:
- working copy `@`: `29991f1e`
- parent `@-` / `dev`: `ecf3af24`
- `dev` still points to:
  - `intent: add store-backed Prometheus sanity model lane`

Meaning:
- the nested repo still needs a small follow-up commit/push for the `8192` context budget.

### Top-level `Mentci-AI--dev`
Current status:
- dirty
- uncommitted modified file:
  - `config/pi/prometheus-agent-settings.json`

Current bounded state:
- `dev`: `81af98a4`
  - `intent: align repo-local sanity model metadata with 8192 context`
- working copy still contains a modified copy of that file above the pushed lineage

Meaning:
- top-level history/push happened,
- but local working-copy hygiene is still incomplete and should be cleaned carefully later.

## Current Prometheus runtime state at compaction time
Most recent observed state after the true home-env Pi test:
- Prometheus services were restarted for the test and remained active
- the request no longer showed the old runaway behavior on the light lane
- the failure was a `400` context-size rejection
- delayed CPU check showed low CPU and no runaway

So at the moment of this handoff:
- Prometheus light sanity lane is **functionally healthy but too small**,
- not obviously runaway,
- and likely still active unless explicitly stopped after this report.

## Important caveat about home profile selection
The home rebuild done during this lane used:
- `ouranos.hom.li.dark`

But the operator later instructed:
- use **light** profile in daytime,
- use **dark** from sunset to sunrise,
- location Malaga.

Meaning:
- future home rebuilds should respect Malaga daylight conditions,
- and the next home rebuild in daytime should use the **light** profile instead of `dark`.

## Recommended next actions after compaction
1. **Finish the nested CriomOS follow-up cleanly**
   - commit and push the `8192` context-budget changes in:
     - `data/config/pi/prometheus-model-catalog.json`
     - `data/config/pi/prometheus-model-lock.json`

2. **Rebuild and redeploy Prometheus**
   - exact attrs only:
     - `.#crioZones.maisiliym.prometheus.os`
     - `.#crioZones.maisiliym.prometheus.deployManifest`
   - deploy from inside `Components/CriomOS`

3. **Rebuild the correct Ouranos home profile**
   - use `light` if current Malaga time is daytime
   - use `dark` only after sunset

4. **Re-run the true home-env Pi test**
   - from `/home/li`
   - with repo/devshell Pi overrides cleared:
     - `env -u PI_CODING_AGENT_DIR -u PI_PACKAGE_DIR ...`
   - target model:
     - `main-sanity`

5. **Only after the 8192 retest** decide whether to continue Phase 2 heavy-model comparison.

## Suggested future agent/process improvements
These were requested or strongly implied and remain worth encoding later:
- `jj-agent` should have bounded timeouts and should block quickly instead of hanging on operational tasks.
- Operational agents should prefer fast blocked/needs-input responses over long analysis.
- When testing Prometheus runtime, always check post-test heat and only force-stop if still hot after it should be idle.

## Compact handoff summary
If compacted again, the single most important state is:

> The Phase 1 store-backed light-model Prometheus sanity lane is deployed and healthy. Direct llama and LiteLLM both return `Pong.` and the node settles back to idle. The true home-environment Pi path now reaches the provider correctly, but it fails because Pi’s prompt envelope is about 5106 tokens and the current sanity lane is only 4096. A minimal 8192-token follow-up fix is prepared but still uncommitted in nested CriomOS due a stuck/interrupted `jj-agent`.
