# Nix Signing Key Bootstrap and Prometheus LLM Module Cleanup Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Eliminate the incorrect build-time dependency on the runtime Nix secret signing key file, redesign the signing-key flow around canonical Nix terminology and boot-time bootstrap behavior, project only the public key back into Maisiliym truth, and keep the Prometheus LiteLLM/llama services isolated in a dedicated CriomOS machine module.

**Architecture:** Separate the concerns cleanly. The runtime-local Nix secret signing key stays on the node and is generated only when appropriate. A world-readable public key file is derived beside it and becomes the operator-export artifact that is persisted back into Maisiliym truth. CriomOS consumes that truth through horizon exports, while Prometheus LLM services remain in a dedicated machine module rather than the generic Nix daemon module.

**Tech Stack:** NixOS modules, systemd oneshot/bootstrap services, Criosphere Cap'n Proto schema, Maisiliym `NodeProposal` truth, manifest-driven CriomOS deployment, LiteLLM, llama.cpp.

---

## Phase 0: Preserve current repository state before deeper implementation

### Task 0.1: Keep root and nested JJ state bounded and non-empty

**TDD scenario:** Trivial change — use judgment

**Files:**
- Verify only current repo states first

**Step 1: Ask `jj-agent` for bounded preflight in both repos**

Repos:
- `/home/li/git/Mentci-AI--dev`
- `/home/li/git/Mentci-AI--dev/Components/CriomOS`

Required commands:
- `jj status`
- `jj diff --summary`
- bounded `jj log -r 'dev|@|@-' --no-graph -n 10`

**Step 2: If either repo is dirty, isolate and finalize existing intent first**

Do not stack the signing-key/bootstrap refactor on top of unrelated dirty work.

**Step 3: Leave both repos with clean anonymous working copies before starting this plan**

Expected: one clean top-level working copy and one clean nested CriomOS working copy.

---

## Phase 1: Rename the runtime-local key surfaces to canonical Nix terminology

### Task 1.1: Rename the private-key path constants in CriomOS

**TDD scenario:** Modifying code with existing tests — run existing checks first

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/constants.nix`
- Modify: `Components/CriomOS/nix/mkCriomOS/nix.nix`
- Modify: any exact adjacent file that imports the renamed constant(s)

**Step 1: Read the current constant names and usages**

Current problematic names:
- `preCriad`
- `preCriadJson`
- anything else using the same misleading prefix for Nix signing material

**Step 2: Replace runtime-secret naming with explicit Nix signing-key names**

Recommended names:
- `nixSigningSecretKeyFile`
- `nixSigningPublicKeyFile`
- `nixSigningPublicKeyJson` only if a JSON sidecar is truly needed

**Step 3: Keep semantics split correctly**

Rules:
- private/secret key path is runtime-local only
- public key file is world-readable export material
- no field or file should imply that a public key is a private/bootstrap secret

**Step 4: Run exact targeted eval/build checks after rename**

Run:
- `nix build .#crioZones.maisiliym.prometheus.deployManifest --no-link --print-out-paths --refresh`

Expected: manifest still builds after the rename-only pass.

---

## Phase 2: Stop ordinary Prometheus builds from depending on runtime secret-key files

### Task 2.1: Tighten `secret-key-files` emission in `nix.nix`

**TDD scenario:** Modifying code with existing tests — run existing checks first

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/nix.nix`
- Read: `Components/CriomOS/nix/mkCrioZones/mkHorizonModule.nix`

**Step 1: Identify the exact current guard**

Current mistake shape:
- `secret-key-files = ...` emitted too broadly based on horizon truth that can exist before the runtime file exists

**Step 2: Replace it with a role-correct guard**

The line must be emitted only for nodes that truly need daemon signing keys at runtime.

Design requirement:
- ordinary Prometheus OS builds must not fail because the local builder lacks a runtime key file

**Step 3: Prefer role/type semantics over node-name hacks when possible**

If a node-role predicate exists for cache/build signing, use that.
If not, document the temporary narrower guard and the reason.

**Step 4: Prove the specific old blocker is gone**

Run exactly:
- `nix build .#crioZones.maisiliym.prometheus.os --no-link --print-out-paths --refresh`

Expected:
- no failure caused by `opening file '/etc/nix/preCriad'`
- no failure caused by parsing a runtime-local Nix secret file just to build Prometheus

---

## Phase 3: Add a boot-time Nix signing key bootstrap service

