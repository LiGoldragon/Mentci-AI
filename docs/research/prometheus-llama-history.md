# Prometheus Llama History

## Background
The original Prometheus cutover leaned on the Ollama daemon because it was the fastest way to spin up a localhost-only inference host that advertised `/v1` endpoints compatible with the existing LiteLLM router and `pi` aliases. At the time, there was a narrow window to prove out the `largeAI` node workbench, and Ollama shipped a pre-built runtime with minimal packaging effort. That trade-off is now a liability: Ollama's binary distribution disagrees with the CriomOS philosophy of keeping everything under local control, it lacks the same maintenance visibility as `llama.cpp`, and its licensing/policy posture makes it an unsuitable long-term inference surface for Prometheus.

## Why the switch to `llama.cpp`
The modern stack uses the local `llama.cpp` build that lives inside `Components/CriomOS` and is wired into both the metal and home modules. The metal config no longer provisions an `ollama` service or user; the home manager profile now strictly exposes the `prometheus-llama-server` systemd unit (see `nix/homeModule/min/default.nix`) and the LiteLLM router to hit `127.0.0.1:11436`. The `pi`/LiteLLM alias surface (the `piAgentModelAliases = [ "main" "subagent" "fast" ]` block in the same file and the `litellm-router.yaml` model map) continues to point at that local `llama.cpp` server and the `cloud-*` fallbacks, preserving the semantics of the `LiteLLM` endpoint without any Ollama fallback. This configuration also keeps inference paths localhost-only, as required for Prometheus.

## Activating the new path
1. Build the OS stack defined in `Components/CriomOS/nix/mkCriomOS/metal/default.nix metalSystem`; the evaluation should no longer reference `pkgs.ollama` or a separate tunnel service.
2. Switch your home profile with `home-manager switch --flake /home/li/git/Mentci-AI--dev#min`; the generated activation links `prometheus-llama-server` to `${pkgs.llama-cpp}/bin/llama-server` and removes any `prometheusUseOllamaFallback` toggles.
3. LiteLLM continues to present the `main`, `subagent`, and `fast` aliases, now backed exclusively by `DeepSeek-R1-Distill-Llama-70B-Q8_0.gguf` served via the local `llama.cpp` binary.

## Historical note
If you encounter references to `Ollama` in legacy logs or planning docs, treat them as the erroneous earlier choice that has now been retired. Future troubleshooting should look first at the `llama.cpp` service and `litellm-router.yaml` configuration instead of trying to resurrect the old tunnel/daemon path.
