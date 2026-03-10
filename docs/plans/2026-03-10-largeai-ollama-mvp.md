# LargeAI Node + Prometheus Ollama MVP Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Introduce the `largeAI` node species, its center/server semantics, and the localhost-only Ollama MVP stack on the `prometheus` target node so CriomOS can boot a tuned GMKtec EVO-X2 large-model appliance.

**Architecture:** Extend the CriomOS schema + Rust contract surfaces to recognize a dedicated `largeAI` species, give that species center-like builder/cache behavior, and source the first `prometheus` node data from the `maisiliym` inputs. Wire a Linux memory tuning module and a systemd service that launches Ollama bound to loopback so that `prometheus` remains a self-contained inference host.

**Tech Stack:** Nix flakes (CriomOS, `maisiliym`), CriomOS `mkCrioSphere`/`mkCrioZones` horizon pipeline, Cap'n Proto schema (`Components/CriomOS/capnp/criosphere.capnp`), `criome-core` Rust contracts/tests, Linux kernel parameters (`amdgpu.gttsize`, `ttm.pages_limit`), `ollama` package/service, GMKtec EVO-X2 hardware docs (see `Research/medium/CriomOS/591912212027_report_evo-x2_linux_uma-vs-gtt_guidance_for_large-model_inference.md` for the small UMA + large shared-memory strategy), and service wiring in `Components/CriomOS/nix/mkCriomOS/metal/default.nix`.

I’m using the writing-plans skill to create the implementation plan.

---

## Current Evidence Base
- `Components/CriomOS/nix/mkCrioSphere/speciesModule.nix` defines the `nodeSpecies` list referenced by the `mkCrioZones` horizon generator.
- `Components/CriomOS/nix/mkCrioZones/mkHorizonModule.nix` calculates `typeIs`/`behavesAs` flags that drive whether a node is a builder, dispatcher, cache, etc.
- `Components/CriomOS/capnp/criosphere.capnp` describes the `NodeSpecies` enum that must stay in sync with the horizon output.
- `Components/criome-core/src/contracts/species.rs`, `proposal.rs`, and `tests/mvp_flow.rs` mirror the enum and rely on it for serialization + horizon tests.
- `Research/medium/CriomOS/591912212027_report_evo-x2_linux_uma-vs-gtt_guidance_for_large-model_inference.md` documents the small UMA + Linux shared-memory tuning strategy for GMKtec EVO-X2 inference rigs.
- `maisiliym/datom.nix` is the current node input manifest for CriomOS nodes and will need the new `prometheus` entry.

---

### Task 1: Extend CriomOS species + horizon surfaces for `largeAI`

**TDD scenario:** Modifying broadly consumed config — run the existing Rust horizon tests (`cargo test -p criome-core`) and a trimmed `nix eval`/`nix build` that includes a representative horizon output before and after the change to keep the serialization + Nix surfaces synchronized.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCrioSphere/speciesModule.nix`
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCrioZones/mkHorizonModule.nix`
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/capnp/criosphere.capnp`
- Modify: `/home/li/git/Mentci-AI--dev/Components/criome-core/src/contracts/species.rs`
- Modify: `/home/li/git/Mentci-AI--dev/Components/criome-core/src/contracts/proposal.rs`
- Modify: `/home/li/git/Mentci-AI--dev/Components/criome-core/tests/mvp_flow.rs`

**Step 1:** Add `"largeAI"` to the `nodeSpecies` enum list in `speciesModule.nix` so Nix knows it exists, update any doc comments there, and make sure the default `typeIs` helpers can produce a `largeAI` flag (e.g., by extending `mkTypeIsFromTypeName`).

**Step 2:** Adjust `mkHorizonModule.nix` to treat `typeIs.largeAI` nodes as center-like for `isBuilder`, `isNixCache`, and any other `behavesAs` logic. Ensure the horizon output still sets `typeIs`/`behavesAs` consistently for `largeAI` nodes (e.g., add the new flag to `typeIs` list and reuse `typeIs.center` logic where appropriate).

**Step 3:** Extend `capnp/criosphere.capnp` by inserting a `largeAI` entry in the `NodeSpecies` and `NodeTypeFlags` enums so Cap'n Proto consumers can read the new flag.

**Step 4:** Update `Components/criome-core/src/contracts/species.rs` to add a `LargeAi` variant, extend `NodeSpecies::as_str` to return `"large_ai"`, and propagate the variant into any `match` statements referenced in `proposal.rs` or other modules. Ensure `proposal.rs` still compiles (the enum is imported there). Add a short-term guard in `criome-core/tests/mvp_flow.rs` that includes a `NodeSpecies::LargeAi` entry to prove horizon deserialization handles it.

**Step 5:** Run `cargo test -p criome-core` and a targeted `nix eval .#crioZones.<existing node>.horizon` (e.g., `nix eval 'import Components/CriomOS {}; crioZones.maisiliym.horizon'` once the repo is stable) to verify schema/contracts stay in sync.

