# Strategy: Aski-Sema-Lojix Integration

## Objective
Re-align the project's core guidelines and documentation with the high-weight author intent captured in the `TheBookOfGoldragon` transcriptions.

## Core Integration Plan

### 1. Guideline Uplift (`Core/`)
*   **Aski Guidelines:** Update `AskiPositioning.md` (or create a dedicated `SemaAskiGuidelines.md` in `Core/`) to reflect the "keyword as object" and "no whitespace reliance" Genius of Lisp principles.
*   **Ownership & Actor Model:** Incorporate single-owner concepts into `SEMA_RUST_GUIDELINES.md` and any future `SemaActorProtocol.md`.
*   **VCS Protocol:** Explicitly mandate Jujutsu (`jj`) as the primary interface in `VersionControlProtocol.md`.

### 2. Architectural Mapping (`Library/`)
*   **SEMA Binary Spec:** Initiate a formal specification for the SEMA binary format in `Library/specs/SEMA_BINARY_FORMAT.md`.
*   **Logics (`lojix`) Spec:** Define the `lojix` syntax rules (extended EDN) in `Library/specs/LOJIX_SYNTAX.md`.
*   **Mentci Box Contract:** Document the OS-level sandboxing and read-only source contract in `Library/architecture/MENTCI_BOX_ISOLATION.md`.

## Execution Roadmap
1. [ ] **Phase 1: Knowledge Indexing**
    - Catalog all existing `Sources/` related to Aski, Lojix, and Sema.
2. [ ] **Phase 2: Formal Specification**
    - Draft the binary and syntax specs.
3. [ ] **Phase 3: Guideline Hardening**
    - Rewrite Core protocols to reflect the "True North" concepts.

## Weight Attribution
This strategy is driven by **High-Weight** sensory input. Changes derived from this track take precedence over earlier, potentially misunderstood prompts.
