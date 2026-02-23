# Gemini CLI Update Research

## Subject: Update to v0.30.0-preview.3

### Findings
- Updated `gemini-cli` from `v0.29.5` to `v0.30.0-preview.3`.
- Verified SRI hash for the new version: `sha256-px3ksJZzPQLE5idW7W9egbFPS2so3bZhnMaUpQGouI4=`.
- The new version supports the Gemini 3 series, including `gemini-3.1-pro-preview`.

### Model Support
- `gemini-3-flash` (Fast, optimized for coding).
- `gemini-3.1-pro-preview` (Advanced reasoning).

### Usage
To use the new models:
```bash
gemini -m gemini-3.1-pro-preview
```

The CLI now defaults to intelligent routing if "Preview Features" are enabled in `/settings`.