**Step 6:** Commit this work as the first checkpoint so future tasks rely on a fully recognized `largeAI` species.

---

### Task 2: Source the first `largeAI` node data (`prometheus`) from `maisiliym`

**TDD scenario:** Configuration/data change — validate by running `nix eval` to ensure the new node shows up in `crioZones` after wiring and by re-running any earlier horizon tests that enumerate nodes.

**Files:**
- Modify: `https://github.com/LiGoldragon/maisiliym/datom.nix` (focus on the root `datom.nix` where `astriz` nodes are declared).
- Possibly adjust the local `Components/CriomOS/nodeNames.nix` if `prometheus` needs to be reachable from CriomOS localization (verify first via `nix eval`).
- Confirm `Components/CriomOS/flake.nix` still points at the branch/ref that exposes the new proposal (this may already be done; note it in the plan to double-check the pin).

**Step 1:** Copy the `astriz` node pattern from `maisiliym/datom.nix` and add a new entry (e.g., `prometheus`) with `spici = "largeAI"`, `saiz/trost` tuned for server class (e.g., `saiz = 3`, `trost = 3`), machine `mycin` describing `arch = "x86-64"` and `modyl = "GMKtec EVO-X2"`, and `korz` sized high enough for inference.

**Step 2:** In the same entry, declare `preCriomes` that inherit the existing `maisiliym` operator keys (so we do not invent new access). Provide network identity (link-local IPs, `wireguardPreCriome`, etc.) consistent with the rest of `maisiliym` nodes.

**Step 3:** Ensure the `prometheus` entry includes `io` hints (disks, `kibord`, `butlodyr`) that align with the GMKtec EVO-X2, and annotate any AMD-specific metadata needed for later hardware tuning.

**Step 4:** Run `nix eval 'with import Components/CriomOS {}; crioZones.maisiliym.prometheus'` (or, if the new node is nested under `maisiliym`, the correct attribute) once the new data is brought into CriomOS to confirm the horizon pipeline picks up the `largeAI` node.

**Step 5:** Commit the new `maisiliym` data branch (pin the branch/ref inside `Components/CriomOS/flake.nix` if it changed) with a message describing the `prometheus` node introduction.

---

### Task 3: Encode the small UMA + large shared-memory Linux strategy for `prometheus`

**TDD scenario:** Hardware tuning — run the existing `nix eval` on the `metal` module and, once booted, verify the kernel sees the right sysfs knobs. Expect to iterate with targeted verification commands such as `nix eval 'import Components/CriomOS {}; crioZones.maisiliym.prometheus.node.system'` and `nix build .#crioZones.maisiliym.prometheus.fullOs` after each tuning step.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/metal/default.nix`
- Possibly update `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/normalize.nix` or other shared modules if the tuning should apply before specialization.

**Step 1:** Reference `Research/medium/CriomOS/591912212027_report_evo-x2_linux_uma-vs-gtt_guidance_for_large-model_inference.md` to capture the recommended firmware steps (UMA_SPECIFIED, UMA Frame Buffer ~512 MiB) and the kernel knobs for the shared-memory pool (`amdgpu.gttsize`, `ttm.pages_limit`, etc.). Document the expected sysctl/boot parameter names in the plan/implementation notes.

**Step 2:** In `metal/default.nix`, add the necessary `boot.kernelParams`, `boot.kernelModules`, and `boot.extraModprobeConfig` entries that steer the AMD driver to expose a large `GTT`/shared-memory pool, without maximizing the UMA reservation. Keep the modifications conditional on `node.typeIs.largeAI` or the `model == "GMKtec EVO-X2"` guard so the tuning applies only to the inference node.

**Step 3:** Introduce `hardware.kernelModules`/`boot.initrd.availableKernelModules` adjustments if any modules must be preloaded for `amdgpu`. Document the expectation that UMA remains small (e.g., 512 MiB) and the kernel-level GTT/TTM parameters enlarge the dynamic window.

**Step 4:** After each change, run `nix eval 'import Components/CriomOS {}; crioZones.maisiliym.prometheus.node.methods.behavesAs'` to ensure the node still surfaces correctly, and run `nix build .#crioZones.maisiliym.prometheus.fullOs` to ensure the metal module compiles.

**Step 5:** Once deployed on hardware, verify the kernel sees the planned settings via `cat /sys/module/amdgpu/parameters/gttsize`, `cat /sys/module/ttm/parameters/pages_limit`, and ensure `dmesg` shows the driver using the new shared-memory size.

**Step 6:** Commit the Linux tuning changes as a dedicated checkpoint.

---

### Task 4: Add a localhost-only Ollama service for `largeAI` nodes

