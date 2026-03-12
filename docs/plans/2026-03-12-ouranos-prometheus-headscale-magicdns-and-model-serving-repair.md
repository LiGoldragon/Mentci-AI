# Ouranos ↔ Prometheus: Headscale+MagicDNS Overlay, then Prometheus Serving Repair, then Ouranos Pi Validation

> Planning only. Execution should be done with **exact model `openai-codex/gpt-5.2`**.

## Goal
Deliver a stable, encrypted overlay network between **ouranos** and **prometheus** with **stable hostnames (MagicDNS-equivalent)**, then repair Prometheus-side `llama.cpp` model serving so it is reachable over that overlay, then validate that the current-user **Pi** session on Ouranos can reach Prometheus models via the local **LiteLLM gateway**.

## Why this plan exists (current pain)
- Prior work assumed a tailnet hostname (e.g. `maisiliym.prometheus.tailnet`) would resolve, but **Ouranos cannot currently resolve it** (see: `/home/li/git/Mentci-AI--dev/Research/medium/CriomOS/591912221233_report_ouranos_runtime-state_litellm-gateway_and_prometheus-reachability.md`).
- We want to replace brittle LAN-IP/hosts hacks with a **real overlay** + **DNS-based stable naming**.

## Scope and constraints
- Overlay tech: **Headscale (control plane) + Tailscale clients**.
- DNS: configure Headscale DNS so devices resolve names like `prometheus.<base_domain>` and `ouranos.<base_domain>`.
- Keep the post-compaction handoff lean:
  - Prefer **a small number of atomic commits**.
  - Avoid introducing broad refactors while wiring overlay.

## Likely code/config surfaces
### NixOS / system-level (CriomOS)
- `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/network/default.nix` (imports network stack)
- `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/network/wireguard.nix` (existing WG mesh; ensure no conflict)
- Likely new module location (preferred, if we implement in this repo):
  - `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/network/tailscale.nix`
  - `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/network/headscale.nix`

### Home Manager / user-level services and routing
- `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix`
  - `prometheus-llama-server` user service (Prometheus)
  - `litellm-gateway` user service (Ouranos)
  - `litellmRouterYaml` upstream `api_base` uses `prometheusCriomeHost`

### Model metadata
- `/home/li/git/Mentci-AI--dev/Components/CriomOS/data/config/pi/prometheus-model-catalog.json`

### Activation surfaces (explicit: NixOS vs Home Manager)
This plan crosses **two different activation mechanisms**. Treat them as separate “surfaces” so changes actually take effect.

- **NixOS / system-level (CriomOS) activation** (Headscale, system firewall, system resolver):
  - Apply with: `sudo nixos-rebuild switch --flake /home/li/git/Mentci-AI--dev#<HOST>`
  - `<HOST>` must match the machine output (e.g. `ouranos` / `prometheus`). If unsure, discover via `nix flake show /home/li/git/Mentci-AI--dev`.
- **Home Manager / user-level activation** (LiteLLM gateway, prometheus-llama-server user unit, per-user config):
  - Apply with: `home-manager switch --flake /home/li/git/Mentci-AI--dev#min`
  - Restart user units with `systemctl --user restart ...`.

**Rule:** whenever a step says “implement in NixOS”, it requires a *NixOS rebuild*; whenever it says “Home Manager”, it requires a *home-manager switch*.

---

## Phase 0 — JJ + repo preflight (lean handoff)
**Objective:** start from a clean, bounded state and produce commits that are easy to review.

1. **Identify target bookmark**
   - Check env: `echo "$MENTCI_TARGET_BOOKMARK"`.
   - If unset, **stop** and decide target before editing history.
2. **Preflight**
   - `jj status`
   - `jj diff --summary`
   - `jj log -r "$MENTCI_TARGET_BOOKMARK|@|@-" --no-graph`
3. **Working rule**
   - One described commit per phase (overlay, serving fix, Ouranos routing), unless a phase needs split for safety.

**Checkpoint:** working copy is clean and target bookmark is known.

---

## Phase 1 — Build overlay: Headscale + Tailscale clients + DNS (MagicDNS-like)

### 1A. Decide topology (hard requirement)
Pick exactly one control-plane placement and record it in the commit message / config:
- **Preferred:** run **Headscale on `ouranos`** (always-on workstation/spine) per `/home/li/git/Mentci-AI--dev/Research/medium/CriomOS/631222063400_report_wireguard-mesh_headscale-vs-netbird-netmaker-innernet_for_prometheus-ouranos.md`.
- Alternative: Headscale on Prometheus (only if Ouranos uptime is not reliable).

Also decide:
- Tailnet DNS base domain (example): `tailnet` → names become `prometheus.tailnet`, `ouranos.tailnet`.
- Whether to enforce ACLs/tags now (recommended minimal ACL: only allow Ouranos ↔ Prometheus on the required ports).

### 1A0. Decision gate: Headscale TLS / cert strategy (must decide before enrollment)
Tailscale clients generally expect the `--login-server` to be **HTTPS** and may reject insecure or untrusted endpoints.

