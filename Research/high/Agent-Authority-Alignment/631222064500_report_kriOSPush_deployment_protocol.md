# KriOSPush Deployment Protocol
## Overview
KriOSPush is the canonical activation agent for all CriomOS nodes. It ensures atomic system state transitions across the mesh.

## Mechanism
1. **Derivation Projection:** Projects the system derivation (`nixos-system-...`) onto the target via `nix copy`.
2. **Remote Activation:** Invokes `kriOSActivate` on the target host, which triggers the systemd-nixos activation script.

## Execution Syntax
```bash
kriOSPush <nix-store-path-to-system-derivation> <TargetHost-IP> [SwitchFlag]
```

## Protocol (Agentic)
1. **Build:** Build the target derivation: `nix build github:Criome/CriomOS/dev#crioZones.<zone>.<species>.<node>.os --no-link --print-out-paths --refresh`.
2. **Copy/Activate:** `kriOSPush <derivation-path> <IPADDRESS>`.
3. **Verification:** `systemctl --user status <component>.service`.
