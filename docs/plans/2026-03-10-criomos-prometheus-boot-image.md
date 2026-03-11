# CriomOS Prometheus Boot Image Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Build a first bootable CriomOS live ISO/USB for the GMKtec EVO-X2 as node `maisiliym.prometheus`, with Ethernet-first networking, inherited `maisiliym` operator access, and first-boot host SSH key generation if missing.

**Architecture:** The implementation has two layers. First, fork the external `maisiliym` node-input repo and add a new `prometheus` node proposal on a dedicated branch so CriomOS can resolve `inputs.prometheus.NodeProposal`. Second, wire that new input into the local CriomOS build graph and extend the metal/live-ISO hardware path with EVO-X2 AMD-specific configuration for a bootable first-boot image.

**Tech Stack:** Nix flakes, CriomOS topology inputs (`NodeProposal`), CriomOS `mkCriomOS` / `liveIso` / `metal` modules, JJ for local history, GitHub fork/branch flow for the external `maisiliym` input.

## Agent Integration Note
`maisiliym.prometheus` is the EVO-X2 live-image/bootstrap node. It is exported from the `maisiliym` input branch `prometheus-node`, so local CriomOS wiring should only repoint the existing `maisiliym` input to that branch to consume `inputs.maisiliym.NodeProposal.nodes.prometheus` without adding a new top-level input.

---

## Current Evidence Base

The plan is grounded in these current files:
- `Components/CriomOS/flake.nix` — currently wires the CriomOS-local `maisiliym` input from `github:LiGoldragon/maisiliym/testing`
- `Components/CriomOS/default.nix` — resolves nodes via `inputs."${name}".NodeProposal`
- `Components/CriomOS/nodeNames.nix` — active node list currently contains `maisiliym`, `goldragon`, `seahawk`
- `Components/CriomOS/nix/mkCriomOS/default.nix` — selects `liveIso` vs `preInstalled` vs `pod`
- `Components/CriomOS/nix/mkCriomOS/liveIso.nix` — bootable ISO path
- `Components/CriomOS/nix/mkCriomOS/metal/default.nix` — hardware-specific system configuration path
- `Research/medium/CriomOS/591912205623_report_evo-x2_boot-target_constraints_and_openclaw-local-llm_notes.md` — AMD AI Max+/Radeon 8060S bring-up constraints

## Milestone Target

Deliver a first artifact that satisfies all of the following:
- builds as a live ISO/USB artifact
- targets `maisiliym.prometheus`
- boots on GMKtec EVO-X2
- brings up Ethernet networking automatically
- inherits `maisiliym` operator authorized-key access
- generates host SSH private keys on first boot if missing
- makes host public keys retrievable after boot
- includes AMD GPU/OpenCL-oriented hardware config sufficient for later GPU validation

---

### Task 1: Fork the external `maisiliym` input and create a branch for `prometheus`

**TDD scenario:** Trivial/config-repo change — use judgment, but verify by reading flake outputs before and after.

**Files / repos:**
- External GitHub repo fork of `LiGoldragon/maisiliym`
- New branch on the fork, recommended name: `prometheus-node`

**Step 1: Inspect the current upstream `maisiliym` flake/input layout**

Read the fork target repository structure and identify where `NodeProposal` is defined and exported.

**Step 2: Create a real GitHub fork**

Use a real fork flow (not vendoring) to create a fork of the `maisiliym` repo if not already forked under the working account.

**Step 3: Create the branch**

Create and push a dedicated branch, recommended:
- `prometheus-node`

**Step 4: Add the `prometheus` node proposal**

Implement a new node proposal in the fork so that the flake exposes:
- `NodeProposal`
- containing a node entry for `prometheus`
- with system/machine/hardware identity appropriate to EVO-X2
- preserving the `maisiliym` operator-access inheritance model

**Step 5: Verify the forked branch exports the expected proposal**

Run a focused flake output/projection check against the fork branch.

**Expected result:**
CriomOS can later consume the fork branch as a real `inputs.prometheus` source.

**Step 6: Finalize via `jj-agent` in the external repo if that repo uses JJ; otherwise use that repo’s native VCS workflow**

Record the exact branch/ref that local CriomOS will pin.

---

### Task 2: Wire the new `prometheus` input into local Mentci/CriomOS flakes

**TDD scenario:** Modifying tested/configured code — run existing eval/build checks before and after.

**Files:**
- Modify: `flake.nix`
- Modify: `Components/CriomOS/flake.nix`
- Modify: `Components/nix/jail_sources.nix`
- Modify: `Components/CriomOS/nodeNames.nix`