### Task 3.1: Create a dedicated Nix signing-key bootstrap module

**TDD scenario:** Modifying code with existing tests — run existing checks first

**Files:**
- Create: `Components/CriomOS/nix/mkCriomOS/nix-signing-key.nix`
- Modify: `Components/CriomOS/nix/mkCriomOS/default.nix`
- Modify: `Components/CriomOS/nix/mkCriomOS/constants.nix`

**Step 1: Create a dedicated machine module for Nix signing-key bootstrap**

The module should own:
- key file paths
- permissions
- bootstrap service
- ordering before the Nix daemon or any cache-signing surface

**Step 2: Implement a oneshot systemd service**

Service behavior:
1. ensure parent directories exist
2. if secret key exists: do nothing
3. if secret key missing and no trusted public key truth exists for this node: generate a new keypair
4. derive/write the public key beside it in a world-readable file
5. if secret key missing but truth already declares a public key: fail clearly instead of silently rotating keys

**Step 3: Use canonical Nix key commands**

Prefer one of:
- `nix-store --generate-binary-cache-key <name> <secret> <public>`
- or `nix key generate-secret` + `nix key convert-secret-to-public`

The plan executor must choose one and use it consistently.

**Step 4: Order it correctly**

The bootstrap service should run before:
- `nix-daemon.service`
- any binary cache service that needs the secret key

**Step 5: Verify on a local test node**

Verification commands:
- `systemctl status <bootstrap-service-name>`
- `ls -l <secret-key-path> <public-key-path>`
- `cat <secret-key-path> | nix key convert-secret-to-public`

Expected:
- secret key exists and is not world-readable
- public key exists and is world-readable
- derived public key matches stored public key file

---

## Phase 4: Rename the truth/public-key field in Criosphere and horizon

### Task 4.1: Replace `nixPreCriome` with a public-key-specific name

**TDD scenario:** Modifying code with existing tests — run existing checks first

**Files:**
- Modify: `Components/CriomOS/capnp/criosphere.capnp`
- Modify: `Components/CriomOS/nix/mkCrioZones/mkHorizonModule.nix`
- Modify: any exact adjacent CriomOS projection file using the same field

**Step 1: Rename the schema field names, not the numeric tags**

Recommended target names:
- `nixSigningPublicKey`
- `trustedNixPublicKey`
- `nixBinaryCachePublicKey`

The executor should choose one and use it consistently.

**Step 2: Preserve wire compatibility**

Do not change Cap'n Proto field numbers; only change field names and projections.

**Step 3: Rename derived booleans/methods as well**

Examples:
- `hasNixPreCriad` → `hasNixSigningPublicKey`
- `trustedBuildPreCriomes` → something aligned with `trusted-public-keys`

**Step 4: Keep final Nix public key shape canonical**

If truth stores the full Nix public key, it should be in the canonical format:
- `key-name:BASE64...`

If truth stores only the base64 payload, the plan executor must document where the key name is derived and why.

**Step 5: Run exact Prometheus build again after schema/projection update**

Run:
- `nix build .#crioZones.maisiliym.prometheus.deployManifest --no-link --print-out-paths --refresh`
- `nix build .#crioZones.maisiliym.prometheus.os --no-link --print-out-paths --refresh`

Expected: both exact attrs still evaluate/build after the rename.

---

## Phase 5: Update Maisiliym truth and operator workflow

### Task 5.1: Align Maisiliym `NodeProposal` truth with the new public-key naming

**TDD scenario:** Modifying code with existing tests — run existing checks first

**Files:**
- Modify: `/home/li/git/maisiliym/datom.nix`
- Modify: `/home/li/git/maisiliym/docs/GUIDELINES.md`
- Modify: `/home/li/git/maisiliym/README.md` if needed

**Step 1: Add/rename the exact node truth field under `NodeProposal.nodes.<node>.preCriomes`**

This field must represent the **public** signing key only.

**Step 2: Keep private key material out of Maisiliym**

Maisiliym must never store the secret/private signing key.

**Step 3: Document operator round-trip**

Required workflow:
1. node boots
2. key bootstrap service generates secret/public keypair if appropriate
3. operator reads exported public key
4. operator writes the public key back into `datom.nix`
5. operator runs Maisiliym validation
6. operator rebuilds/deploys CriomOS consumers from updated truth

**Step 4: Validate Maisiliym truth**

