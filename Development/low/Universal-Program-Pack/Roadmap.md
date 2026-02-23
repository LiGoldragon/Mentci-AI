# Roadmap: Universal Program Pack

## Phase 1: Inventory and Boundaries
- Enumerate `Library/` artifacts that are universally reusable.
- Mark Mentci-specific artifacts that must remain local.
- Define migration candidates and no-move exclusions.

## Phase 2: Extract and Normalize
- Create `ProgramPack/` prototype tree.
- Move universal protocol/program docs into `ProgramPack/`.
- Add `manifest.edn` with stable IDs and canonical paths.

## Phase 3: Host Integration
- Add `LocalPack/` in Mentci as host extension layer.
- Implement resolver ordering (`LocalPack` then `ProgramPack`).
- Validate fallback behavior when local items are absent.

## Phase 4: Compatibility and Hardening
- Add contract tests for manifest shape and path resolution.
- Add migration notes for external repositories.
- Publish versioning rules for pack updates.

## Phase 5: Rollout
- Apply pack to one external pilot repository.
- Collect path-collision and extension feedback.
- Finalize pack naming and freeze v1 structure.
