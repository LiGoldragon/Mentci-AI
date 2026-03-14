# Qwen 3.5 llama.cpp upgrade base: runtime findings and packaging gap

## Intent
Preserve the exact state and conclusions of the current Qwen 3.5 lane so it can serve as the base for a focused `llama.cpp` upgrade to a very recent upstream with `qwen35moe` support.

This artifact is intended as the starting packet for:
- upgrading the Prometheus `llama.cpp` runtime,
- keeping the change scoped to Prometheus/CriomOS,
- and then restoring a real large-model `main-reasoning` lane using Qwen 3.5.

## What was already proven before the Qwen 3.5 pivot
From Ouranos:
- direct Prometheus LiteLLM API access works over the raw Ygg IPv6 endpoint
- the true home Pi path works when home files are correct
- the current sanity lane is good enough to return `Pong.` quickly

The older blocking issues around DNS/hostname resolution and stale home Pi state were separated from the actual runtime issues.

## Home Pi ownership was corrected declaratively in CriomOS
A key operator correction was incorporated into the implementation direction:
- **no ad-hoc host mutation** for `~/.pi/settings.json`
- ownership belongs in the CriomOS home environment

Implemented direction in source:
- `Components/CriomOS/nix/homeModule/min/default.nix`
  - now writes:
    - `~/.pi/agent/settings.json`
    - `~/.pi/settings.json`

This eliminated the old stale project-override problem when running Pi from `/home/li`.

## Pi startup-surface finding that still matters
The phrase that best captures the recurring concern is:
- **Pi startup surface**
- or **always-loaded startup extension set**

Important finding preserved from this lane:
- `lsp-pi` is still auto-loaded by default in the packaged Pi runtime
- removing it from the default startup path remains a strong next cleanup target for reducing token/context noise

This is relevant because large Pi prompt envelopes are still costly even when the runtime path itself is healthy.

## Six-model menu work that was started
The model-catalog lane was expanded conceptually toward a six-model declarative menu, but the live runtime only needs one real large-model lane first.

The most important retained intent is:
- keep the catalog/menu declarative in CriomOS
- but do not pretend six distinct runtime lanes exist until the backing services/artifacts actually exist

## Qwen 3.5 target that was chosen
The operator requested a large Qwen-family model specifically.

Resolved practical target:
- **Qwen 3.5 35B A3B**
- concrete artifact used:
  - source repo: `unsloth/Qwen3.5-35B-A3B-GGUF`
  - filename: `Qwen3.5-35B-A3B-Q8_0.gguf`
  - exact SHA256 retrieved from the HF API tree:
    - `3808866c016ab02b4adb26b873f7008a2cdd2c0704a39704050119ab0631db46`

Rationale at the time:
- large enough to satisfy the "real big model" intent
- practical single-file GGUF path
- Qwen 3.5 family explicitly desired by the operator

## Exact failure on the stable packaged runtime
With the existing packaged `pkgs.llama-cpp-rocm`, the deployed Qwen 3.5 reasoning lane failed with:
- `unknown model architecture: 'qwen35moe'`

This is the key technical pivot:
- the model artifact and fetch path were fine
- the current packaged runtime was too old for the model architecture

So the blocker is not:
- DNS
- home Pi config
- the gateway
- or the GGUF fetch itself

The blocker is:
- **runtime support gap in the packaged `llama.cpp`**

## Important upstream research conclusion
External research established:
- upstream `llama.cpp` **does** have `qwen35moe` support now
- support landed in early 2026 and is present in sufficiently recent upstream
- mainline is the right base, but ROCm/GPU support for Qwen 3.5 MoE is still sensitive and needs careful validation

This means the real follow-up lane should be:
- package a newer upstream `llama.cpp`
- not continue thrashing on model choice alone

## Exact upstream experiment that was attempted
A Prometheus-scoped local override was attempted against:
- upstream repo: `ggml-org/llama.cpp`
- exact revision: `fff0e0eafe817eef429ecb64f892ab7bdae31846`
- `fetchFromGitHub` content hash required by Nix:
  - `sha256-iYQZRSorAucYG+iqzP9R1D0EiYEL0DnOHa8Ng2u6BXE=`

This choice was made because it was a very recent master and therefore a plausible candidate for `qwen35moe` support.

