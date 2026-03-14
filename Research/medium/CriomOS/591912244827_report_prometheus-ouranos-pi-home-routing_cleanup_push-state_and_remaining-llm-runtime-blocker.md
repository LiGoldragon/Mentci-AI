# Prometheus → Ouranos Pi home routing cleanup, push state, and remaining LLM runtime blocker

## Intent
Preserve the latest high-value context before compaction for the lane: **Prometheus should eventually host a working LLM stack that works correctly from the Ouranos Pi home environment**.

This report supersedes and extends:
- `Research/medium/CriomOS/591912242054_report_prometheus-llm_runtime-status_and_pi-ygg-handoff.md`

## Scope of this update
This update covers the later work after the first handoff report:
1. proving Pi was no longer supposed to use the old local Ouranos LiteLLM gateway,
2. making that routing change declarative in CriomOS/Home Manager,
3. activating the correct Home Manager generation on Ouranos,
4. fixing the top-level Mentci-AI devshell crash,
5. pushing the related repos/bookmarks,
6. preserving the still-unfixed backend runtime pathology.

## Important current summary
### What is now correct
- **Ouranos Pi home config is declaratively pointed at raw Prometheus Ygg LiteLLM `11434`.**
- **The old local Ouranos LiteLLM gateway on `127.0.0.1:11435` has been removed from the active Home Manager generation.**
- **The top-level Mentci-AI devshell no longer crashes on the earlier `expected a set but found a string` issue, nor the later Nix `ref && rev` assertion path.**
- **Maisiliym, nested CriomOS, and top-level Mentci-AI changes were pushed in this lane.**

### What is still not correct
- **Pi still does not pass the real success criterion**:
  - `pi --provider prometheus --model main-deepseek --thinking off --no-session --no-tools -p 'Reply with exactly pong.'`
  - still times out instead of returning a quick `pong`.
- **Prometheus llama.cpp runtime still has the critical operational bug**:
  - after requests, it can remain hot / continue spinning / fail to spin down cleanly,
  - `systemctl stop` can hang,
  - it may need forceful shutdown.

So the lane is now in a better and cleaner configuration state, but the **remaining blocker is runtime behavior of the actual model-serving stack**, not routing confusion.

---

## Detailed state

## 1. Pi routing truth: current active lane is raw Prometheus Ygg, not local `127.0.0.1:11435`
This was verified explicitly during the session.

Repo/devshell Pi path uses:
- `PI_CODING_AGENT_DIR=/home/li/git/Mentci-AI--dev/.pi/agent`
- `PI_PACKAGE_DIR=/home/li/git/Mentci-AI--dev/.pi/pi-source`

The active Pi provider config now points to:
- `http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1`

This is true both for:
- the repo/devshell Pi lane, and
- the declarative Ouranos home-managed Pi config under `~/.pi/agent/models.json`.

## 2. Declarative CriomOS/Home Manager cleanup was completed
### The bug
The old local Ouranos user service still created:
- local LiteLLM gateway on `127.0.0.1:11435`
- `~/.config/litellm-router.yaml`
- Home-managed `~/.pi/agent/models.json` originally targeting `127.0.0.1:11435`

### Exact declarative source that was fixed
Primary file:
- `Components/CriomOS/nix/homeModule/min/default.nix`

Key changes made in the lane:
- removed the Ouranos-local Pi gateway creation path,
- removed writing `~/.config/litellm-router.yaml` for Ouranos,
- removed the Ouranos user `litellm-gateway` service path,
- changed generated Pi `models.json` to use catalog-driven canonical endpoint,
- added a Home Manager activation cleanup step to stop/remove the legacy local gateway artifacts.

Supporting catalog change:
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`

Catalog canonical endpoints were changed to raw Prometheus Ygg:
- canonical: `http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1`
- backup: `http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11436/v1`

### Important activation discovery
A major confusion point was that **deploying the Ouranos OS attr was not enough**.
The stale `11435` listener lived in the **Home Manager generation for user `li`**, not just the OS path.

The correct activation that finally removed the listener was the exact home attr:
- `.#crioZones.maisiliym.ouranos.hom.li.dark`

Activation succeeded only after moving aside two manually-created runtime override files that were blocking Home Manager’s link step:
- `/home/li/.pi/agent/settings.json` → `/home/li/.pi/agent/settings.json.pre-hm-20260314004327`
- `/home/li/.pi/agent/models.json` → `/home/li/.pi/agent/models.json.pre-hm-20260314004327`

