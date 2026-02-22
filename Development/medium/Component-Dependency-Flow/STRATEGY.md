# Strategy: Component Dependency Flow

## Objective
Allow components under `Components/` to dynamically receive locations of required external components (also under `Components/`) through a centrally controlled structured data flow.

## Core Principle
No component may hardcode another component's filesystem path.

All cross-component locations must come from one source of truth in `Components/`.

## Central Source of Truth
Create a canonical registry in `Components/`:
- `Components/index.edn`

Registry records:
- `componentId` (stable logical name)
- `path` (current resolved repo-relative path)
- `interfaces` (provided capabilities)
- `versionRef` (optional change ID/tag/commit)
- `status` (`active`, `deprecated`, `experimental`)

## Structured Flow Design
1. **Authoritative input**
- Human/automation updates `Components/index.edn`.

2. **Validation + normalization**
- A validator script checks schema + existence + uniqueness and emits normalized map.
- Proposed entrypoint: `Components/scripts/component_registry/main.clj`.

3. **Injection layer**
- Launcher/runtime injects resolved dependency map into component execution via structured data:
  - `jailConfig.componentRegistry` (structured attrs), and/or
  - `MENTCI_COMPONENT_REGISTRY_JSON` env payload, and
  - `MENTCI_COMPONENT_REGISTRY_PATH` for file-based consumers.

4. **Component-side resolution**
- Components consume only logical IDs and resolve through injected registry.
- No direct `../other-component` assumptions.

## Typed Contract
Add a schema for registry payload:
- `Components/schema/component_registry.edn` (or capnp if cross-language binary interface is required).

Minimum fields:
- `:components` -> vector of maps with `:id`, `:path`, `:interfaces`, `:status`.

## Required Runtime APIs
- Clojure helper:
  - `Components/scripts/lib/component_registry.clj`
  - `resolve-component-path` by `componentId`
- Rust helper:
  - `Components/mentci-aid/src/component_registry.rs`
  - parse injected JSON/EDN and resolve by ID

## Enforcement Rules
1. Guard fails if component code hardcodes sibling component paths.
2. Guard fails if registry references missing paths.
3. Guard fails on duplicate component IDs.
4. Guard fails if a required dependency ID has no registry entry.

## Rollout Phases
1. **Phase 1: Registry Introduction**
- Add registry file + validator + schema.
- Keep compatibility fallback for old hardcoded paths.

2. **Phase 2: Consumer Migration**
- Migrate each component to logical-ID resolution.
- Remove fallback where each migration completes.

3. **Phase 3: Hard Enforcement**
- Block hardcoded cross-component paths in checks.
- Require registry-based resolution for all component dependencies.

## Risks
- Drift between registry and actual moved paths.
- Partial migrations leave mixed resolution modes.
- Hidden dependencies not represented in registry.

## Mitigations
- Add registry freshness check in pre-commit/session guard pipeline.
- Migrate component-by-component with explicit dependency manifests.
- Add automated scan for path literals referencing sibling components.

## Acceptance Criteria
- All cross-component dependencies are declared by logical ID.
- Runtime path resolution occurs only via injected registry data.
- Registry validation is mandatory in prompt/session guard path.
- Moving a component directory requires only registry update, not widespread code edits.
