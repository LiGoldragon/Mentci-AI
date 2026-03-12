# Report: Ouranos LiteLLM MVP — Recovery Root Causes/Fixes, Dark Profile Activation, Runtime Truth, Tool-Use Forward Path

## Solar time
- `5919.12.23.13.56`

## Authoring model
- `openai-codex/gpt-5.2`

## Why this exists
Persist the currently verified recovery/runtime/research state for the `ouranos-litellm-mvp` path so follow-on work can start from a stable, reconciled truth.

---

## 1) Repaired root causes and fixes (nested purity + “syntax”)

### A. Nested flake purity violation (CriomOS local activation)
**Root cause:** Ouranos home activation initially imported a Nix file via an out-of-flake path escape (example shape: `../../../../nix/litellm-proxy.nix`). In the nested JJ repo `/home/li/git/Mentci-AI--dev/Components/CriomOS`, this breaks the repo-local Nix purity rule and can fail local activation.

**Fix:** Vendor/define the `litellm-proxy.nix` packaging surface inside the nested flake root and import it from within `Components/CriomOS/**` only (no upward path escapes).

### B. LiteLLM router config invalid / incomplete (provider metadata + fallback shape)
**Root cause:** The initial LiteLLM router YAML used bare model IDs without provider metadata and had fallback entries in an invalid dictionary shape.

**Fix:** Provide explicit provider routing for Prometheus upstreams (e.g. Ollama via `custom_llm_provider: ollama` + `model: ollama_chat/<id>`), and use a LiteLLM-compliant fallback structure.

---

## 2) Successful dark-profile activation
The intended Ouranos “dark” home profile was successfully activated previously (the activation outcome mattered more than the label): after activation, the user session gained the Home Manager-generated `~/.pi/agent/models.json` + gateway-oriented `settings.json`, plus user-level systemd units for the gateway/tunnel path (when enabled).

---

## 3) Current runtime truth (reconciled)

### A. Services on this machine (ouranos)
- `litellm-gateway.service` is **active (running)** (user systemd).
- `prometheus-ollama-tunnel.service` is **not present** in the current user systemd graph (so Prometheus upstream connectivity is not currently established through that path).

### B. Gateway truth: currently only cloud lanes are exposed
Direct evidence from the local OpenAI-compatible endpoint:
- `curl -m 3 http://127.0.0.1:11435/v1/models` returns only:
  - `cloud-reasoning`
  - `cloud-coder`
  - `cloud-fast`

Interpretation: the local gateway is up, but the Prometheus-backed lanes (`prometheus-*`) are not currently healthy/available in the running configuration.

### C. “Direct Prometheus DeepSeek” is still failing operationally
Runtime status from the latest verified operator context: Prometheus-hosted DeepSeek is still in a **load/timeout** failure mode when hit directly (i.e. the model is present in shards, but the serving lane is not returning timely responses yet).

### D. Model file truth: local merged DeepSeek exists; Qwen/Llama are still sharded
Local model directory snapshot:
- `/home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0.gguf` exists (merged output)
- DeepSeek shard inputs exist:
  - `DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf`
  - `DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf`
- Llama 3.3 70B shards exist (`-00001-of-00002`, `-00002-of-00002`)
- Qwen 2.5 72B shards exist (multi-part series)

Remote host truth (last reported): the remote Prometheus host still holds **shard sets only** (i.e. merged GGUF not consistently present remotely), so “it downloaded” is not the same as “the server can load it.”

---

## 4) Forward-path recommendations (external research) — tool-use testing for DeepSeek/Qwen/Llama

### Recommendation 1: Use an OpenAI-compatible server that *explicitly* supports tool calling
For `llama.cpp` / `llama-server`, follow the upstream function-calling guidance (template/tool-use support is a first-class concern; verify props/chat_template_tool_use).
- Source: https://github.com/ggml-org/llama.cpp/blob/master/docs/function-calling.md

### Recommendation 2: Qwen tool calling works best with “Hermes-style” templates/parsers
Qwen’s own documentation emphasizes template-driven function calling (and warns about stopword-based ReAct-style approaches for reasoning models).
- Source: https://qwen.readthedocs.io/en/latest/framework/function_call.html

### Recommendation 3: Llama 3.3 inherits Llama 3.1 prompt format and supports the same function-calling format as Llama 3.2
Use the official Llama prompt/tool calling conventions when testing tool calls (don’t invent an ad-hoc schema if the template already defines one).
- Source: https://www.llama.com/docs/model-cards-and-prompt-formats/llama3_3/

### Recommendation 4: DeepSeek tool calling may require template overrides depending on the served variant
The `llama.cpp` docs note DeepSeek R1 distills often work best with a tool-use template override; treat “model supports tool calls” as *runtime-template + server-feature* dependent, not as a simple model-name fact.
- Source: https://github.com/ggml-org/llama.cpp/blob/master/docs/function-calling.md

### Minimal tool-use test suite (practical)
Run the same 3-step harness across `deepseek`, `qwen`, `llama` lanes:
1. One trivial tool (pure function): `add(a:int,b:int)->int`
2. One I/O-ish tool with schema: `read_metric(query:string)->string`
3. One multi-tool / parallel call attempt (if supported)

Success criteria:
- the model emits *structured* tool calls (not JSON blobs in `content`)
- arguments validate against schema (no hallucinated fields)
- tool result is incorporated correctly in the follow-up assistant message

---

## 5) Recommended next steps (most leverage)

1. **Restore a working Prometheus upstream lane** for Ouranos:
   - either reinstate the SSH tunnel unit (if that’s still the chosen transport), or update router upstreams to a reachable endpoint.
   - verify `/v1/models` includes the `prometheus-*` aliases again.

2. **Decide the Prometheus serving stack for large models** (Ollama vs `llama.cpp`):
   - if `llama.cpp` is the target, wire `llama-server` and validate function-calling templates early.

3. **Finish GGUF merge story and make it reproducible**:
   - ensure the remote host has merged `.gguf` artifacts, not only shards.
   - automate the merge step in the provisioning/sync workflow (so “provisioned” implies “loadable”).

4. **Fix cloud fallback hygiene**:
   - the current gateway shows cloud lanes only, but logs indicate API-key/auth gaps for the cloud deployments; either provide keys securely or disable cloud fallbacks so failures are crisp.

5. **Run the minimal tool-use test suite** through the gateway for all three target families:
   - Qwen (tool calling baseline)
   - Llama 3.3 (tool calling baseline)
   - DeepSeek distill (template-override experiment)

If these steps succeed, we regain: (a) a stable local gateway, (b) a stable on-Prometheus inference primitive, and (c) an evidence-backed tool-use capability matrix for DeepSeek/Qwen/Llama under the project’s OpenAI-compatible gateway architecture.
