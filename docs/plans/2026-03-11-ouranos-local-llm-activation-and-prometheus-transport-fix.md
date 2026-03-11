# Ouranos Local LLM Activation and Prometheus Transport Fix Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Make the current-user `pi` session on `ouranos` actually usable with the local LiteLLM gateway by activating the Home Manager payload and replacing the dead tailnet-only transport assumption with a working user-space path to Prometheus.

**Architecture:** Keep the gateway and client wiring in the existing Ouranos Home Manager surface, but add a user-space SSH tunnel service on `ouranos` that forwards the Prometheus loopback Ollama port into a local loopback port. Then point the LiteLLM router at that local forwarded port so the transport works without requiring immediate system-level Tailscale changes.

**Tech Stack:** CriomOS Home Manager modules under `Components/CriomOS/nix/homeModule/min/`, user `systemd` services, `ssh -L` forwarding, LiteLLM router YAML, Home Manager activation, and bounded runtime verification with `curl` plus one local `pi` config probe.

---

### Task 1: Preserve current parent-repo report/plan intent

**TDD scenario:** Trivial/doc checkpoint — no new tests, but confirm the parent dirty tree is only the intended report/index/plan files before finalizing.

**Files:**
- Modify: `Research/medium/CriomOS/index.edn`
- Create: `Research/medium/CriomOS/591912221233_report_ouranos_runtime-state_litellm-gateway_and_prometheus-reachability.md`
- Create: `docs/plans/2026-03-11-ouranos-local-llm-activation-and-prometheus-transport-fix.md`

**Step 1:** Verify the parent diff is limited to the current report/index/plan files.
**Step 2:** Ask `jj-agent` to finalize and push that parent-side investigation/planning intent.

### Task 2: Add a user-space Prometheus SSH tunnel in the nested CriomOS home module

**TDD scenario:** Modifying existing config with runtime verification — verify current build/runtime first, then add the minimal service, then verify again.

**Files:**
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`

**Step 1:** Confirm the current home build is green and the current runtime still lacks the local service chain.
**Step 2:** Add a `systemd.user.services` tunnel unit that establishes a local forward from `127.0.0.1:<local-port>` on `ouranos` to `127.0.0.1:11434` on Prometheus via SSH to `192.168.0.17`.
**Step 3:** Keep it user-space only, localhost only, and restartable on failure.
**Step 4:** Rebuild the home target and verify the tunnel unit is syntactically present in the generated profile.

### Task 3: Point LiteLLM at the local forwarded upstream instead of the dead tailnet hostname

**TDD scenario:** Modifying existing config with runtime verification.

**Files:**
- Modify: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`

**Step 1:** Replace the Prometheus upstream `api_base` from the dead tailnet host to the local forwarded loopback port.
**Step 2:** Preserve aliases (`main`, `subagent`, `fast`) and fallback policy.
**Step 3:** Re-run the bounded LiteLLM startup + `/v1/models` check.

### Task 4: Activate and verify on `ouranos`

**TDD scenario:** Verification checkpoint — no new code unless runtime checks fail.

**Files:** none required for code, but capture evidence in Research if runtime reality diverges.

**Step 1:** Activate the Ouranos Home Manager profile carrying the new gateway+tunnel wiring.
**Step 2:** Verify:
- `systemctl --user status litellm-gateway`
- `systemctl --user status <ssh-tunnel-unit>`
- `curl http://127.0.0.1:11435/v1/models`
- `ls ~/.pi/agent/models.json ~/.pi/agent/settings.json`
**Step 3:** Restart or reload `pi` so it reads the new config.
**Step 4:** Run one bounded local-LLM probe.

### Task 5: Persist verification + finalize nested repo

**TDD scenario:** Verification/report checkpoint.

**Files:**
- Create/modify a Research artifact under `Research/medium/CriomOS/` if new runtime evidence or blockers appear

**Step 1:** Persist the runtime verification evidence.
**Step 2:** Finalize the nested `Components/CriomOS` JJ line on `ouranos-litellm-mvp`.
**Step 3:** Push the preserved nested bookmark without integrating into `main`.
