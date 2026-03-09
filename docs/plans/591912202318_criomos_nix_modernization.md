# CriomOS Nix Modernization Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Modernize `Sources/criomos` from custom flake/default.nix evaluation loops to a `flake-parts`-based flake that exposes standard `nixosModules` and then standard `nixosConfigurations`, with CozoDB added later as a pre-evaluation type-check gate for `crioSphereProposal`.

**Architecture:** Keep the migration incremental. First, wrap the existing behavior in `flake-parts` without changing semantics. Next, separate topology normalization from host construction, publish the existing module graph as standard `nixosModules`, and replace the custom `evalNixos` wrapper with `nixpkgs.lib.nixosSystem`. Only after the Nix structure is modernized should CozoDB be introduced as an external validation stage that materializes a checked topology artifact before host evaluation.

**Tech Stack:** Nix flakes, flake-parts, nixpkgs NixOS module system, home-manager, existing `mkCrioSphere`/`mkCrioZones` topology code, Rust + CozoDB for the validator stage.

---

### Task 1: Introduce `flake-parts` with zero behavior drift

**TDD scenario:** Modifying tested code — run existing evaluation probes first

**Files:**
- Modify: `Sources/criomos/flake.nix`
- Create: `Sources/criomos/nix/flake/default.nix`
- Create: `Sources/criomos/nix/flake/compat.nix`

**Step 1: Capture the current flake surface**
Run:
- `cd Sources/criomos && nix flake show .`
Expected:
- packages are present
- `crioZones` is present
- `nixosModules` is absent
- `nixosConfigurations` is absent

**Step 2: Add `flake-parts` as a flake input**
Update `flake.nix` so `outputs` becomes:
- `outputs = inputs@{ flake-parts, ... }: flake-parts.lib.mkFlake { inherit inputs; } { ... };`
- add `inputs.flake-parts.url = "github:hercules-ci/flake-parts";`
- keep the current package/crioZones behavior by importing a compatibility flake module

**Step 3: Preserve the current output contract through a compatibility module**
In `Sources/criomos/nix/flake/compat.nix`:
- import the current legacy output builder (`../../default.nix`)
- expose current `packages` and `crioZones` through `flake = { ... };`

**Step 4: Verify no output drift**
Run:
- `cd Sources/criomos && nix flake show .`
Expected:
- same package surface as before
- same `crioZones` surface as before
- no host build regressions introduced by the wrapper

**Step 5: Commit**
- `jj status`
- `jj describe -m "intent: bootstrap criomos flake-parts wrapper"`

---

### Task 2: Extract topology and package composition into explicit flake modules

**TDD scenario:** Modifying tested code — add stable evaluation probes before refactoring

**Files:**
- Modify: `Sources/criomos/default.nix`
- Create: `Sources/criomos/nix/flake/perSystem.nix`
- Create: `Sources/criomos/nix/flake/topology.nix`
- Create: `Sources/criomos/nix/lib/topology.nix`

**Step 1: Move per-system package logic out of the monolithic `default.nix`**
Extract:
- `mkPkgsAndWorldFromSystem`
- `mkNixApiOutputsPerSystem`
- `packages = world.pkdjz // { ... }`
into `Sources/criomos/nix/flake/perSystem.nix` using flake-parts `perSystem`.

**Step 2: Move topology-loading logic into a dedicated library**
Extract:
- `crioSphereProposalFromName`
- `uncheckedCrioSphereProposal`
- `proposedCrioSphere`
- `proposedCrioZones`
into `Sources/criomos/nix/lib/topology.nix`

Expose through flake outputs:
- `flake.lib.criomos.topology`

**Step 3: Add evaluation smoke probes**
Run:
- `cd Sources/criomos && nix eval .#lib.criomos.topology --apply 'x: builtins.attrNames x'`
- `cd Sources/criomos && nix flake show .`
Expected:
- topology can be evaluated
- packages still appear under per-system outputs

**Step 4: Commit**
- `jj describe -m "intent: split criomos topology and per-system flake modules"`

---

### Task 3: Publish standard `nixosModules` from the existing `mkCriomOS` module graph

**TDD scenario:** Modifying tested code — run module-level evaluation probes first

**Files:**
- Create: `Sources/criomos/nix/modules/criomos/default.nix`
- Create: `Sources/criomos/nix/flake/nixosModules.nix`
- Modify: `Sources/criomos/nix/mkCriomOS/default.nix`

**Step 1: Create a module aggregator that mirrors the current `mkCriomOS` imports**
The new module should import `users.nix`, `nix.nix`, `normalize.nix`, `network/*`, and conditionally `edge`, `router`, `metal`.
This file must return a module, not a derivation.

**Step 2: Export standard flake module outputs**
Expose in `Sources/criomos/nix/flake/nixosModules.nix`:
- `flake.nixosModules.default` (the aggregator from Step 1)

**Step 3: Convert legacy `mkCriomOS` into a transition shim**
Refactor `Sources/criomos/nix/mkCriomOS/default.nix` so it no longer owns the module graph.
Its temporary responsibility should be:
- gather `specialArgs`
- call standard `nixpkgs.lib.nixosSystem` or `pkdjz.evalNixos` as a shim
- import `self.nixosModules.default`
- return `config.system.build.toplevel` only as a compatibility layer

**Step 4: Verify standard module exposure**
Run:
- `cd Sources/criomos && nix flake show .`
Expected:
- `nixosModules` now appears

**Step 5: Commit**
- `jj describe -m "intent: expose criomos nixosModules"`

---

### Task 4: Replace `pkdjz.evalNixos` with standard `nixosConfigurations`

**TDD scenario:** Modifying tested code — add host-eval smoke checks before deleting the wrapper

**Files:**
- Modify: `Sources/criomos/nix/pkdjz/evalNixos/default.nix`
- Create: `Sources/criomos/nix/flake/nixosConfigurations.nix`
- Modify: `Sources/criomos/default.nix`

**Step 1: Introduce a standard host builder**
In `nixosConfigurations.nix`, create a helper:
- loop over `proposedCrioZones` to generate a stable host map (e.g., `"<cluster>-<node>"`).
- implementation: `inputs.nixpkgs.lib.nixosSystem { system = horizon.node.system; modules = [ self.nixosModules.default ... ]; specialArgs = { ... }; }`

**Step 2: Publish `flake.nixosConfigurations`**
Expose standard host outputs while retaining a temporary compatibility alias for `crioZones`.

**Step 3: Verify host evaluation**
Run:
- `cd Sources/criomos && nix flake show .`
Expected:
- `nixosConfigurations` is present
- compatibility `crioZones` aliases still work

**Step 4: Commit**
- `jj describe -m "intent: publish criomos nixosConfigurations"`

---

### Phase Checkpoint
At this point, the Nix refactor is complete. The system exposes standard `nixosModules` and `nixosConfigurations`. The custom evaluation loop is bypassed for the modern paths. 

The next phase will introduce CozoDB for type-checking the input `crioSphereProposal`.