**TDD scenario:** Service integration — confirm the new systemd service compiles via `nix build .#crioZones.maisiliym.prometheus.fullOs`, run `systemctl status` after activation, and (once booted) curl `http://127.0.0.1:11434/v1/models` to prove the server responds.

**Files:**
- Create/Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/metal/default.nix` (or a new `services/ollama.nix` included by the metal module) to declare the service.
- Possibly extend `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/nix.nix` or overlays if `ollama` is not currently available in pkgs (use `nix search nixpkgs ollama` to confirm availability; add an overlay if needed).

**Step 1:** Verify that `nix search nixpkgs ollama` resolves to a package or define a small overlay if `ollama` is missing. Capture the package path in the plan for later reference.

**Step 2:** In the metal module (or a new included module), add `services.ollama` with a `systemd.services.ollama` unit that:
- uses `${pkgs.ollama}/bin/ollama serve` or the correct CLI invocation,
- sets `Environment = "OLLAMA_HTTP_BIND=127.0.0.1:11434"` and any additional env variables to force localhost-only binding,
- uses `WantedBy = [ "multi-user.target" ]`, `Restart = "on-failure"`, and `AmbientCapabilities`/`CapabilityBoundingSet` to keep privileges minimal,
- includes `Requires = [ "network.target" ]` since it offers HTTP.

**Step 3:** Guard the service by checking `horizon.node.typeIs.largeAI` or `horizon.node.name == "prometheus"` so it runs only on the inference node for this MVP.

**Step 4:** Add `programs`/`environment.systemPackages` entries for CLI tooling (e.g., `pkgs.ollama`, `pkgs.curl`, `pkgs.jq`, `pkgs.htop`) that operators may use to inspect Ollama locally.

**Step 5:** After building the node, run `systemctl start ollama.service; systemctl status ollama.service` (within the built system) and, once booted, run `curl --fail http://127.0.0.1:11434/v1/models` to get a JSON response. Document the expectation that the port is unreachable from external interfaces.

**Step 6:** Commit the service wiring once it passes local `nix build` + `systemctl` checks.

---

### Task 5: Verification + live node checks on `prometheus`

**TDD scenario:** Hardware verification — after each gated commit, rerun the shortened `phase loop` (brainstorm/plan/implement/test/review) and, before concluding, gather `live` evidence by booting `prometheus`.

**Files:** None (verification only), but log results in `/home/li/git/Mentci-AI--dev/Research/medium/…` or `docs/notes` if unexpected behavior occurs.

**Step 1:** After building the new `prometheus` system, boot the GMKtec EVO-X2 and ensure the installer sees the new image.

**Step 2:** On the live node, verify:
- `cat /etc/hostname` reports `prometheus` and the horizon metadata (e.g., `cat /etc/horizon.json` includes `largeAI`),
- `systemctl status ollama.service` shows active = running with `ExecStart` referencing `ollama serve`,
- `curl http://127.0.0.1:11434/v1/models` returns an HTTP 200 so the service is bound to loopback,
- `cat /sys/module/amdgpu/parameters/gttsize`, `cat /sys/module/ttm/parameters/pages_limit`, and `dmesg` confirm the GTT/shared-memory knobs from the plan,
- `ip a`/`networkctl` show Ethernet up so the node stays reachable.

**Step 3:** Record the verification commands and outputs (e.g., `systemctl status` snippet, `curl` output) into a short note under `Research/medium` or `docs/notes` for future auditors.

**Step 4:** If any expectation fails, re-enter the plan loop at the appropriate task (e.g., revise kernel params or service). Only declare completion once all verifications pass.

**Step 5:** Tag the final commit/session with a milestone message such as `largeAI prometheus + Ollama MVP verified` and push the runtime target bookmark.

---

## Phase Strategy & Commit Checkpoints
1. **Phase 1 (Species + contracts)** — finish Task 1, ensure `cargo test`/`nix eval` pass, commit.
2. **Phase 2 (Node data + Linux tuning)** — complete Tasks 2 and 3, revalidate horizon outputs, commit after each subtask to keep history surgical.
3. **Phase 3 (Ollama service)** — implement Task 4, run `nix build` + `systemctl` checks, commit.
4. **Phase 4 (Live verification)** — execute Task 5 on hardware, gather evidence, finalize with a verification note commit.

After each commit, run the bounded verification commands listed and use `jj new` when branching into a new logical change. Keep each commit atomic and push the runtime target bookmark after verification.

---

## Risks
- The CriomOS horizon pipeline may expect hardcoded species names beyond `nodeSpecies`; ensure every reference sees `largeAI`.
- The `maisiliym` datom might need restructure to export a `NodeProposal` object; validate early when adding the `prometheus` entry.
- `ollama` may not yet exist as a packaged dependency; if so, add a lightweight overlay or vendor the binary, and document the extra step.
- Live hardware may require firmware revisions; keep the UMA/GTT doc handy and capture kernel logs when testing.
