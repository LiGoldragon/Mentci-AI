# Prometheus Qwen timeout investigation: Pi timeout surface vs upstream runtime behavior

## Intent
Determine whether the observed behavior:
- Pi request times out / returns no answer,
- while the Prometheus `llama-server` keeps running,

is mainly caused by:
1. a configurable Pi-side timeout setting, or
2. an upstream runtime / gateway bug or pathological long-running inference.

## Short answer
This does **not** currently look like “just change a Pi timeout setting.”

Key conclusions:
- The OpenAI-compatible provider path used by Pi does **not** set a short custom timeout in repo code.
- The underlying OpenAI SDK default timeout is about **10 minutes**, not ~20–30 seconds.
- Repo config surfaces for Prometheus/Pi do **not** expose a timeout knob for this lane today.
- The real runtime evidence shows:
  - `GET /v1/models` on Prometheus returns quickly,
  - actual inference requests to the heavy reasoning lane can block for **>60s** even for a trivial prompt,
  - after the client gives up, the upstream `llama-server` continues running and burning CPU.

So the strongest current diagnosis is:
- **upstream runtime/gateway behavior is the primary problem**,
- not a simple repo-configurable Pi timeout.

## What was verified

## 1. Pi live config is pointing at remote Prometheus LiteLLM, not localhost
The live Pi config on this machine resolves `prometheus` to:
- `http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1`

And `main-reasoning` resolves to the Qwen lane alias.

This matters because earlier local-loopback probes would not have been authoritative for the real path.

## 2. There is no obvious repo-configured Prometheus timeout knob
Inspected surfaces:
- `config/pi/prometheus-agent-settings.json`
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- `Components/CriomOS/nix/homeModule/min/default.nix`

Result:
- no explicit Prometheus/Pi request timeout setting was found for this path.
- there are routing/endpoint fields, but not a user-tunable request timeout for the provider lane.

## 3. Pi provider code does not impose a short timeout itself
Pi’s OpenAI-compatible provider path uses the OpenAI JS SDK.

Important evidence:
- provider code forwards `options.signal` for aborts
- provider code does **not** construct the OpenAI client with a custom short timeout
- the OpenAI SDK default timeout is about `600000 ms` (`10 minutes`)

Implication:
- absent some outer wrapper or different runtime layer, Pi itself is not obviously imposing a 20-second timeout in this provider path.

## 4. The remote Prometheus gateway is reachable for metadata, but inference is slow/sticky
Bounded runtime probes showed:
- `GET /v1/models` against the configured Prometheus endpoint returned quickly with HTTP 200
- `POST /v1/completions` to the reasoning lane blocked until a `60s` shell timeout
- a bounded `pi --provider prometheus --model main-reasoning -p 'Ping.'` attempt also did not return within the shorter bound used for the test

This means:
- the endpoint is reachable,
- the gateway is up,
- but the heavy inference request is the part that is not returning promptly.

## 5. Prometheus-side evidence shows the reasoning server keeps working after client timeout
Prometheus-side SSH snapshots during/after the test showed:
- LiteLLM active on `11434`
- the reasoning `llama-server` process present
- the reasoning process consuming very high CPU (hundreds of percent) after the client-side bounded test had already ended

Implication:
- once the request is accepted upstream, it keeps working even after the client has given up.
- this exactly matches the previously documented cancel/disconnect gap in the stack.

## Important nuance: do not overread the 20s test
A bounded shell command used `timeout 20s` around the Pi CLI probe for reproducibility.
That does **not** prove Pi has an internal 20s timeout.

What it proves is:
- the Qwen reasoning request did not finish within 20s,
- and a direct upstream inference request did not finish even within 60s,
- while the upstream process remained active afterward.

So the correct interpretation is:
- not “Pi definitely has a 20s timeout,”
- but “the heavy inference path is slow/sticky enough that any reasonable outer timeout will trip before the upstream request completes.”

## Best current diagnosis
The most likely stack explanation is:
1. Pi sends the request correctly to the configured Prometheus endpoint.
2. LiteLLM/gateway accepts it and forwards it.
3. `llama-server` on Prometheus starts working the request.
4. The client-side caller eventually gives up (bounded shell timeout in the test; maybe another timeout/harness condition in the user-observed path).
5. The upstream runtime keeps processing instead of stopping immediately.

So there are two distinct issues:
- **slow or pathological inference latency** on the Qwen lane,
- **missing prompt upstream cancellation/release semantics** after client timeout/disconnect.

## What this means for “timeout setting or bug?”
### Not the first answer:
- “just increase a Pi timeout setting”

Why not:
- no clear repo-configured timeout knob exists for this path,
- provider code defaults to a much larger SDK timeout already,
- direct upstream inference is slow/stuck enough that the real problem is upstream behavior.

### Stronger answer:
- this is primarily a **runtime/gateway behavior bug or performance path issue**, not merely a missing Pi config change.

## Most plausible technical candidates now
1. **Qwen reasoning lane genuinely takes too long for first token / tiny prompts** under current runtime conditions
   - perhaps due to model state, very large ctx settings, or general performance regression
2. **`llama-server` request continues after disconnect/timeout**
   - already consistent with prior research on `llama.cpp` + LiteLLM cancel gaps
3. **the request may be stuck/pathological rather than merely slow**
   - because a trivial `Ping.` still failed to return within 60 seconds

## Recommended next debugging steps
1. Capture Prometheus-side logs during one bounded request:
   - `journalctl -u prometheus-litellm`
   - `journalctl -u prometheus-llama-reasoning`
2. Run a longer direct completion probe (e.g. 300s) to distinguish:
   - “slow but eventually completes” vs
   - “stuck / pathological / never completes”
3. Measure first-token latency directly if possible.
4. Confirm whether the Qwen lane is already hot-loaded or repeatedly paying a load/warm path.
5. Keep the cancellation finding separate: even if timeouts are increased, upstream slot release still appears broken.

## Bottom line
- **Do we need to change a Pi timeout setting?**
  - Not as the first/main fix. No obvious repo-level timeout knob was found, and the provider path already defaults to a much larger SDK timeout.
- **Is there a bug / upstream issue?**
  - Yes, most likely. The current evidence points to the Prometheus reasoning inference path being excessively slow or stuck, and the upstream `llama-server` continuing to run after the client gives up.

## Evidence sources
### Local/live config
- `/home/li/.pi/agent/models.json`
- `/home/li/.pi/agent/settings.json`
- `/home/li/.pi/settings.json`
- `/home/li/git/Mentci-AI--dev/config/pi/prometheus-agent-settings.json`

### Repo config surfaces
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- `Components/CriomOS/nix/homeModule/min/default.nix`

### Pi/OpenAI provider code
- `.pi/pi-source/node_modules/@mariozechner/pi-ai/providers/openai-completions.js`
- `.pi/pi-source/node_modules/openai/client.js`

### Runtime tests
- direct `GET /v1/models`
- direct `POST /v1/completions`
- bounded `pi --provider prometheus --model main-reasoning -p 'Ping.'`
- Prometheus-side SSH process/service snapshots
