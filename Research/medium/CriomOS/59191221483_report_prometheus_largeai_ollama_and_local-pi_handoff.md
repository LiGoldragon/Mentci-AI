# Prometheus largeAI / Ollama / local pi handoff

## Summary
This session turned `prometheus` into a working local-AI node suitable for SSH + tmux + on-node `pi` usage.

## Live node state
- Host: `prometheus`
- IP: `192.168.0.17`
- Installed system boots from NVMe and is reachable over SSH.
- `ollama.service` is active and bound to `127.0.0.1:11434`.
- Local Ollama API is reachable at `http://127.0.0.1:11434/v1`.

## Installed local models
- `qwen2.5-coder:7b`
- `deepseek-r1:latest`

## Verified local model behavior
- `curl http://127.0.0.1:11434/v1/models` returned both models.
- `ollama run qwen2.5-coder:7b ...` succeeded.
- `ollama run deepseek-r1 ...` succeeded.

## largeAI architecture progress
Completed and checkpointed:
1. `largeAI` species added across CriomOS horizon/species/schema surfaces and mirrored Rust contracts.
2. `prometheus` reclassified in `maisiliym` as `largeAI`.
3. Strix Halo / EVO-X2 Linux shared-memory tuning added in CriomOS metal module.
4. Localhost-only Ollama service added for `largeAI` nodes with writable `/var/lib/ollama` state.
5. Durable DNS fallback added in CriomOS network config so nodes no longer depend only on dead localhost DNS.

## Important runtime details
### Ollama service
Configured for:
- `OLLAMA_HTTP_BIND=127.0.0.1:11434`
- `OLLAMA_HOME=/var/lib/ollama`
- `OLLAMA_MODELS=/var/lib/ollama/models`
- dedicated `ollama` user/group

### pi on prometheus
Installed for user `li` via user-level Nix profile.

Config written on host:
- `/home/li/.pi/agent/models.json`
- `/home/li/.pi/agent/settings.json`

Those point `pi` at the local Ollama OpenAI-compatible endpoint and default to:
- provider: `ollama`
- model: `qwen2.5-coder:7b`

## Verified pi usage
Working command as user `li`:
```bash
PI_SOURCE_STABLE_LINK="" pi -p "Explain 1+1" --provider ollama --model qwen2.5-coder:7b --no-skills --no-extensions --no-tools
```
This produced a valid model response through local Ollama.

## Known rough edges
1. `pi` wrapper currently expects `PI_SOURCE_STABLE_LINK` to exist because of `set -u` behavior.
   - Current workaround: `export PI_SOURCE_STABLE_LINK=""`
   - Good future fix: patch the wrapper/package so this variable is optional.
2. The local Qwen model does not support the "thinking" mode expected by some provider metadata.
   - Current fix on host: `reasoning: false` in `/home/li/.pi/agent/models.json`.
3. The earlier suspend debugging established that the system is currently up and usable, but the original suspend incident on this mini PC was not conclusively tied to a lid path (there is no lid). Avoid overfitting future fixes to lid-specific logic.

## Repo state at end of session
Parent repo:
- bookmark: `dev`
- `dev = dev@origin = 39381678`

Nested CriomOS repo recorded in parent:
- `Components/CriomOS -> 8521b70c5fb13d315c9d1f7b85d5436970faa3ad`

Working tree state:
- clean
- one empty JJ working copy above `dev`

## Most useful next actions
1. Fix the `pi` wrapper so `PI_SOURCE_STABLE_LINK=""` is no longer needed.
2. Decide whether to add a second `pi` profile/config preset for `deepseek-r1:latest` as a reasoning-heavy default.
3. If needed, encode the live host resolver override more deeply if future network manager/regeneration behavior still rewrites `/etc/resolv.conf` unexpectedly.
4. Run realistic coding tasks through local `pi` on `prometheus` and tune model defaults based on quality/speed.
