# Maisiliym Push, CriomOS Input/Home Cleanup, and Mentci-AI Devshell Fix Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Push the pending `maisiliym` truth changes to `dev`, update CriomOS to follow the correct `maisiliym` input and Pi/Ygg home configuration, and fix the Mentci-AI devshell failure so `nix develop ... --command pi` works again.

**Architecture:** Sequence work by dependency. First publish the pending source-of-truth change in `maisiliym`. Then make CriomOS consume `maisiliym/dev` and preserve the Pi raw-Ygg routing/home cleanup declaratively. Finally fix the top-level devshell failure in `Components/nix/jail.nix` so the repo shell can evaluate again against the corrected inputs. Verify each repo independently before any bookmark move/push.

**Tech Stack:** JJ, Nix flakes, Home Manager, CriomOS, Mentci-AI dev shell, execute deploy-manifest, Pi.

---

### Task 1: Push `maisiliym` truth to `dev`

**TDD scenario:** Modifying tested code — use bounded status + exact file verification before/after.

**Files:**
- Modify: `/home/li/git/maisiliym/datom.nix`

**Step 1: Verify current bounded status**

Run:
```bash
cd /home/li/git/maisiliym && jj status && jj diff --summary && jj bookmark list dev
```
Expected: dirty working copy with `M datom.nix` on top of `dev`.

**Step 2: Verify exact `ouranos` and compatibility truth in `datom.nix`**

Read the `ouranos` stanza and confirm:
- `preCriomes.nixSigningPublicKey`
- `preCriomes.yggdrasil.address = "201:6de1:5500:7cac:2db9:759e:42d2:fb1d"`
- `preCriomes.yggdrasil.preCriome = "6487aabfe0d4f491a2986f4b41388ebcbd3c69a654f32feb9b1fba351bbd590a"`

**Step 3: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- commit the dirty `datom.nix` change with an `intent:` message,
- move `dev` to the new non-empty revision,
- push `dev`,
- verify `dev == dev@origin`.

**Step 4: Verify pushed state**

Run bounded verification:
```bash
cd /home/li/git/maisiliym && jj status && jj bookmark list dev
```
Expected: clean working copy; `dev` advanced and aligned.

### Task 2: Update CriomOS to consume `maisiliym/dev` and preserve Pi raw-Ygg home configuration

**TDD scenario:** Modifying tested code — verify exact failing/desired behavior with bounded builds before and after.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/flake.nix`
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/flake.lock`
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix`

**Step 1: Verify current bounded status**

Run:
```bash
cd /home/li/git/Mentci-AI--dev/Components/CriomOS && jj status && jj diff --summary && jj bookmark list dev
```
Expected: dirty tree with home-module/catalog changes; `flake.nix` still on `maisiliym/prometheus-node` before edit.

**Step 2: Change the flake input to `maisiliym/dev`**

Edit in `flake.nix`:
```nix
maisiliym.url = "github:LiGoldragon/maisiliym/dev";
```

**Step 3: Update the lock for only that input**

Run:
```bash
cd /home/li/git/Mentci-AI--dev/Components/CriomOS && nix flake lock --update-input maisiliym
```
Expected: `flake.lock` updates only for `maisiliym`.

**Step 4: Verify exact attrs still build**

Run:
```bash
cd /home/li/git/Mentci-AI--dev/Components/CriomOS && \
  nix build .#crioZones.maisiliym.ouranos.hom.li.dark --override-input maisiliym /home/li/git/maisiliym --no-link --print-out-paths --refresh && \
  nix build .#crioZones.maisiliym.ouranos.os --override-input maisiliym /home/li/git/maisiliym --no-link --print-out-paths --refresh
```
Expected: both attrs build successfully.

**Step 5: Verify built home generation does not contain the legacy gateway**

Inspect the built home generation output for absence of:
- `127.0.0.1:11435`
- `litellm-gateway`
- `.config/litellm-router.yaml`

**Step 6: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- commit the CriomOS changes with an `intent:` message,
- move `dev` to the new non-empty revision,
- push `dev`,
- verify `dev == dev@origin`.

### Task 3: Fix top-level Mentci-AI devshell evaluation failure

**TDD scenario:** Modifying tested code — use the exact `nix develop` failure as the regression and verify it goes green after the minimal patch.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Components/nix/jail.nix`
- Modify: `/home/li/git/Mentci-AI--dev/config/pi/prometheus-agent-settings.json`
- Create/Keep: `/home/li/git/Mentci-AI--dev/Research/medium/CriomOS/591912242054_report_prometheus-llm_runtime-status_and_pi-ygg-handoff.md`

**Step 1: Reproduce the devshell failure before editing**

Run:
```bash
cd /home/li/git/Mentci-AI--dev && nix develop . --command bash -lc 'true'
```
Expected: fail with `expected a set but found a string` at `Components/nix/jail.nix:12`.

**Step 2: Apply the minimal fix in `Components/nix/jail.nix`**

Change `sourcePath` inside `mkInput` to tolerate string/path inputs:
```nix
sourcePath = "${if builtins.isAttrs input && input ? outPath then input.outPath else input}";
```

**Step 3: Keep the top-level Pi devshell config aligned**

Verify `config/pi/prometheus-agent-settings.json` points to:
```json
"baseUrl": "http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1"
```
and only advertises models actually served by Prometheus LiteLLM.

**Step 4: Re-run the devshell verification**

Run:
```bash
cd /home/li/git/Mentci-AI--dev && nix develop . --command bash -lc 'which pi && echo --- && pi --help | head -n 5'
```
Expected: shell evaluates successfully, `which pi` resolves, help header prints.

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- commit the top-level Mentci-AI changes with an `intent:` message,
- move `dev` to the new non-empty revision,
- push `dev`,
- verify `dev == dev@origin`.

### Task 4: Final verification packet

**TDD scenario:** Trivial verification / evidence consolidation.

**Files:**
- No source edits required.

**Step 1: Verify all three repos are clean**

Run bounded status in:
- `/home/li/git/maisiliym`
- `/home/li/git/Mentci-AI--dev/Components/CriomOS`
- `/home/li/git/Mentci-AI--dev`

Expected: clean working copies, no accidental dirty state.

**Step 2: Verify bookmark alignment evidence**

Run bounded bookmark checks in all three repos:
```bash
jj bookmark list dev
```
Expected: `dev` exists and reflects the just-pushed revisions.

**Step 3: Verify the exact user-facing devshell symptom is fixed**

Run:
```bash
nix develop github:LiGoldragon/Mentci-AI/dev --refresh --command bash -lc 'which pi && pi --help | head -n 3'
```
Expected: succeeds without the previous `jailConfig` derivation error.

**Step 4: Request code review**

Dispatch a reviewer for the combined diff ranges before claiming completion.
