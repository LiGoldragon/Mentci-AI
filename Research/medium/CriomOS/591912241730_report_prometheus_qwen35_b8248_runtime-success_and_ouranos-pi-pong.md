# Prometheus Qwen 3.5 b8248 runtime success and Ouranos Pi pong

## Intent
Record the exact lane that produced a working Prometheus `llama.cpp` runtime for the authoritative Qwen 3.5 target and proved the Ouranos home-managed Pi acceptance command succeeds within the operator budget.

## Operator target
Authoritative target for this lane:
- model artifact: `unsloth/Qwen3.5-35B-A3B-GGUF`
- filename: `Qwen3.5-35B-A3B-Q8_0.gguf`
- public naming: preserve existing `main-reasoning` alias if possible
- acceptance machine: Ouranos home-managed Pi
- acceptance command shape:
  - fresh `pi` process
  - `--provider prometheus`
  - `--model main-reasoning`
  - `--thinking off --no-session --no-tools`
  - prompt: `Reply with exactly pong.`
- latency bar:
  - under 30 seconds
  - warm Prometheus service state is acceptable

## Root cause re-evaluation
The current CriomOS-pinned nixpkgs `llama-cpp-rocm` package was still on an older upstream snapshot:
- nixpkgs package version: `7581`
- pinned upstream commit in the built source: `f14f4e4`

That snapshot contains `qwen3moe` support but not `qwen35moe`.
The newer released upstream `b8248` explicitly contains:
- `src/llama-arch.cpp`: `qwen35moe`
- `src/CMakeLists.txt`: `models/qwen35moe.cpp`
- `src/llama-model.cpp`: `llm_build_qwen35moe`

So the blocking issue for the exact requested model remained a runtime-version gap, not a model-lock/catalog problem.

## Upstream target choice
The lane used the latest released upstream instead of master:
- chosen upstream release: `b8248`
- rationale:
  - newer than the pinned nixpkgs package
  - released, not rolling master
  - already includes `qwen35moe`
  - still retains `llama-server`, `--api-key`, and `include/llama.h`, so the current nixpkgs recipe shape still fits with a bounded version/source override

## Repo-local package implementation
A repo-local Prometheus-scoped package override was added at:
- `Components/CriomOS/nix/llama-cpp-prometheus.nix`

Implementation shape:
- derive from `pkgs.llama-cpp-rocm`
- override only:
  - `version = "8248"`
  - `src = fetchFromGitHub { tag = "b8248"; ... }`

Resolved source hash used by the final successful build:
- `sha256-2HPsaeSV9pwPm0Yh0/4ZRrrmZvvjpij5jX98bHOwn8E=`

Runtime wiring update:
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
  - now calls the repo-local package override
  - `ExecStart` points at `${llamaCppPackage}/bin/llama-server`

## Local deterministic verification before deploy
The repo-local package built successfully:
- output path:
  - `/nix/store/4wqnqwzbgfgwywzsgrmhg68c6m5ixl5b-llama-cpp-8248`

Verification from the built binary:
- `llama-server --help` still exposes:
  - `--api-key`
  - `--n-gpu-layers`
  - `--ctx-size`
  - `--parallel`

This confirmed the existing Prometheus service flag surface remained compatible with the chosen upstream release.

## Deployment result
CriomOS Prometheus OS and manifest built successfully from inside `Components/CriomOS`.
Deployment succeeded over the Yggdrasil lane.

Post-deploy runtime evidence:
- `prometheus-llama-reasoning`: active
- `prometheus-litellm`: active
- `11434`: listening
- `11437`: listening
- `prometheus-llama-reasoning` runs from:
  - `/nix/store/4wqnqwzbgfgwywzsgrmhg68c6m5ixl5b-llama-cpp-8248/bin/llama-server`

Direct model listing evidence:
- LiteLLM `/v1/models` includes:
  - `main-reasoning`
  - `qwen3.5-35b-a3b`
- direct reasoning-lane `/v1/models` includes:
  - `prometheus-main-reasoning`

## Runtime behavior findings
### Direct reasoning service
Direct authenticated call to the reasoning lane succeeded.
Observed result:
- elapsed: about `15.24s`
- response came from model `prometheus-main-reasoning`
- service fingerprint reported `b8248-5f4cdac`

This proved the new runtime was not only listening but actually serving the Qwen 3.5 lane.

### LiteLLM route
A direct LiteLLM chat-completions call to `main-reasoning` succeeded quickly after the lane warmed.
Observed result:
- elapsed: about `0.82s`
- response returned through the gateway path

### Ouranos home-managed Pi acceptance command
The first exact acceptance probe timed out at 30 seconds with no output.
This was followed by direct reasoning-lane and LiteLLM probes.
After the lane was warm, the exact home-managed Pi command succeeded.

Measured final acceptance result:
- elapsed: `20.89s`
- stdout: `pong`
- return code: `0`

So the operator success condition was met:
- Ouranos home-managed Pi
- exact `pong`
- under `30s`

## Important nuance preserved
This lane meets the requested bar only under the accepted warm-state assumption.
The first exact `pi` probe timed out before the lane had clearly settled.
After direct post-gate probes warmed the reasoning service path, the exact command passed in `20.89s`.

This means the correct summary is:
- warm-state acceptance is proven
- cold/warmup latency remains a separate tuning topic

## Pi optimization observations (document only)
No Pi behavior was changed in this lane.
Observed/documented only:
- the packaged Pi startup surface still appears heavier than necessary
- prior research indicating that `lsp-pi` is always loaded remains relevant
- reducing default startup extension load is still a worthwhile follow-up lane, but it was intentionally not implemented here

## Files touched in this lane
- `Components/CriomOS/nix/llama-cpp-prometheus.nix`
- `Components/CriomOS/nix/mkCriomOS/llm.nix`

## Compact handoff summary
> Prometheus now serves the exact requested `unsloth/Qwen3.5-35B-A3B-GGUF` artifact through a repo-local CriomOS `llama.cpp` override targeting upstream release `b8248`, which includes `qwen35moe`. The local package build succeeded, the Prometheus OS deployed successfully, `prometheus-llama-reasoning` runs from `/nix/store/...-llama-cpp-8248/bin/llama-server`, and the Ouranos home-managed Pi acceptance command now returns exact `pong` in `20.89s` under the accepted warm-service assumption. The first exact Pi probe timed out before warmup, so cold-start latency remains a separate tuning lane. Pi startup-surface optimization opportunities were observed but not implemented.
## Guard observations
- `execute session-guard` currently cannot pass until this change is pushed to `origin/dev` and the remote bookmark is aligned.
- `execute root-guard` fails because `Components/mentci-aid/src/actors/root_guard.edn` (os path reported) does not exist; the required sidecar needs to be added for the guard to succeed.