Run from `/home/li/git/maisiliym`:
- `nix flake check`

Expected: truth remains valid after the field rename/addition.

---

## Phase 6: Keep Prometheus LLM services in a dedicated machine module

### Task 6.1: Ensure LLM services live outside `nix.nix`

**TDD scenario:** Modifying code with existing tests — run existing checks first

**Files:**
- Create or keep: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Modify: `Components/CriomOS/nix/mkCriomOS/default.nix`
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix` only if needed for router/home config coherence
- Modify: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml` if it is kept as a real source-of-truth example

**Step 1: Keep `nix.nix` for Nix daemon configuration only**

It should not own application daemons like LiteLLM or llama.cpp.

**Step 2: Keep Prometheus LLM services in `llm.nix`**

That module should own:
- firewall ports `11434`, `11436`
- `prometheus-litellm`
- `prometheus-llama-backup`

**Step 3: Include the module through `default.nix` cleanly**

Prefer conditional inclusion based on node role/name rather than leaving the generic base module polluted with Prometheus-specific runtime logic.

**Step 4: Keep router coherence**

Requirements:
- clients hit LiteLLM on `11434`
- LiteLLM proxies to llama backup on `11436`
- no self-recursion
- router config exists on Prometheus where the system service expects it

**Step 5: Re-run exact Prometheus builds**

Run:
- `nix build .#crioZones.maisiliym.prometheus.deployManifest --no-link --print-out-paths --refresh`
- `nix build .#crioZones.maisiliym.prometheus.os --no-link --print-out-paths --refresh`

Expected: both exact attrs build without the former secret-key runtime-file blocker.

---

## Phase 7: Deploy and verify live behavior

### Task 7.1: Deploy Prometheus through the manifest-driven lane and verify end-to-end

**TDD scenario:** Modifying code with existing tests — run existing checks first

**Files:**
- Verify touched CriomOS files only

**Step 1: Build exact deployment manifest**

Run:
- `nix build .#crioZones.maisiliym.prometheus.deployManifest --no-link --print-out-paths --refresh`

**Step 2: Deploy through `execute deploy-manifest`**

Run:
- `execute deploy-manifest --manifest $(nix build .#crioZones.maisiliym.prometheus.deployManifest --no-link --print-out-paths --refresh) --node prometheus`

Use Ygg-first transport.
Do not use localhost override for Prometheus.

**Step 3: Verify runtime on Prometheus**

Run on Prometheus:
- `systemctl status prometheus-litellm`
- `systemctl status prometheus-llama-backup`
- `nix show-config | rg 'secret-key-files|trusted-public-keys'`
- `ls -l <secret-key-path> <public-key-path>`

**Step 4: Verify consumer behavior from another node**

From Ouranos:
- `getent hosts prometheus.maisiliym.criome`
- `curl -sS http://prometheus.maisiliym.criome:11434/v1/models`
- `curl -sS http://prometheus.maisiliym.criome:11436/health`

If the backup endpoint uses a different health path, document the exact path used.

**Step 5: Verify operator round-trip artifact**

Confirm the world-readable public key artifact exists and can be copied into Maisiliym truth.

Expected end state:
- Prometheus OS builds without depending on a runtime-only secret file
- signing key bootstrap service is correct
- public key export artifact exists
- Maisiliym has a clear target field for the public key
- Prometheus LiteLLM canonical endpoint works on `11434`
- direct llama backup works on `11436`

---

## Phase 8: Review and finalize

### Task 8.1: Review, guard, and finalize both repos

**TDD scenario:** Trivial change — use judgment

**Files:**
- Review all touched files in both repos only

**Step 1: Request code review**

Have a reviewer inspect:
- naming correctness vs canonical Nix terminology
- no build-time dependency on runtime secret file
- correct boot-time key generation semantics
- no silent key rotation when truth already declares a public key
- correct LLM module placement and routing

**Step 2: Run guards before finalization**

Top repo and nested repo as applicable:
- `jj status`
- `jj diff --summary`
- `execute session-guard`
- `execute root-guard`

**Step 3: Finalize via `jj-agent`**

Use bounded JJ commands in the relevant repos only.
Do not push empty commits.
Verify bookmark alignment before reporting completion.

**Step 4: Re-run live verification after push if deployment occurred**

Do not claim success without fresh output from:
- exact Prometheus builds
- manifest-driven deploy
- service status
- key bootstrap artifact checks
- cross-node HTTP probes
