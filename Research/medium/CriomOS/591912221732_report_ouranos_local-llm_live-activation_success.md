# Report: Ouranos Local LLM Live Activation Success

## Prompt
Implement the fix so the current-user `pi` session on `ouranos` can use the local LiteLLM gateway to reach the Prometheus-hosted Ollama models.

## Summary
The local-LLM path on `ouranos` is now live.

Implemented/fixed:
1. Added a user-space SSH tunnel service on `ouranos` that forwards local `127.0.0.1:21434` to remote `127.0.0.1:11434` on Prometheus (`li@192.168.0.17`).
2. Repointed the LiteLLM router from the dead tailnet hostname to that local forwarded port.
3. Fixed a pre-existing nested CriomOS flake packaging bug by making `litellm-proxy.nix` local to the CriomOS flake.
4. Fixed the tunnel service auth failure by pinning `SSH_AUTH_SOCK` to the gpg-agent socket actually holding the working SSH key.
5. Activated the local `ouranos` Home Manager profile successfully.

## Files Changed
Nested CriomOS repo:
- `Components/CriomOS/nix/homeModule/min/default.nix`
- `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`
- `Components/CriomOS/nix/litellm-proxy.nix`

## Root Causes
### Root cause 1: inactive local profile
The active `ouranos` user profile predated the LiteLLM work, so the local user session had no:
- `litellm-gateway` user service
- `~/.pi/agent/models.json`
- gateway-oriented `settings.json`

### Root cause 2: dead tailnet-only upstream assumption
`maisiliym.prometheus.tailnet` was not resolvable from `ouranos`, so the original router upstream could never work in the current runtime environment.

### Root cause 3: local activation build bug
Local activation initially failed because:
- `Components/CriomOS/nix/homeModule/min/default.nix` imported `../../../../nix/litellm-proxy.nix`
- that escapes the nested CriomOS flake root and broke local activation builds

### Root cause 4: systemd user service auth mismatch
The tunnel service initially failed with:
- `Permission denied (publickey,keyboard-interactive)`

Cause:
- interactive shell used `/run/user/1001/gnupg/S.gpg-agent.ssh`
- systemd user environment exposed `/run/user/1001/gcr/ssh`
- the gcr socket had no identities

## Live Verification Evidence
### Services
Direct post-gates on `ouranos` showed:
- `prometheus-ollama-tunnel.service` active/running
- `litellm-gateway.service` active/running

### Pi config
Direct post-gates showed:
- `/home/li/.pi/agent/models.json` symlinked from the active Home Manager generation
- `/home/li/.pi/agent/settings.json` symlinked from the active Home Manager generation
- current `settings.json` includes:
  - `defaultProvider = "ouranos-lite-gateway"`
  - `defaultModel = "main"`
  - enabled models:
    - `ouranos-lite-gateway/main`
    - `ouranos-lite-gateway/subagent`
    - `ouranos-lite-gateway/fast`

### LiteLLM gateway
Direct HTTP probe:
- `http://127.0.0.1:11435/v1/models`
returned model ids including:
- `main`
- `subagent`
- `fast`
- `prometheus-deepseek`
- `prometheus-qwen`
- `cloud-reasoning`
- `cloud-coder`
- `cloud-fast`

### Prometheus Ollama through tunnel
Direct HTTP probe:
- `http://127.0.0.1:21434/api/tags`
returned:
- `deepseek-r1:latest`
- `qwen2.5-coder:7b`

## Practical Result
A fresh or reloaded `pi` session on `ouranos` should now be able to use the local LiteLLM gateway and route to the Prometheus-hosted local models through the SSH tunnel.

## Remaining Note
An already-running `pi` session that started before the profile activation may still be stale. Reload or restart that session so it re-reads the new `~/.pi/agent/models.json` and `settings.json`.