## What failed in the packaging upgrade attempt
The failed upgrade work surfaced a very important packaging conclusion:
- the nixpkgs `llama-cpp-rocm` recipe is now structurally out of sync with newer upstream

Concrete drift discovered:
1. **header path drift**
   - nixpkgs expected `include/llama.h`
   - newer upstream exposes `llama.h` at repo root

2. **binary/install drift**
   - upstream now installs `server`
   - older nixpkgs logic expected `llama-server`
   - upstream CLI layout also changed relative to the older recipe assumptions

3. **example target drift**
   - newer upstream treats the server binary under example-target handling
   - older nixpkgs packaging disabled examples in a way that no longer matched current install expectations

4. **CLI surface drift**
   - after forcing the newer build through packaging, deployed services failed immediately with:
     - `error: unknown argument: --api-key`
   - so the old service flags are not safe to assume on the newer binary without re-validation

5. **GPU/ROCm support drift**
   - deployed logs also showed:
     - `Not compiled with GPU offload support, --n-gpu-layers option will be ignored`
   - meaning the override experiment did not preserve the intended ROCm-enabled build semantics correctly

## Why the upgrade experiment was backed out
The newer `llama.cpp` build was not left in place because it made the system worse:
- both the sanity lane and the reasoning lane failed under the mismatched upgraded package
- the newer package was not yet trustworthy for Prometheus deployment

So the override was intentionally removed and Prometheus was redeployed back to the stable packaged runtime baseline.

## Current recovered state after backing out the broken package override
Current meaningful recovered state:
- `prometheus-litellm`: active
- `prometheus-llama-sanity`: active
- `11434`: listening again
- `11436`: listening again

The reasoning lane remains non-operational, but the last known-good sanity baseline was restored so the machine is not left in the worse broken state.

## Most important exact technical conclusion
The next correct lane is **not** another model swap.

It is:
- **upgrade the Prometheus `llama.cpp` package recipe to a recent upstream in a Prometheus-scoped way**
- while adapting the package recipe and service flags to the new upstream layout

## Best implementation shape for the next lane
The smallest principled path is:
1. keep the override scoped to Prometheus/CriomOS only
2. start from a recent upstream `llama.cpp` revision that includes `qwen35moe` support
3. update the package recipe, not just the source pin
4. validate the following independently before any deployment claim:
   - build/install layout
   - server binary name/path
   - accepted CLI flags
   - ROCm/GPU support actually present
   - sanity lane still works on the new runtime
   - Qwen 3.5 MoE loads on the new runtime

## Strong warning preserved for the next session
Do **not** assume that a newer `llama.cpp` source pin alone is enough.

The source pin was only the beginning.
The real task is a package-recipe adaptation lane.

## Files already touched in the broader lane and worth checking first
Prometheus/CriomOS runtime surfaces:
- `Components/CriomOS/nix/mkCriomOS/llm.nix`
- `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- `Components/CriomOS/data/config/pi/prometheus-model-lock.json`
- `Components/CriomOS/nix/homeModule/min/default.nix`
- `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`
- `config/pi/prometheus-agent-settings.json`

Pi startup-surface / optional-LSP work:
- `Components/nix/pi-with-extensions.nix`
- `Components/nix/pi_with_extensions_check.nix`

## Recommended first task for the next session
The best first task is:
- inspect and locally fork/adapt the nixpkgs `llama-cpp` package recipe for recent upstream compatibility

Specifically, verify and repair:
- header copy/install assumptions
- example/server installation assumptions
- server binary naming assumptions
- ROCm build enablement still actually active
- CLI flag compatibility for the service unit

## Compact handoff summary
If this report is compacted again, keep this paragraph:

> Qwen 3.5 35B A3B was wired declaratively and the exact GGUF artifact was fetched successfully, but the current packaged `pkgs.llama-cpp-rocm` on Prometheus is too old and fails with `unknown model architecture: 'qwen35moe'`. A Prometheus-scoped upgrade attempt against recent upstream `ggml-org/llama.cpp` (`fff0e0eafe817eef429ecb64f892ab7bdae31846`) proved that the real blocker is now package-recipe drift: header path, example/server install shape, binary naming, CLI flags, and ROCm build semantics all changed relative to nixpkgs’ older recipe. The correct next lane is a focused `llama.cpp` package upgrade/adaptation for Prometheus, not more model thrashing.
