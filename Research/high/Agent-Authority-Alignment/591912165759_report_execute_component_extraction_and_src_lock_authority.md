# Execute Component Extraction and Source Lock Authority Migration

## Request addressed
- Extract `execute` from `mentci-aid` dependency surface.
- Remove Nix-local lock directory usage.
- Move component packaging to source-local `Cargo.lock` authority.
- Re-verify no rebuild churn on unrelated tiny commits.

## What changed
1. **Execute extracted as dedicated component source**
   - Added `Components/mentci-execute` as standalone component source.
   - `execute` derivation now uses `mentci-execute-src` instead of `mentci-aid-src`.

2. **Nix-local lock directory removed**
   - Removed `Components/nix/locks/`.
   - All component derivations now use `cargoLock = src + "/Cargo.lock"`.

3. **Component input source strategy**
   - Local component repositories with committed lockfiles are consumed as git sources:
     - `mentci-launch-src`, `mentci-user-src`, `mentci-mcp-src`, `mentci-stt-src`, `mentci-box-src`, `mentci-execute-src`.
   - This avoids super-repo path-hash coupling and keeps lock authority component-local.

4. **Bootstrap path remains minimal**
   - Default startup path uses `mentci-bootstrap` + `mentci-launch`.
   - `execute` is not part of default shell package set.

## Validation
### Full build
Executed:
- `nix build --no-link .#mentciAi .#execute .#mentciBootstrap .#chronos .#mentciStt .#mentciUser .#mentciMcp .#mentciLaunch .#mentciBox .#mentciBoxDefault .#pi .#piWithExtensions .#unifiedLlm .#vtcode`

Result:
- Successful after component-local git source/lock adjustments.

### Rebuild trigger checks (tiny unrelated commits)
Performed two tiny docs/text commits, each followed by:
- `nix build --dry-run --no-link` on the same attr set.

Result:
- Empty dry-run output in both runs (no scheduled rebuilds).

## Known caveat
Nix currently warns that relative `git+file:` URLs are future-deprecated. Functional now, but should be migrated to stable absolute/remote source references in a later hardening pass.
