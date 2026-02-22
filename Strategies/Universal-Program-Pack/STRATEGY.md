# Strategy: Universal Program Pack Extraction

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Architecture Portability)

## 1. Goal
Create a universal, context-agnostic pack of core programs and strategies that can be reused across repositories via canonical paths.

## 2. Core Decision
Adopt `ProgramPack/` as the single universal directory name.

## 3. Operating Model
- Keep universal assets in `ProgramPack/`.
- Keep repository-specific extensions in `LocalPack/`.
- Resolve assets with local-first precedence.
- Record all paths in EDN manifests for machine-safe discovery.

## 4. Why This Fork-Out Model
- Reduces hard coupling to Mentci-specific repository layout.
- Enables portable program reuse with stable path contracts.
- Preserves local autonomy without forking universal sources.

## 5. Initial Trials
- [ ] Trial 1: Build `ProgramPack/manifest.edn` for existing `Library/*`.
- [ ] Trial 2: Implement a resolver script that composes `LocalPack` over `ProgramPack`.
- [ ] Trial 3: Validate on one non-Mentci repository using only canonical paths.

*The Great Work continues.*
