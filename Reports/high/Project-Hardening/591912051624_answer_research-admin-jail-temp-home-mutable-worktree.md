# Research Report

- Subject: `Project-Hardening`
- Kind: `answer`
- Prompt: `I want an admin-jail which creates a temporary home and mounts a mutable copy of the worktree ... research ways to do this with mentci-aid`

## 1. Question
Can an admin-jail provide:
1. Temporary `HOME`
2. Mutable worktree copy
3. Compatibility with current Mentci workflows

And is this blocked by Nix only exposing a store snapshot?

## 2. Current State (Repo Evidence)
1. Jail runtime is currently Bubblewrap via upstream `jail.nix`:
- `flake.nix` uses `jail_nix = git+https://git.sr.ht/~alexdavid/jail.nix`.
- `Components/nix/mentci_jail_run.nix` builds `mentci-jail-run` with `jail_nix.lib.init`.

2. CWD is mounted mutable in jail right now:
- `Components/nix/mentci_jail_run.nix` includes `c."mount-cwd"`.
- Runtime probe inside jail succeeded:
  - `nix develop -c mentci-jail-run sh -lc 'echo ... > .tmp && rm .tmp'`
  - observed `cwd-writable`.

3. Bootstrap path for mentci-aid already supports writable workspaces:
- `Components/src/jail_bootstrap.rs` creates/uses an output workspace (`Outputs/<name>`) and writes `.mentci/runtime.json`.

## 3. Is the "Nix Snapshot Only" Concern Correct?
Partially correct, but not a hard blocker.

1. Nix derivations are built from a store snapshot, yes.
2. Runtime jail can still bind live host paths (including current checkout) via Bubblewrap mounts.
3. Therefore, mutable live-tree workflows are possible even when the shell package itself came from a store snapshot.

Conclusion: admin-jail with temp-home + mutable working tree is feasible in current architecture.

## 4. Admin-Jail Design Options

### Design Constraint (Added)
Jail lifecycle is persistent (not ephemeral) and integrated into the OS through stable paths.

Implications:
1. Primary jail identity must live under durable roots (for example `/var/lib/mentci/jails/<profile>`).
2. Runtime metadata, workspace roots, and HOME roots should be path-stable across reboots.
3. Ephemeral scratch is allowed only as subordinate cache/scratch, not as the main jail root.

### Option A: Mutable Host Tree Mount (Fastest)
Use current `mount-cwd`, but set ephemeral `HOME`.

How:
1. Add runtime setup in `mentci_jail_run`:
- Create temp home dir (`mktemp -d`) at runtime.
- `set-env HOME` to that directory.
- Optional cleanup on exit.

Pros:
1. Minimal complexity.
2. No copy cost.
3. Immediate compatibility with existing scripts.

Cons:
1. Jail edits directly touch host working tree.
2. Weaker isolation for destructive mistakes.

### Option B: Mutable Copy Workspace (Safer Admin Mode)
Create temporary copied workspace and operate there.

How:
1. Runtime entry wrapper:
- `tmp_root=$(mktemp -d)`
- `rsync -a --exclude .jj --exclude .git "$PWD/" "$tmp_root/worktree/"`
2. Start jail with cwd bound to copied tree.
3. Commit/publish path uses explicit sync-back or jj workspace transport.

Pros:
1. Strong safety boundary.
2. Easy discard.

Cons:
1. Copy overhead for large repos.
2. Sync-back complexity.

### Option C: JJ Workspace-Based Admin Jail
Use `jj workspace add` for writable isolated tree + temporary HOME.

How:
1. `mentci-aid job/jails bootstrap` creates unique workspace path in temp root or `Outputs/`.
2. Jail enters that workspace, not root checkout.
3. Commit flow remains bookmark-based (`targetBookmark`), then optional merge/push.

Pros:
1. Uses existing Mentci idioms (`runtime.json`, bookmarks, shipping).
2. Fast (no full copy if workspace metadata/share model used).
3. Stronger operational auditability.

Cons:
1. Requires extra bootstrap orchestration.

### Option D: Persistent OS-Integrated Admin Jail (Recommended Under Constraint)
Use a stable jail root and deterministic profile paths, then mount mutable workspaces within that root.

