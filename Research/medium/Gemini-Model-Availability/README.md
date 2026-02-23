# Gemini Model Availability & CLI Internal Mapping

## Subject: Gemini 3.1 Pro and GA Status

### Findings (as of 2026-02-23)

#### 1. General Availability (GA) Status
- **Gemini 3 Pro/Flash:** Currently in **Public Preview**. No "non-preview" (GA) stable IDs (e.g., `gemini-3-pro`) are active in the API yet.
- **Gemini 3.1 Pro:** Released as **Public Preview** on February 19, 2026.

#### 2. Model Identifier Mapping (Internal CLI)
Analysis of `gemini-cli` (v0.30.0-preview.3) `gemini.js` reveals the following hardcoded identifiers:
- `PREVIEW_GEMINI_MODEL`: `gemini-3-pro-preview`
- `PREVIEW_GEMINI_3_1_MODEL`: `gemini-3.1-pro-preview`
- `PREVIEW_GEMINI_3_1_CUSTOM_TOOLS_MODEL`: `gemini-3.1-pro-preview-customtools`
- `PREVIEW_GEMINI_FLASH_MODEL`: `gemini-3-flash-preview`
- `DEFAULT_GEMINI_MODEL`: `gemini-2.5-pro` (Current stable fallback)

#### 3. Alias Mappings
The CLI provides convenience aliases which are safer to use during the preview rollout:
- `-m pro`: Points to `gemini-3-pro-preview`.
- `-m flash`: Points to `gemini-3-flash-preview`.
- `-m auto-gemini-3`: Routes to the best available Gemini 3 series model.
- `-m auto`: Standard intelligent routing.

#### 4. Observed API Behavior (Empirical)
Live testing against the Gemini API yielded the following results for a standard developer tier:
- `gemini-3-pro-preview`: **Active** (but prone to `429 RESOURCE_EXHAUSTED` / No capacity).
- `gemini-3-flash-preview`: **Active** (Standard default).
- `gemini-3.1-pro-preview`: **404 NOT_FOUND**. 
  - *Interpretation:* The model ID is correct and recognized by the CLI, but not yet enabled for the specific API key/region or the rollout is still propagating.
- `gemini-3-flash-latest`: **404 NOT_FOUND**.
  - *Note:* While documented in some web sources, the current API endpoint used by the CLI (`cloudcode-pa.googleapis.com`) does not yet recognize this specific alias.

### Recommendations
1. **Prefer Aliases:** Use `gemini -m pro` or `gemini -m flash` instead of full version strings to let the CLI handle version increments.
2. **Avoid Direct 3.1 Calls:** Until the 404 behavior resolves globally, stick to the Gemini 3 Pro Preview for high-reasoning tasks.
3. **Internal Logic:** The CLI already has the code for 3.1; no further Nix packaging is required to support these models once the API enables them for the account.