**Step 1: Run a baseline focused evaluation check**

Run a bounded eval/build command that currently proves the CriomOS flake still resolves before changes.

**Step 2: Add the new flake input**

Point both the repo root and `Components/CriomOS/flake.nix` to the forked `maisiliym` branch/revision that contains the `prometheus` node proposal.

**Step 3: Mirror the source into jail wiring**

Update `Components/nix/jail_sources.nix` so the new input is available wherever CriomOS expects upstream node sources.

**Step 4: Extend the active node list**

Update `Components/CriomOS/nodeNames.nix` to include:
- `prometheus`

**Step 5: Verify the new node resolves**

Run a focused eval proving that `inputs.prometheus.NodeProposal` is now visible through the local build graph.

**Expected result:**
`Components/CriomOS/default.nix` can resolve `inputs.prometheus.NodeProposal` without missing-input failure.

**Step 6: Commit this local wiring as its own intent**

Use `jj-agent` to finalize and push a small atomic commit for the new flake/input topology wiring. Use `jj-expert` only as fallback/deep-debug rescue when the `jj-agent` lane is unavailable or misbehaving.

---

### Task 3: Define the GMKtec EVO-X2 node shape in CriomOS topology

**TDD scenario:** Modifying configuration with uncertain coverage — add focused eval checks and inspect produced structure.

**Files:**
- Modify: forked `maisiliym` repo files that define/export `NodeProposal`
- Possibly modify: `Components/CriomOS/implicitNodes.nix` only if shared implicit metadata is required
- Possibly modify: `Components/CriomOS/nix/mkCrioSphere/clustersModule.nix`
- Possibly modify: `Components/CriomOS/nix/mkCrioSphere/speciesModule.nix`

**Step 1: Inspect the resolved node structure for an existing node**

Use a focused evaluation of one known working node to understand the exact required fields.

**Step 2: Model `prometheus` consistently**

Ensure the new node encodes at minimum:
- machine species = metal
- arch = x86-64
- hostname identity = `prometheus`
- higher-level node identity = `maisiliym.prometheus`
- live-ISO behavior enabled
- Ethernet-first network assumptions

**Step 3: Preserve `maisiliym` user/access inheritance**

Do not invent a second access model. Reuse the existing `maisiliym` authorized-keys/user structure.

**Step 4: Verify topology normalization**

Run a bounded evaluation proving `mkCrioSphere` / `mkCrioZones` accept the new node proposal and produce a corresponding zone/node output.

**Expected result:**
A new resolved zone/output path exists for `maisiliym.prometheus` or equivalent node indexing in `crioZones`.

---

### Task 4: Add first-boot host SSH key generation policy

**TDD scenario:** Modifying existing system configuration — verify existing system-module behavior first, then add focused behavior checks.

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/metal/default.nix`
- Possibly modify: `Components/CriomOS/nix/mkCriomOS/default.nix`
- Possibly create/modify a dedicated module under `Components/CriomOS/nix/mkCriomOS/` if the logic should be isolated

**Step 1: Inspect current SSH/server configuration path**

Locate how SSH is currently enabled/configured for metal/live nodes.

**Step 2: Add first-boot key behavior**

Implement policy such that on boot:
- if host private keys are missing, generate them
- if they already exist, do not overwrite them

**Step 3: Expose public keys for retrieval**

Ensure host public keys are available after boot through a predictable path and/or journald/service output suitable for remote retrieval once access is established.

**Step 4: Keep user keys separate from host keys**

Do not bake host private keys into the image.
Do not disturb inherited `maisiliym` operator authorized keys.

**Step 5: Verify with a focused system config evaluation**

Confirm the SSH service and key-generation logic appear in the resulting system config.

---

### Task 5: Add EVO-X2 hardware and AMD GPU/OpenCL-oriented config

**TDD scenario:** Config/hardware enablement — use focused eval/build checks before and after each change.

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/metal/default.nix`
- Modify: `Components/CriomOS/nix/mkCriomOS/liveIso.nix`
- Possibly modify: `Components/CriomOS/nix/mkCriomOS/constants.nix`
- Possibly modify: `Components/CriomOS/nix/mkCriomOS/normalize.nix`
- Possibly modify: `Components/CriomOS/nix/mkCriomOS/nix.nix`

**Step 1: Add AMD-oriented graphics enablement**

Include the NixOS-side settings needed for AMDGPU/OpenCL-oriented bring-up on this class of hardware.

**Step 2: Add first-milestone runtime packages/tools**

