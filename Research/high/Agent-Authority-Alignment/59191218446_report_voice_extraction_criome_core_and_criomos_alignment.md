# Voice Extraction: Criome-Core and CriomOS Alignment

## Source
- Recording: `.voice-recordings/12.17.51.6.opus`
- Transcript artifact: `.voice-recordings/transcripts/{{chronos}}.md`

## Extracted directives
1. **Contract namespacing must be layered**
   - Prefer namespace/file layering over long concatenated type names.
   - Domain access should read as context path (Criome -> node -> proposal).

2. **Species and size are typed concepts**
   - Species are enums, not arbitrary strings.
   - Size/trust should be finite magnitudes with shared behavior.

3. **Nix remains projection consumer**
   - Keep high-cost derivation out of Nix when Rust can pre-derive.
   - Nix should consume shaped data for OS/home builds.

4. **Cozo script strategic direction**
   - Cozo script is a target lingua-franca for messaging/spec lanes.
   - Migration must be incremental with explicit bridges from existing Cap'n Proto/EDN authorities.

5. **Component maturity must be explicit**
   - `criome-core` is not production; experimentation is allowed but must be clearly labeled unstable.

6. **Execution decomposition**
   - Large mixed workflows should be split into per-topic commits/subtasks to reduce context load.

## Repository integrations made in this session
- Expanded `.pi/skills/sema-programmer/SKILL.md` with a new mandatory Criome modernization lane:
  - namespaced contract/file policy,
  - species/size enum policy,
  - Nix consumer boundary,
  - Cozo transitional migration policy,
  - unstable-lane labeling requirements,
  - sub-agentization heuristic.
- Added central component stability map:
  - `Components/stability-index.edn`
- Marked `criome-core` status in central component registry as unstable MVP:
  - `Components/index.edn` (`:status :unstable-mvp`).
- Applied unstable-lane experimentation in `criome-core` implementation:
  - moved contracts to namespaced module tree,
  - introduced typed species/magnitude enums,
  - kept mvp flow tests green.

## CriomOS-oriented backlog from transcript
1. Add hotfix lane for immediate CriomOS pain points (terminal/user-env/build quality of life).
2. Continue pre-eval Rust preprocessing for CriomOS data normalization before Nix consumption.
3. Define explicit Cozo contract migration plan with reversible bridges and checkpoint reports.
4. Keep UI minimalism and context-heavy symbolic design as contract constraints for new data representations.
