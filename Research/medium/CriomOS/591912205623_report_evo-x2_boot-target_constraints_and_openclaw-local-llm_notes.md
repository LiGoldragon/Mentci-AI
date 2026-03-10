# Report: EVO-X2 Boot Target Constraints and OpenClaw Local-LLM Notes

## Prompt
Use subagents to research and implement a criomos modernization that was drafted in `docs/plans/591912202318_criomos_nix_modernization.md` - first achieve a working image to boot on a GMKtec EVO-X2 which will run LLM models and OpenClaw.

## Summary
- The drafted modernization plan is structurally useful but path-stale: the active target in this repo is `Components/CriomOS`, not `Sources/criomos`.
- The plan as written modernizes the flake surface (`flake-parts`, `nixosModules`, `nixosConfigurations`), but it does not yet guarantee the user's nearer goal: one real bootable EVO-X2 image with AMD GPU / OpenCL / local-LLM viability.
- External evidence indicates the EVO-X2 platform is a strong local-LLM candidate, but AMD GPU runtime details matter:
  - the machine is used in the wild for local inference on Linux,
  - OpenCL on NixOS should be configured through the NixOS AMDGPU module rather than ad-hoc package installs,
  - OpenClaw local mode is possible, but its own docs warn that local deployments want large context and a strong model/runtime stack.

## External Findings

### 1. EVO-X2 hardware and local inference viability
Strong evidence:
- Nish Tahir benchmark note identifies the GMKtec EVO-X2 as `AMD RYZEN AI MAX+ 395 w/ Radeon 8060S` with `128GiB` RAM and documents successful local inference on Linux after ROCm/GPU-driver setup.
- The `pablo-ross/strix-halo-gmktec-evo-x2` repository documents a real Strix Halo / EVO-X2 workflow for ROCm-backed llama.cpp inference, including `gfx1151`, large-GTT tuning, and OpenAI-compatible llama-server deployment.

Practical intent:
- Use the EVO-X2 as a single-box local inference machine.
- Materialize a bootable Linux image that can later host a local model server and an OpenClaw instance pointed at that server.

Relevant message/data shape:
- local LLM runtimes are exposed as OpenAI-compatible HTTP endpoints (`/v1` style model APIs in llama.cpp / LM Studio / compatible proxies), which is the shape OpenClaw expects for local-model integration.

### 2. NixOS AMD GPU / OpenCL implications
Strong evidence:
- NixOS Wiki `AMD_GPU` page states:
  - `hardware.graphics.enable = true;`
  - `hardware.amdgpu.opencl.enable = true;`
  - OpenCL must be configured at system level; ad-hoc shell installs are insufficient because the ICD loader uses fixed paths.
- The same page explicitly calls out high-RAM AMD iGPU systems for LLM use and notes that GTT/TTM tuning matters more than dedicated VRAM on such systems.

Practical intent:
- The first bootable image should expose the AMD iGPU correctly, enable graphics and OpenCL at the OS/module level, and include validation tools such as `clinfo`/`rocminfo` or equivalent runtime checks.

Relevant message/data shape:
- NixOS module configuration attrs for hardware/runtime setup (`hardware.graphics`, `hardware.amdgpu.opencl`, `boot.kernelParams`, runtime packages).

### 3. OpenClaw local-model constraints
Strong evidence:
- Official OpenClaw local-model docs say local deployment is possible via OpenAI-compatible local endpoints, but recommend large context and a strong local model stack.
- OpenClaw expects a provider entry with a `baseUrl`, model ID, context window, and token limits.

Practical intent:
- OpenClaw does not need to live inside the boot image's earliest milestone if the first milestone is just "boot the box and prove GPU/OpenCL/local-model viability".
- A practical first phase is: bootable system -> GPU/OpenCL validation -> local model server -> OpenClaw hookup.

Relevant message/data shape:
- OpenClaw local provider config resembles:
  - `baseUrl = "http://127.0.0.1:<port>/v1"`
  - `models = [{ id, name, contextWindow, maxTokens, ... }]`

## Repo-State Findings
- The active CriomOS target in this repo is `Components/CriomOS`.
- Current flake shape is still legacy/custom:
  - `flake.nix` shells directly into `default.nix`
  - `default.nix` exports per-system packages and custom `crioZones`
  - `nix/mkCriomOS/default.nix` still builds through `pkdjz.evalNixos`
- The drafted plan should therefore be adapted to current paths before execution.

## Execution Implication
The safest first implementation batch is not "flake-parts everywhere" in the abstract. It is:
1. adapt the old plan to `Components/CriomOS`,
2. identify one EVO-X2-targeted host/image output,
3. make that output bootable/evaluable,
4. layer AMD GPU/OpenCL validation into that path,
5. only then continue the broader flake modernization.

## Sources
- NixOS Wiki — AMD GPU: https://wiki.nixos.org/wiki/AMD_GPU
- OpenClaw docs — Local Models: https://docs.openclaw.ai/gateway/local-models
- Pablo Ross — Strix Halo GMKtec EVO-X2 setup: https://github.com/pablo-ross/strix-halo-gmktec-evo-x2
- Nish Tahir — GMKTec Evo X2 benchmarks: https://nishtahir.com/gmktec-evo-x2-ryzen-ai-max-395-benchmarks/
