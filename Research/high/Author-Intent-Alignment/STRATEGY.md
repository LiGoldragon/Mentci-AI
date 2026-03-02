# Strategy: Author Intent Alignment

## Objective
Realign the repository filesystem casing and authority tiers with the author's intent for durability and editability.

## Roadmap
1. [ ] **Phase 1: Definition Update**
    - Update `Core/ARCHITECTURAL_GUIDELINES.md` to reflect the stricter `ALL_CAPS` definition (Human Approval Only).
2. [ ] **Phase 2: Transition Core Files**
    - Migrate guidelines (Sema, VC) to `PascalCase`.
    - Update `Library/RestartContext.md` and `Core/AGENTS.md` load lists.
3. [ ] **Phase 3: Transition Library Files**
    - Migrate architecture protocols to `PascalCase`.
4. [ ] **Phase 4: Integrity Check**
    - Repair cross-references.
    - Validate with `root_guard`.

## Architectural Notes
- `SemaRustGuidelines.md` carries the same authority as the previous `SEMA_RUST_GUIDELINES.md`, but its casing signals to the agent that it can be refined for clarity/structure without violating a "Supreme Law".
- **Supreme Law Targets (< 5 files):**
  1. `Core/ARCHITECTURAL_GUIDELINES.md`
  2. `Core/HIGH_LEVEL_GOALS.md`
  3. `Core/AGENTS.md`
