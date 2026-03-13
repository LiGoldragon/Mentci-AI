# Prometheus OS-Level LiteLLM + llama.cpp Refactor Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Move the Prometheus serving stack from user-home/user-service assumptions to OS-level services, making LiteLLM the canonical mesh-facing API on port 11434 while keeping direct llama.cpp on 11436 as a debug/backup lane.

**Architecture:** Keep CriomOS as the system/service authority and preserve current `/home/li/...` model/config paths for the first pass to reduce migration risk. Introduce OS-level `systemd.services` for LiteLLM and llama.cpp, open the canonical LiteLLM port in the firewall, and update Prometheus metadata/router config in the same pass so remote nodes can use `prometheus.maisiliym.criome` consistently.

**Tech Stack:** NixOS modules, systemd services, LiteLLM, llama.cpp (ROCm), Prometheus model catalog JSON, bounded runtime verification over Ygg/DNS.

---

### Task 1: Capture the current user-service surface in code comments / plan context

**TDD scenario:** Trivial change — use judgment

**Files:**
- Read: `Components/CriomOS/nix/homeModule/min/default.nix`
- Read: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`
- Read: `Components/CriomOS/nix/mkCriomOS/nix.nix`
- Read: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`

**Step 1: Confirm current service/port ownership**

Verify:
- user-service llama currently binds `11436`
- user-service LiteLLM currently binds localhost `11435`
- firewall currently exposes `11436`

**Step 2: Preserve the current path assumptions for first pass**

Do not move `/home/li/.local/share/prometheus-llama/models/...` or router/config storage yet.

**Step 3: Record any path assumptions in the implementation notes**

Expected: the code changes later should clearly show these are transitional and slated for a second pass.

### Task 2: Add OS-level llama.cpp service on Prometheus

**TDD scenario:** Modifying code with existing tests — run existing tests first

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/default.nix` or the exact OS-level module insertion point selected during implementation
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`

**Step 1: Remove the canonical llama service role from the user-service lane**

Keep the user-level fallback/debug assumptions explicit, but stop treating them as the primary service surface.

**Step 2: Add an OS-level `systemd.services` unit for Prometheus llama**

Requirements:
- node-gated to Prometheus only
- `After=network-online.target`
- binds to `0.0.0.0:11436`
- uses the existing DeepSeek GGUF path in `/home/li/.local/share/prometheus-llama/models/...`
- restart on failure

**Step 3: Verify the unit text is generated in the system config**

Expected: the service is clearly OS-level, not `systemd.user.services`.

### Task 3: Add OS-level LiteLLM service on Prometheus as canonical API

**TDD scenario:** Modifying code with existing tests — run existing tests first

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/default.nix` or the selected OS-level service module
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Modify: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`

**Step 1: Introduce an OS-level LiteLLM service**

Requirements:
- node-gated to Prometheus
- binds on canonical external port `11434`
- not localhost-only
- depends on the local llama service being available
- uses a system-managed service definition, not a user service

**Step 2: Update router config to make LiteLLM canonical**

Requirements:
- canonical DeepSeek model path routes through LiteLLM `11434`
- direct llama remains on `11436` as backup/debug
- preserve a single canonical model first (`DeepSeek-R1-Distill-Llama-70B`)

**Step 3: Keep llama backup semantics explicit**

Expected: 11436 is documented and wired as backup/debug, not the main path.

### Task 4: Update Prometheus metadata in the same pass

**TDD scenario:** Modifying code with existing tests — run existing tests first

**Files:**
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: any exact adjacent generated-config source in `homeModule/min/default.nix` that reflects the catalog

**Step 1: Annotate canonical vs backup service expectations**

Add metadata sufficient to reflect:
- canonical service endpoint / port = LiteLLM `11434`
- backup/debug endpoint / port = llama `11436`

**Step 2: Keep one canonical model stable first**

DeepSeek-R1-Distill-Llama-70B remains the main stable model surface.

**Step 3: Ensure provider/model naming still works for remote nodes**

Expected: remote clients can understand Prometheus as the directly reachable model node via `prometheus.maisiliym.criome`.

### Task 5: Open the canonical LiteLLM firewall port

**TDD scenario:** Modifying code with existing tests — run existing tests first

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/nix.nix`

**Step 1: Add `11434` to the Prometheus firewall allowance**

Keep `11436` open for the direct llama backup lane.

**Step 2: Do not widen exposure beyond what is needed**

Expected: only the canonical and backup ports are explicitly allowed.

### Task 6: Verify exact build and deployment behavior

**TDD scenario:** Modifying code with existing tests — run existing tests first

**Files:**
- Verify the touched Nix/service files only

**Step 1: Run the smallest relevant verification commands**

Examples:
- targeted exact-attr build for the Prometheus system output
- targeted exact-attr build for the Prometheus deploy manifest if still used in this lane

**Step 2: Deploy Prometheus through the manifest-driven deploy path**

Use the already-built safe path:
- manifest-driven deploy
- Ygg-first transport

**Step 3: Verify remote service surfaces**

From another node (e.g. Ouranos), verify:
- DNS: `getent hosts prometheus.maisiliym.criome`
- LiteLLM canonical API on `11434`
- llama backup API on `11436`
- one minimal API probe for each reachable endpoint

### Task 7: Review and finalize

**TDD scenario:** Trivial change — use judgment

**Files:**
- Review all touched CriomOS files only

**Step 1: Request review**

Have a reviewer inspect the OS-levelization for:
- service ordering
- home-path assumptions
- firewall correctness
- metadata consistency

**Step 2: Finalize via bounded JJ commands**

Inside `Components/CriomOS` only:
- `jj status`
- `jj diff --summary`
- `jj describe -m "..."`
- `jj bookmark set dev -r @`
- `jj git push --bookmark dev`
- `jj new dev`

**Step 3: Re-run post-deploy verification**

Expected end state:
- `prometheus.maisiliym.criome:11434` is the canonical reachable LiteLLM API
- `prometheus.maisiliym.criome:11436` remains the direct llama backup/debug API
- both are OS-level services
- model/config storage migration remains a separate second pass