How:
1. Define stable root:
- `MENTCI_JAIL_ROOT=/var/lib/mentci/jails` (or equivalent managed path).
2. Define deterministic profile layout:
- `${MENTCI_JAIL_ROOT}/admin/home`
- `${MENTCI_JAIL_ROOT}/admin/workspaces/<workspace-id>`
- `${MENTCI_JAIL_ROOT}/admin/runtime`
3. `mentci-aid job/jails bootstrap` resolves/creates workspace under stable workspaces root.
4. Bubblewrap jail mounts these stable paths and uses them as runtime anchors.
5. Optional ephemeral temp data is nested under `${MENTCI_JAIL_ROOT}/admin/tmp`.

Pros:
1. Satisfies persistent jail + OS integration requirement.
2. Stable audit and policy integration points.
3. Compatible with mutable worktrees and JJ shipping.

Cons:
1. Requires retention/quota management.
2. Requires explicit ownership/permission management for OS-level root.

### Option E: NixOS-Managed Persistent Jail Service (Recommended Deployment Form)
Wrap Option D in a NixOS module so jail behavior is part of host OS state, not per-shell ad hoc setup.

How:
1. Add NixOS module `services.mentci-aid`:
- Creates system user/group `mentci-ai`.
- Creates stable roots (for example `/var/lib/mentci/jails/admin/...`) with owned permissions.
- Installs and runs `mentci-aid` as a `systemd` service.
2. Add controlled operator access:
- Allowlisted users enter terminal-as-`mentci-ai` through a constrained entry command (`sudo` rule or helper wrapper), not unrestricted root shell.
- Entry command lands in the persistent admin-jail profile paths.
3. Keep Bubblewrap isolation in the service execution path:
- Service and operator terminal path both route into the same jail profile contract.

Pros:
1. Persistent and reproducible at OS layer.
2. Clear security boundary (`mentci-ai` identity + explicit allowlist).
3. Operationally simpler for day-2 use (service + standardized entrypoint).

Cons:
1. Requires NixOS module maintenance and systemd policy design.
2. Requires careful `sudo`/group policy to avoid privilege leaks.

## 5. mentci-aid Integration Path

## Phase 1: Add Admin Jail Profile
1. Extend runtime policy schema with jail profiles:
- `adminMutableHost`
- `adminMutableWorkspace`
2. Add profile switch to `mentci-aid job/jails bootstrap` args and runtime JSON.

## Phase 2: Stable Path Contract
1. Add stable path fields to runtime JSON:
- `jailRoot`
- `jailProfileRoot`
- `workspaceRoot`
- `homeRoot`
- `runtimeRoot`
2. Add `homeMode`:
- `stable` (default under this constraint)
- `temporary` (debug/testing override only)
3. Implement stable root creation + permission checks.

## Phase 3: Workspace Strategy
1. Default admin profile to Option E deployment form (Option D layout + NixOS module service wrapper).
2. Keep JJ workspace isolation semantics.
3. Keep host-mount mode as explicit override.

## Phase 3.1: NixOS Module + Access Control
1. Define module options:
- `services.mentci-aid.enable`
- `services.mentci-aid.user` (default `mentci-ai`)
- `services.mentci-aid.jailRoot` (stable path root)
- `services.mentci-aid.allowedOperators` (users permitted to open terminal as `mentci-ai`)
2. Materialize:
- system user/group
- path creation tmpfiles rules
- systemd unit for daemon mode
- operator entry wrapper/sudoers drop-in bound to explicit command.

## Phase 4: Shipping and Audit
1. Keep `mentci-commit` and `mentci-jj` bookmark policy unchanged.
2. Record profile + stable jail roots in runtime JSON for traceability.

## Phase 5: Retention and Hygiene
1. Add pruning policy for stale workspaces.
2. Add quota and ownership drift checks for stable jail roots.

## 6. Recommendation (Updated for Persistent Constraint)
1. Keep Nix + Bubblewrap jails for deterministic tool/runtime closure and policy control.
2. Implement admin-jail as:
- Stable jail roots (always)
- Stable HOME by default (`temporary` only as explicit debug override)
- JJ-isolated mutable workspace (default)
- Direct host mutable mount only via explicit override.
 - NixOS module-managed `mentci-aid` service with allowlisted terminal-as-`mentci-ai` operator path.

This satisfies persistent OS integration while preserving reproducibility and auditability.

## 7. If You Drop Nix Jails Entirely
Feasible, but tradeoffs:
1. Lose deterministic dependency/runtime closure.
2. Need replacement for package/runtime reproducibility and policy distribution.
3. Operational drift risk increases across machines.

A middle ground is more robust under this constraint:
1. Keep Nix for environment reproducibility.
2. Keep Bubblewrap jail runtime with persistent stable roots.
3. Treat ephemeral paths as subordinate scratch only.