Decide one and write it down before implementing config:
1. **Publicly trusted cert** (preferred if feasible): run Headscale behind Caddy/Nginx with a real DNS name + Let’s Encrypt.
2. **Internal PKI**: issue a cert for the Headscale endpoint and ensure both Ouranos and Prometheus trust that CA.
3. **Temporary dev-only trust**: use self-signed and explicitly configure trust on clients (only if you’re willing to do the OS trust-store work).

**Checkpoint:** you can `curl -fsS https://<headscale-endpoint>/health` (or equivalent) from Prometheus without TLS trust errors.

### 1A1. Decision gate: DNS integration (ack Unbound / pinned nameservers)
Do **not** assume `--accept-dns=true` will “just work” on NixOS: the host may be running **Unbound** locally, using **systemd-resolved**, or have a **pinned** `/etc/resolv.conf`.

Decide how tailnet DNS will integrate:
- **If Unbound is present**: either
  - configure Unbound to forward `.<base_domain>` to the Headscale/Tailscale DNS nameserver(s), *or*
  - keep Unbound as the resolver and set Headscale `nameservers` to point at Unbound, plus a split-DNS rule for the base domain.
- **If systemd-resolved is used**: prefer split-DNS integration and ensure Tailscale hooks are enabled.
- **If nameservers are pinned**: plan to explicitly wire resolver behavior in NixOS (don’t rely on runtime mutation).

**Checkpoint:** after enrollment, `getent hosts prometheus.<base_domain>` works on both machines *without* manual `/etc/hosts` edits.

### 1B. Implement Headscale (NixOS)
**Plan shape (NixOS module):**
- Enable service: `services.headscale.enable = true;`
- Configure `settings`:
  - `server_url` (must be reachable by clients)
  - `listen_addr`
  - `dns_config` with:
    - `magic_dns` (or headscale equivalent)
    - `base_domain = "tailnet"` (or chosen)
    - `nameservers` (LAN resolvers and/or public)

**Firewall + ports:**
- Ensure Headscale listen port is reachable from Prometheus (LAN is OK initially).

**Provisioning commands (run on Headscale host):**
- `headscale users create <user>`
- `headscale preauthkeys create --user <user> --reusable --expiration 24h`

**Verification checkpoint (Headscale host):**
- `systemctl status headscale`
- `headscale nodes list` (should be empty before enrollment)

### 1C. Enroll Tailscale clients (Ouranos + Prometheus)
On each node:
- Enable Tailscale client (NixOS recommended): `services.tailscale.enable = true;`
- Bring up with headscale login server:
  - `tailscale up --login-server https://<headscale-host>:<port> --auth-key <preauth> --accept-dns=true`

**Verification checkpoints (each node):**
- `tailscale status`
- `tailscale ip -4`
- `tailscale ping prometheus` (from Ouranos)

### 1D. Verify DNS (MagicDNS-equivalent)
From **Ouranos**:
- `getent hosts prometheus.<base_domain>`
- `ping -c1 prometheus.<base_domain>`

From **Prometheus**:
- `getent hosts ouranos.<base_domain>`

**Expected:** DNS returns an overlay IP (100.x.y.z style) and ping succeeds.

**Discovery checkpoint (do this before Phase 3 hardcodes hostnames):**
Headscale/Tailscale naming can differ from your assumption (`prometheus.<base_domain>` vs a fully qualified tailnet name).

- On the **Headscale host**:
  - `headscale nodes list`
  - Record the actual node names / FQDNs that Headscale believes it is serving.
- On **Ouranos**:
  - `tailscale status`
  - Record the exact MagicDNS-style name shown for Prometheus (call it `PROMETHEUS_TAILNET_FQDN`).

**Rule:** Phase 3 must use the discovered `PROMETHEUS_TAILNET_FQDN` instead of guessing a suffix.

### 1E. Commit expectations for Phase 1
- Commit includes:
  - any new network modules added under `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/network/`
  - wiring changes to import them
  - minimal docs/comments in code only (no new md files beyond this plan)
- Push:
  - `jj describe -m "overlay: headscale+tailscale + magicdns for ouranos/prometheus"`
  - `jj push -b "$MENTCI_TARGET_BOOKMARK"`

**Hard blocker list (Phase 1):**
- Tailscale client refusing non-HTTPS `--login-server` (may require real TLS certs or a reverse proxy).
- Headscale DNS config not taking effect (often `--accept-dns` or OS resolver integration).
- Firewall on either node blocking overlay handshake.

---

## Phase 2 — Prometheus: repair `llama.cpp` serving after overlay works

### 2A. First verify local serving on Prometheus
On **Prometheus**:
1. Service health:
   - `systemctl --user status prometheus-llama-server`
2. Port listening:
   - `ss -ltnp | rg ":11436"`
3. Local API probe:
   - `curl -fsS -H 'Authorization: Bearer sk-no-key-required' http://127.0.0.1:11436/v1/models | head`

If the service is down:
- Inspect logs: `journalctl --user -u prometheus-llama-server -n 200 --no-pager`

### 2B. Confirm models actually exist
Prometheus model directory is declared in:
- `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix` (`prometheusLlamaModelDir`)

