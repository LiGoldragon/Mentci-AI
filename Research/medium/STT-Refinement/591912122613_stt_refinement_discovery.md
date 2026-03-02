# Research: Mentci-STT Refinement and Phonetic Correction

- **Solar:** 591912122613
- **Subject:** `STT-Refinement`
- **Status:** `discovery`
- **Weight:** `Medium`

## 1. Objective
Refine the `mentci-stt` component to improve transcription accuracy for Mentci-specific terminology and implement phonetic instruction handling.

## 2. Current State Analysis
- **Component:** `Components/mentci-stt`
- **Logic:** Rust (`src/main.rs`) using Gemini Flash 2.5.
- **Data:** Cap'n Proto schema (`schema/stt.capnp`) and EDN sidecar (`data/default_request.edn`).
- **Issues:**
    - Phonetic instructions are passed to the prompt but not verified for effectiveness.
    - Lack of automated verification for Mentci keyword accuracy.

## 3. Planned Improvements
1. **Schema Evolution:** Add `phoneticMapping` to the Cap'n Proto schema for explicit term-to-phonetic-hint mapping.
2. **Logic Refinement:** Improve prompt construction in `main.rs` to better utilize phonetic hints.
3. **Verification:** Implement a local "whisper-style" or "levenshtein" check to verify key terminology presence.

## 4. References
- `Core/SEMA_RUST_GUIDELINES.md`
- `Components/mentci-stt/schema/stt.capnp`
- `Components/mentci-stt/data/default_request.edn`