Include minimum observability tools for later validation, such as candidates like:
- `clinfo`
- `pciutils`
- `usbutils`
- `ethtool`
- any AMD/OpenCL runtime packages already supported by the current nixpkgs lane

**Step 3: Apply boot/kernel parameter tuning carefully**

Add only the minimum justified EVO-X2/AMD parameters needed for first boot and later compute validation.

**Step 4: Keep this milestone Ethernet-first**

Prefer reliable wired-network bootstrapping; do not expand to Wi‑Fi unless required for the image to evaluate.

**Step 5: Verify the system config evaluates**

Run focused evaluation checks proving the new hardware config is present in the resolved system.

---

### Task 6: Produce the live ISO artifact for `maisiliym.prometheus`

**TDD scenario:** Existing build path — verify old path first, then new target build.

**Files:**
- Modify only if needed after eval failures from previous tasks
- Build target expected from: `Components/CriomOS/default.nix` / `crioZones` outputs

**Step 1: Identify the exact build attribute**

Resolve the exact attribute path for the new node’s ISO/system output.

**Step 2: Run the first target build**

Build the live ISO artifact for `maisiliym.prometheus`.

**Step 3: Capture artifact path and image metadata**

Record the resulting store path, ISO basename, and any build-time warnings/errors.

**Step 4: Fix only what blocks bootability**

If build fails, apply the smallest follow-up fix and re-run.

**Expected result:**
A bootable live ISO/USB artifact exists for the new node.

**Step 5: Commit the image-path/build-target enablement as its own intent**

Use `jj-agent` to finalize the build-target milestone once the artifact exists. Use `jj-expert` only as fallback/deep-debug rescue when the `jj-agent` lane is unavailable or misbehaving.

---

### Task 7: Validate the first-boot contract

**TDD scenario:** Integration verification — deterministic post-gates required.

**Files:**
- No required source edits unless failures appear
- Add/update a research/validation note only if substantial unexpected findings arise

**Step 1: Define the acceptance checklist**

The first boot is successful only if all of these are true:
- UEFI boot succeeds on the EVO-X2
- Ethernet comes up automatically
- SSH is reachable
- inherited `maisiliym` user access works
- host SSH keys are generated if absent
- host public keys can be retrieved after boot

**Step 2: Run on hardware**

Boot the image on the GMKtec EVO-X2 via USB.

**Step 3: Capture raw evidence**

Collect:
- IP/hostname reachability evidence
- SSH connection evidence
- public host key output
- GPU visibility evidence for later OpenCL work

**Step 4: Record any missing hardware gaps**

If OpenCL/ROCm is not yet complete, distinguish that from basic boot/access success.

**Step 5: Commit the validated bring-up state**

Finalize only after real hardware evidence exists.

---

### Task 8: Follow-up milestone (not part of first boot image)

**TDD scenario:** Deferred work.

**Out of scope for first milestone:**
- OpenClaw production integration
- local LLM serving stack on the image
- Wi‑Fi-first workflows
- internal-disk installer image
- secure automated discovery overlays (Tailscale/WireGuard/Yggdrasil-first)

These should be done only after the live image boots and remote access is stable.

---

## Verification Commands to Expect During Execution

Use bounded commands only. Likely examples:
- focused flake/eval checks for the forked `maisiliym` branch
- focused local eval/build checks for `Components/CriomOS`
- exact ISO build attribute command once discovered
- bounded JJ post-gates after each logical commit

## Commit Strategy

Use a small commit chain, not one large mutation:
1. fork/input preparation (external repo / branch)
2. local input wiring for `prometheus`
3. topology/node proposal resolution
4. SSH first-boot behavior
5. AMD/EVO-X2 hardware config
6. image build success
7. hardware validation/session summary

## Highest-Risk Unknowns

1. The exact `NodeProposal` export structure in the external `maisiliym` repo may require more than simply cloning one node entry.
2. The current CriomOS `liveIso` path may not yet emit the exact attribute shape we expect for a newly added node without additional topology normalization.
3. EVO-X2 AMD GPU/OpenCL support may require kernel/package tuning beyond first-pass NixOS defaults.
4. Existing SSH policy may already generate host keys implicitly, in which case the real task is to verify/preserve that behavior and surface public-key retrieval, not to reinvent it.

## Recommended Execution Mode

Use **Subagent-Driven (same session)** because the work naturally splits into:
- external repo fork/branch work,
- local CriomOS wiring,
- hardware config/build validation,
while still needing tight orchestration and deterministic post-gates.