### Verified post-activation result
After switching the correct Home Manager generation:
- `systemctl --user status litellm-gateway` → unit not found
- `ss -lntp | grep 11435` → no output
- `ls -l /home/li/.config/litellm-router.yaml` → no such file
- `/home/li/.pi/agent/models.json` base URL → raw Prometheus Ygg `11434`

This is the key configuration cleanup milestone for the Ouranos Pi home environment.

---

## 3. Prometheus runtime state: healthy enough to answer, still broken operationally
### What is true on the service plane
Prometheus raw endpoints can respond:
- LiteLLM canonical on `11434`
- direct llama.cpp on `11436`

Observed model surface on raw Prometheus LiteLLM:
- `main-deepseek`
- `deepseek-r1-distill-llama-70b`

Observed direct llama.cpp model id on `11436`:
- `prometheus-main-deepseek`

### But the main blocker remains runtime behavior
Even after a clean restart, very small requests can return quickly, but the backend still shows the bad symptom:
- llama.cpp CPU remains high after requests,
- it does not spin down when idle the way it should,
- `systemctl stop` can hang,
- the machine audibly continues working.

This matches the strongest current runtime hypothesis for the lane:
- **llama.cpp / streaming cancellation / slot cleanup is still not behaving correctly**, so server-side work can continue even after the client side is effectively done or gone.

This is the remaining practical blocker for making Pi feel correct from Ouranos.

### Important last known forced-off state
At the end of the latest runtime interaction, the Prometheus LLM stack was forced off and verified down:
- `systemctl is-active prometheus-litellm.service prometheus-llama-backup.service` → `failed` / `failed`
- no listeners on `:11434` or `:11436`
- no matching `llama-server` / LiteLLM serving processes

So if the operator currently hears the machine calm, that is expected.

---

## 4. Pi success criterion is still unmet
The user-defined success criterion for this lane is strict:
- a Pi test is failure until it returns a quick `pong`

Latest exact required test still fails:
```bash
cd /home/li/git/Mentci-AI--dev && timeout 15s pi --provider prometheus --model main-deepseek --thinking off --no-session --no-tools -p 'Reply with exactly pong.'
```
Result:
- exit `124`
- no output
- no quick `pong`

Therefore the lane is **not complete**.

What is complete is the routing/home truth cleanup.
What is not complete is the runtime-serving behavior.

---

## 5. Devshell fix state
The top-level repo had two distinct devshell blockers during the lane.

### First blocker fixed
Earlier `nix develop` crash:
- `expected a set but found a string`
- surface: `Components/nix/jail.nix`

Root cause:
- `sourcePath = "${input.outPath}";` assumed all inputs were attrsets with `.outPath`
- some sources were plain string store paths

Fix made:
- tolerate string/path inputs in `Components/nix/jail.nix`

### Second blocker fixed
After that, `nix develop` hit a deeper Nix assertion:
- `Assertion '!(ref && rev)' failed in ... GitArchiveInputScheme::toURL`

Root cause found:
- multiple `flake.lock` github nodes carried **both** `ref` and `rev`
- `chronos-src` was the first one proven to trip the crash path

Fix made:
- normalized top-level `flake.lock` to remove `locked.ref` where `locked.rev` already exists for affected github nodes

### Verification that now succeeds
The key repro/fix verification now works:
```bash
cd /home/li/git/Mentci-AI--dev && nix develop . --command bash -lc 'which pi && echo --- && pi --help | head -n 5'
```
Observed result:
- shell evaluates successfully
- `which pi` returns the wrapped Pi binary
- help text prints

This is an important completed milestone.

---

## 6. Push / bookmark state from this lane
### Maisiliym
`maisiliym` pending `datom.nix` truth was finalized and pushed to `dev`.

Important current pushed commit:
- `dev` points to commit id `ed1a3a91`
- intent: `finalize ouranos yggdrasil and signing compatibility truth`

### Nested CriomOS
CriomOS changes for:
- tracking `maisiliym/dev`
- updating `flake.lock`
- removing the old Ouranos local gateway path
- pointing Pi home config at raw Prometheus Ygg `11434`

were finalized and pushed to `dev`.

