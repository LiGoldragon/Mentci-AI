# CriomOS Prometheus Deploy Execution Packet

> **REQUIRED AGENT:** Prefer `/agent:criomos-deployer` for this lane.

**Goal:** Execute bounded Prometheus-serving refactors and deployments without broad Nix evaluation, local-path Maisiliym overrides, or target-node ambiguity.

**Scope:** Prometheus-only exact attr builds and manifest-driven deployment, plus cross-node verification from Ouranos.

**Non-goals:** No broad flake evaluation. No localhost activation for Prometheus. No local Maisiliym checkout overrides. No storage-path migration out of `/home/li/...` in this packet.

---

## Source and authority
- Maisiliym node/network truth is authoritative at the GitHub source `github:LiGoldragon/maisiliym`.
- CriomOS consumes that truth and generates deployment manifests.
- `execute deploy-manifest` is the canonical activation lane.

## Exact build commands
Use only exact attrs and GitHub-source override form when override is required:

```bash
nix build .#crioZones.maisiliym.prometheus.os \
  --override-input maisiliym github:LiGoldragon/maisiliym \
  --no-link --print-out-paths --refresh

nix build .#crioZones.maisiliym.prometheus.deployManifest \
  --override-input maisiliym github:LiGoldragon/maisiliym \
  --no-link --print-out-paths --refresh
```

If the pinned input is already the desired GitHub state, the override may be omitted. Local path overrides such as `/home/li/git/maisiliym` are forbidden for this lane.

## Exact deploy command
```bash
execute deploy-manifest \
  --manifest "$(nix build .#crioZones.maisiliym.prometheus.deployManifest \
    --override-input maisiliym github:LiGoldragon/maisiliym \
    --no-link --print-out-paths --refresh)" \
  --node prometheus \
  --override-input maisiliym github:LiGoldragon/maisiliym
```

## Targeting rules
- Node: `prometheus`
- Preferred transport: Yggdrasil
- Expected Ygg target: `202:68bc:1221:1b13:5397:2a56:4aea:d4a9`
- Do not use `--allow-localhost` for Prometheus in this packet.

## Verification from Ouranos
Return raw output for:

```bash
getent hosts prometheus.maisiliym.criome
```

Then probe both surfaces:

```bash
curl -sS http://prometheus.maisiliym.criome:11434/v1/models
curl -sS http://prometheus.maisiliym.criome:11436/health
```

If the direct llama endpoint exposes a different health/status path, report the exact path used and why.

## Success condition
- Prometheus system attr builds successfully.
- Prometheus deploy manifest builds successfully.
- Manifest-driven deploy reports Prometheus as the actual target.
- `prometheus.maisiliym.criome:11434` responds as canonical LiteLLM.
- `prometheus.maisiliym.criome:11436` responds as direct llama backup/debug.
