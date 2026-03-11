# Report: Ouranos Runtime State — LiteLLM Gateway and Prometheus Reachability

## Prompt
Investigate whether the current-user `pi` session on `ouranos` can access the Prometheus LLM service through the LiteLLM gateway, whether Prometheus has been updated and is running the expected services, whether the local user environment has been updated, and whether the user should currently be able to use the local LLM.

## Scope
Read-only runtime investigation. No edits, restarts, or activations were performed.

## Direct Runtime Post-Gates
The following bounded direct checks were run on the current machine:

```bash
hostname
readlink -f /home/li/.config/environment.d/10-home-manager.conf
ls -l /home/li/.pi/agent/models.json /home/li/.pi/agent/settings.json
curl -sS -m 3 http://127.0.0.1:11435/v1/models
systemctl --user status litellm-gateway --no-pager
curl -I -m 4 http://maisiliym.prometheus.tailnet:11434
getent hosts maisiliym.prometheus.tailnet
```

## Findings
### 1. This session is on `ouranos`
Direct evidence:
- `hostname` -> `ouranos`

### 2. The local user environment is at least Home-Manager-managed, but the new LiteLLM user-space payload is not active
Direct evidence:
- `/home/li/.config/environment.d/10-home-manager.conf` resolves to a Nix store path:
  - `/nix/store/3rikdznw1zlfvni5g4b402dcwnw55b8s-hm_environment.d10homemanager.conf`
- `~/.pi/agent/settings.json` exists
- `~/.pi/agent/models.json` does **not** exist

Interpretation:
- A Home Manager environment is loaded.
- The new Ouranos LiteLLM/pi client generation has **not** been activated into the current user environment, because the generated `models.json` expected by the new design is missing.

### 3. The local LiteLLM gateway is not installed/running in the current user session
Direct evidence:
- `systemctl --user status litellm-gateway` -> `Unit litellm-gateway.service could not be found.`
- `curl http://127.0.0.1:11435/v1/models` -> `curl: (7) Failed to connect to 127.0.0.1 port 11435`

Interpretation:
- The expected Home Manager user service has not been loaded into the active user session.
- Therefore the local OpenAI-compatible gateway endpoint is not available to `pi` right now.

### 4. The current `pi` config is still pointing at Codex/OpenAI, not the local LiteLLM gateway
Observed current local file contents from the investigation packet:
- `~/.pi/agent/settings.json` contains:
  - `defaultProvider: "openai-codex"`
  - `defaultModel: "gpt-5.3-codex"`

Interpretation:
- The current-user `pi` environment is still using the old provider settings.
- Even restarting `pi` right now would not switch to the local gateway, because the gateway-specific `models.json` is absent and the service is absent.

### 5. From `ouranos`, the Prometheus tailnet hostname is currently not resolvable
Direct evidence:
- `curl -I http://maisiliym.prometheus.tailnet:11434` -> `curl: (6) Could not resolve host: maisiliym.prometheus.tailnet`
- `getent hosts maisiliym.prometheus.tailnet` -> no output

Interpretation:
- Even if the LiteLLM gateway were started locally, the current `ouranos` runtime would not be able to reach the configured upstream Prometheus endpoint until tailnet/DNS reachability is fixed.

## Reconciled Conclusion
Two different truths are relevant here:

1. **Repo/document state:** Earlier work and preserved reports indicate that Prometheus was updated in a prior session and that the intended architecture/wiring was completed and validated in bounded local tests.
2. **Current live runtime state on `ouranos`:** The active user environment here does **not** yet have the new gateway profile/service/config loaded, and `ouranos` cannot currently resolve the configured Prometheus tailnet hostname.

So the practical answer is:
- **No, your current `pi` session on `ouranos` cannot use the Prometheus LLM through the LiteLLM gateway right now.**
- **No, simply restarting `pi` right now is not sufficient**, because the active user environment is still missing the new generated `models.json`, still points at Codex/OpenAI in `settings.json`, and the `litellm-gateway` user service is not present.
- **Prometheus may well have been updated earlier**, but from this live `ouranos` session the relevant upstream is not currently reachable by the configured tailnet hostname.

## What Must Be True Before It Works
1. The Ouranos Home Manager activation carrying the new LiteLLM module must be switched into the active user profile.
2. The `litellm-gateway` user service must exist in `systemd --user` and start successfully.
3. `~/.pi/agent/models.json` must exist and `settings.json` must point to the local gateway defaults.
4. `ouranos` must be able to resolve/reach `maisiliym.prometheus.tailnet:11434` (or the router config must be adjusted to a currently reachable upstream).
5. After those are true, the running `pi` process must be reloaded/restarted, because `pi` does not hot-bind changed agent config into an already-running session.

## Best Next Step
Run the intended Home Manager activation for `ouranos`, verify that `litellm-gateway` appears in `systemctl --user`, verify `curl http://127.0.0.1:11435/v1/models`, then restart or `/reload` `pi` and test one prompt through the `main` alias.