Important current pushed commit:
- `dev` points to commit id `7d28e217`
- intent: `track maisiliym dev and remove ouranos local pi gateway`

### Top-level Mentci-AI
Top-level changes for:
- devshell repair
- raw Prometheus Pi settings
- runtime handoff artifacts
- subagent default model / fail-closed instructions

were recovered from a failed JJ split attempt, finalized, and pushed to `dev`.

Important current pushed commit:
- `dev` points to commit id `7bb9819c`
- intent: `fix devshell evaluation and persist safer subagent defaults`

## Important caution about top-level JJ state
The top-level repo currently still has local empty working-copy lineage above the pushed `dev` line:
- `@` empty
- `@-` empty described commit `intent: default subagents to gpt-5-mini and fail closed on unclear instructions`

This was left during the recovery path and **was not cleaned up in this lane** because the operator explicitly asked **not** to do the subrepo cleanup sweep right now and wanted context compacted.

So:
- pushed `dev` state is the important preserved result,
- but the local top-level worktree/history still needs future hygiene cleanup.

Do **not** mistake the current pushed `dev` success for “all repo hygiene is complete.” It is not.

---

## 7. New persistent subagent defaults in repo filesystem
A later user instruction required changing all subagents to a safer default model and fail-closed behavior.

This was persisted in the repo filesystem.

### New default model
Repo-local agents now default to:
- `openai/gpt-5-mini`

### New fail-closed rule
A reusable prompt file was added:
- `.pi/prompts/stop-if-unclear.md`

Repo-local agent definitions were updated to explicitly include:
- stop if the instructions are unclear
- return `Status: blocked - instructions unclear` instead of guessing

This was done specifically to reduce the chance of low-intelligence subagents making a mess.

---

## 8. Current high-value next steps after compaction
### A. Primary unresolved blocker
Fix the **Prometheus runtime spin-down / cancellation / stuck-generation behavior**.

This is now the real blocker behind the missing quick `pong`.

### B. Once runtime is constrained
Re-test the true success criterion:
```bash
timeout 15s pi --provider prometheus --model main-deepseek --thinking off --no-session --no-tools -p 'Reply with exactly pong.'
```
Required outcome:
- quick `pong`
- no continuing backend spin afterward

### C. Future hygiene lane (explicitly deferred for now)
The user explicitly said **do not clean up all the subrepos right now**.
So cleanup of:
- stray empty JJ commits / local worktree alignment,
- broader component sweep,
- generalized repo hygiene,

was intentionally deferred.

---

## 9. Exact operational commands worth preserving
### Check current Pi home config on Ouranos
```bash
grep -n 'baseUrl' /home/li/.pi/agent/models.json
```
Expected current value:
```text
http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1
```

### Check that the old local gateway is gone
```bash
ss -lntp | grep 11435 || true
systemctl --user status litellm-gateway || true
ls -l /home/li/.config/litellm-router.yaml || true
```
Expected current state:
- no `11435` listener
- no `litellm-gateway` unit
- no router file

### Re-test the true Pi success criterion
```bash
cd /home/li/git/Mentci-AI--dev && timeout 15s pi --provider prometheus --model main-deepseek --thinking off --no-session --no-tools -p 'Reply with exactly pong.'
```

### Force Prometheus LLM stack off if it starts spinning again
```bash
ssh -6 root@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 'systemctl stop prometheus-litellm.service prometheus-llama-backup.service || true; systemctl kill prometheus-litellm.service prometheus-llama-backup.service || true; pkill -af "llama-server|litellm|.litellm-wrapped" || true'
```

### Verify Prometheus stack is actually off
```bash
ssh -6 root@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 'systemctl is-active prometheus-litellm.service prometheus-llama-backup.service || true; ss -lntp | egrep ":11434|:11436" || true; pgrep -af "llama-server|litellm|.litellm-wrapped" || true'
```

---

## 10. Compact handoff summary
If this session is compacted again, the single most important truth is:

> The configuration/routing cleanup is now substantially correct: Ouranos Pi home config is declaratively pointed at raw Prometheus Ygg LiteLLM `11434`, and the stale local `127.0.0.1:11435` gateway path has been removed from the active Home Manager generation. The remaining blocker is no longer routing confusion — it is the Prometheus llama.cpp/LiteLLM runtime not spinning down correctly and still failing the quick `pong` criterion.
