# Research: STT Context Injection Options

## 1. Evaluation Axes
- local/offline support
- context bias support (initial prompt, hotwords, grammar)
- runtime cost
- integration ease with Babashka orchestration

## 2. Candidate Engines
1. **faster-whisper (CTranslate2)**
- Strengths: strong quality/speed tradeoff, widely used Whisper-compatible API.
- Context support: `initial_prompt`/prefix style prompting.
- Integration: invoke through Python CLI wrapper or small adapter process.

2. **whisper.cpp**
- Strengths: local CPU-focused deployment, reproducible binaries.
- Context support: prompt-based priming, practical for offline environments.
- Integration: command-line friendly for Babashka wrappers.

3. **OpenAI Whisper reference**
- Strengths: canonical model behavior.
- Tradeoff: often slower/heavier operationally than optimized alternatives.

4. **Vosk/Kaldi-family**
- Strengths: grammar and vocabulary constraints can be explicit.
- Tradeoff: model quality may lag Whisper-class models for noisy speech.

## 3. Recommended Direction
Primary path:
1. standardize on Whisper-compatible engine
2. inject repo vocabulary via `initial_prompt`
3. run multi-pass decode with confidence comparison
4. maintain lexicon growth file under repository control

## 4. Why Context Helps
Repository speech includes out-of-distribution terms. Prompt-priming reduces substitutions for:
- project names
- tool names
- filesystem tokens
- coined terms

## 5. Known Limits
- Prompt bias is not a hard constraint; model can still hallucinate.
- Overly long prompts dilute useful term weighting.
- Homophones still need post-decode checks.

## 6. Operational Recommendation
Keep context payload short, weighted, and versioned; combine with a transcript guard instead of trusting single-pass output.

*The Great Work continues.*