On Prometheus:
- `ls -la ~/.local/share/prometheus-llama/models/`
- Ensure at least one of the canonical files exists:
  - `DeepSeek-R1-Distill-Llama-70B-Q8_0.gguf`
  - `Qwen-2.5-72B-Instruct.gguf`
  - `Llama-3.3-70B-Instruct.gguf`

If missing, decide:
- copy/sync GGUFs into that directory, OR
- update canonical model filenames in the home module (only if the repo is the source of truth).

### 2C. Make serving reachable *over overlay*, safely
Current Prometheus unit runs `llama-server` with `--host 0.0.0.0` (see `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix`).

Pick one exposure strategy:
1. **Preferred:** bind to the tailnet interface only (requires runtime discovery of tailnet IP) OR bind to `0.0.0.0` and restrict with NixOS firewall rules to the overlay interface.
2. Alternative: keep `llama-server` on loopback and add a local TCP forward via `tailscale serve tcp 11436 127.0.0.1:11436` (if available/acceptable).

**Verification checkpoint (from Ouranos, once chosen):**
- `curl -fsS -H 'Authorization: Bearer sk-no-key-required' http://<PROMETHEUS_TAILNET_FQDN>:11436/v1/models | head`

### 2D. Commit expectations for Phase 2
If code changes are required (service bind/firewall/model filename fixes):
- Primary file:
  - `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix`
- Optional (if firewall rules are added at OS layer):
  - `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/network/*.nix`

Commit message example:
- `prometheus: fix llama.cpp serving + expose on tailnet safely`

Verification required before pushing:
- local `/v1/models` on Prometheus
- remote `/v1/models` from Ouranos over overlay

---

## Phase 3 — Ouranos: point LiteLLM routing at the stable overlay hostname

### 3A. Update the Prometheus upstream host selection
In `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix`:
- `litellmRouterYaml` currently uses `prometheusCriomeHost` for `api_base`.

Implement a deterministic preference (only for Ouranos):
- if `isOuranosNode`, set `prometheusCriomeHost = "<PROMETHEUS_TAILNET_FQDN>"` where `<PROMETHEUS_TAILNET_FQDN>` is recorded in **Phase 1D (Discovery checkpoint)**.
  - Do **not** guess this value (it might or might not be `prometheus.<base_domain>` depending on Headscale naming).
- else keep existing behavior (nodeIp / criome domain fallback).

### 3B. Rebuild + activate Ouranos home profile
On Ouranos:
- `home-manager switch --flake /home/li/git/Mentci-AI--dev#min`

Restart gateway:
- `systemctl --user restart litellm-gateway`
- `journalctl --user -u litellm-gateway -n 200 --no-pager`

### 3C. Verify Ouranos gateway now reaches Prometheus over overlay
On Ouranos:
- `curl -fsS http://127.0.0.1:11435/v1/models | head`
- Make one completion call routed to Prometheus (model alias must exist):
  - `curl -fsS http://127.0.0.1:11435/v1/chat/completions \
      -H 'Content-Type: application/json' \
      -d '{"model":"main-deepseek","messages":[{"role":"user","content":"say ok"}]}' | head`

**Expected:** request succeeds without DNS errors, and Prometheus model path is used (check gateway logs).

### 3D. Commit expectations for Phase 3
- Single commit touching only:
  - `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/homeModule/min/default.nix`

Commit message example:
- `ouranos: route litellm -> prometheus via headscale dns hostname`

---

## Phase 4 — Validate access from the Ouranos Pi session (end-to-end)

### 4A. Confirm Pi is pointed at local gateway
These are written by the Ouranos Home Manager profile:
- `~/.pi/agent/models.json`
- `~/.pi/agent/settings.json`

Verify on Ouranos:
- `jq . ~/.pi/agent/settings.json`
- Confirm provider baseUrl is `http://127.0.0.1:11435/v1`.

### 4B. Pi probe
From the same Ouranos user session:
- `pi --help` (sanity)
- Run one small request against the default model in settings.

**Checkpoint:** Pi request succeeds and logs show traffic reaches Prometheus via the overlay DNS hostname.

---

## Finalization expectations (JJ)
- After each phase:
  - `jj status` must be clean
  - `jj diff --summary` must match intended surfaces only
  - `jj describe` must be set on the revision
  - push the runtime target bookmark: `jj push -b "$MENTCI_TARGET_BOOKMARK"`
- End state:
  - no stacked empty commits above the bookmark
  - `jj log -r "$MENTCI_TARGET_BOOKMARK|@|@-" --no-graph` shows a small, reviewable sequence

---

## Likely blockers / contingencies
- **TLS for Headscale:** if clients require a real cert, plan to front Headscale with Caddy/Nginx and obtain a cert (LAN-only may require internal PKI).
- **DNS not propagating:** verify `--accept-dns=true`, OS resolver integration, and headscale `dns_config`.
- **Port exposure risk:** avoid leaving `llama-server` open to LAN; restrict to tailnet interface or firewall.
- **Model asset availability:** if canonical GGUF filenames differ, align the module list or rename/link assets.
