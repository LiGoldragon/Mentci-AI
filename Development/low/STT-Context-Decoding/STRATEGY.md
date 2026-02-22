# Strategy: Context-Biased STT for Voice Prompts

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Input Reliability + Agent Throughput)

## 1. Problem
Voice prompts can contain repository-specific terms (Aski, Mentci, CriomOS, path names, symbols) that generic STT models frequently mis-transcribe.

## 2. Objective
Create a deterministic STT pipeline that:
1. transcribes voice prompts locally
2. injects repository context as decoding bias
3. produces a confidence-scored transcript artifact
4. supports iterative correction and lexicon growth

## 3. Design Constraints
- Preserve `VoicePrompts/` as ignored local input substrate.
- Keep canonical code under Clojure/Babashka orchestration.
- Avoid changing `Sources/` (read-only reference substrate).
- Keep transcript generation auditable with explicit artifacts.

## 4. Recommended Architecture
1. **Audio normalization stage**
- Convert source audio to mono 16k WAV (`ffmpeg`).

2. **Lexicon stage (context pack)**
- Source A: maintained manual list in `workflows/stt_context_terms.edn`.
- Source B: auto-mined candidate terms from repository identifiers.
- Merge A+B into bounded prompt text for decoder context.

3. **Decode stage**
- Primary engine: Whisper-compatible decoder (prefer `faster-whisper` or `whisper.cpp`) with `initial_prompt`/prefix bias.
- Multi-pass decode:
1. baseline pass (no context)
2. context-biased pass (lexicon prompt)
3. optional high-temperature recovery pass for uncertain spans

4. **Selection and verification stage**
- Compare passes with confidence metrics.
- Flag low-confidence spans and near-lexicon homophones.
- Emit transcript + uncertainty report.

5. **Execution stage**
- Persist final transcript in `Research/` or a dedicated transcript artifact path.
- Execute decoded instructions only after confidence gate passes.

## 5. Planned Tooling
1. `scripts/stt_lexicon/main.clj`
- Builds context string from `workflows/stt_context_terms.edn` + repo mining.

2. `scripts/transcribe_voice_prompt/main.clj`
- Handles normalization, decode calls, and result packaging.

3. `scripts/transcript_guard/main.clj`
- Fails execution when confidence or ambiguity thresholds are violated.

## 6. Prompt-Bias Format
Lexicon prompt should be compact and rank-ordered:
- high-priority project names
- uncommon symbols and path tokens
- user-maintained additions from prior correction cycles

Example conceptual prompt payload:
`Project vocabulary: Mentci, mentci-aid, Aski, CriomOS, attractor, attractor-docs, Lojix, jj, Babashka, Malli, Sources, Reports.`

## 7. Correction Loop
1. Agent reviews low-confidence tokens.
2. Confirmed misses are appended to `workflows/stt_context_terms.edn`.
3. Future decodes improve through accumulated local vocabulary.

## 8. Risks and Mitigations
1. **Over-biasing**
- Mitigation: keep baseline pass and require comparative scoring before selection.

2. **Toolchain drift**
- Mitigation: pin runtime/tool versions through Nix execution wrappers.

3. **False confidence**
- Mitigation: confidence gate + explicit unresolved-span reporting.

## 9. Acceptance Criteria
1. `VoicePrompts/` remains ignored and untracked.
2. Repeated domain terms show improved transcription accuracy over baseline.
3. Low-confidence spans are surfaced before instruction execution.
4. Lexicon maintenance is simple and incremental.

*The Great Work continues.*
