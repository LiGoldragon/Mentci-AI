# Prometheus LLM runtime status and Pi raw-Ygg handoff

## Intent
Preserve the current high-value operational context before session compaction.

## What was fixed
### 1. Ouranos local Nix was unblocked through CriomOS
The urgent `key is corrupt` failure came from live Ouranos emitting `secret-key-files = /etc/nix/preCriad` while the key file was empty/corrupt.

The fix that was actually deployed:
- remove the `secret-key-files = ...` emission from `Components/CriomOS/nix/mkCriomOS/nix.nix`
- redeploy **Ouranos** through CriomOS using `execute deploy-manifest` via `ssh root@localhost`

Verified result after deploy:
- `nix show-config | grep '^secret-key-files'` shows `secret-key-files =`
- exact CriomOS Nix builds work again without the workaround on Ouranos

### 2. Prometheus OS-level LLM services are live again
A major root cause was that Prometheus no longer had the intended OS-level LLM module active. The repo was repaired so Prometheus again gets:
- `prometheus-litellm` on `11434`
- `prometheus-llama-backup` on `11436`

### 3. Strix Halo / EVO-X2 startup stall was fixed
After debugging, the decisive fix for Prometheus was:
- remove stale user-level `prometheus-deepseek-70b` service on Prometheus
- run the OS-level `llama-server` with `--no-mmap`

This matches external Strix Halo / ROCm guidance: mmap-backed load paths can stall after offload on EVO-X2 / Strix Halo.

## Current Prometheus service state
Prometheus Ygg address:
- `202:68bc:1221:1b13:5397:2a56:4aea:d4a9`

Current working API surface:
- LiteLLM canonical: `http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1`
- direct llama backup: `http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11436`

Verified good on Prometheus:
- `11436 /health` returns `{"status":"ok"}`
- `11434 /v1/models` returns:
  - `main-deepseek`
  - `deepseek-r1-distill-llama-70b`
- `11434 /v1/chat/completions` returns successful completions

## Current DNS state
The remaining issue is **DNS-only** on Ouranos:
- `prometheus.maisiliym.criome` does **not** currently resolve on Ouranos
- direct Ygg IPv6 access works fine

Therefore:
- Prometheus LLM stack itself is working
- consumer hostname path is still broken

## Pi hookup state
### User-global runtime override
A local runtime override was installed at:
- `/home/li/.pi/agent/models.json`
- `/home/li/.pi/agent/settings.json`

Those files now point Pi at the raw Ygg Prometheus endpoint and only the working models:
- `prometheus/deepseek-r1-distill-llama-70b`
- `prometheus/main-deepseek`

### Repo-local Pi config
The repo-local Pi config that is actually used when running `pi` from `/home/li/git/Mentci-AI--dev` was also updated:
- `/home/li/git/Mentci-AI--dev/.pi/agent/models.json`
- `/home/li/git/Mentci-AI--dev/.pi/agent/settings.json`

Before that fix, repo-local Pi was still pointing at `127.0.0.1:11435` and produced:
- `400 Invalid model name ... deepseek-r1-distill-llama-70b`

After the fix:
- that model-name mismatch is gone
- direct backend curls succeed
- but `pi --print` still does not cleanly complete in-session (one earlier repro produced a 429 cooldown-style error; the latest smoke test timed out)

So the remaining Pi issue is now a **Pi-side runtime/request-path issue**, not a Prometheus LLM service issue.

## Important exact files changed in nested CriomOS
Main logical outcomes in `Components/CriomOS` included:
- `nix/mkCriomOS/nix.nix`
  - stop emitting `secret-key-files = ...`
- `nix/mkCriomOS/llm.nix`
  - restore OS-level Prometheus services
  - add LiteLLM restart trigger on config changes
  - add reduced startup flags
  - add `--no-mmap`
- `nix/mkCriomOS/default.nix`
  - include `llm.nix` for Prometheus
- `nix/homeModule/min/default.nix`
  - generate Prometheus router config coherently
- `nix/homeModule/min/litellm-router.yaml`
  - reduced to the honest one-model local surface
- `data/config/pi/prometheus-model-catalog.json`
  - reduced to the actual local model surface
- compatibility-first public-key work also landed around:
  - `capnp/criosphere.capnp`
  - `nix/mkCrioSphere/clustersModule.nix`
  - `nix/mkCrioZones/horizonOptions.nix`
  - `nix/mkCrioZones/mkHorizonModule.nix`

## VCS state
Nested `Components/CriomOS` was pushed successfully.
Verified:
- `dev == dev@origin` at commit `baf284bc`

That pushed commit intent is:
- `intent: Prometheus llama-server: disable mmap to avoid Strix Halo ROCm load stall`

## Operational commands worth preserving
### Restart Prometheus LLM services
```bash
ssh -6 root@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 'systemctl restart prometheus-llama-backup prometheus-litellm'
```

### Stop Prometheus LLM services immediately
Useful if the machine is hot/noisy and the model should not keep running.
```bash
ssh -6 root@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 'systemctl stop prometheus-litellm prometheus-llama-backup'
```

### Check Prometheus LLM health directly over Ygg
```bash
curl -g -6 'http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1/models'
curl -g -6 'http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11436/health'
```

### Minimal direct completion test
```bash
curl -g -6 -sS \
  -H 'Content-Type: application/json' \
  -d '{"model":"deepseek-r1-distill-llama-70b","messages":[{"role":"user","content":"ping"}],"max_tokens":1,"temperature":0}' \
  'http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1/chat/completions'
```

## Remaining follow-up lanes
1. **DNS repair on Ouranos**
   - `prometheus.maisiliym.criome` still does not resolve there
   - service works by raw Ygg IPv6 literal
2. **Pi CLI runtime bug**
   - direct API is working
   - Pi config mismatch was fixed
   - a remaining Pi-side timeout / cooldown-style issue still needs debugging
3. **Step 2 of Nix signing-key redesign**
   - proper generated/renamed signing-key files and public-key export path still remain to be completed after the urgent unbreak
