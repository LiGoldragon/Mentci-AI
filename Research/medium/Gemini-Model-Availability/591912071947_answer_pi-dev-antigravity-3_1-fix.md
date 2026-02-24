# Research Artifact: pi-dev Antigravity Gemini 3.1 Fix

- Solar: ♓︎ 7° 19' 47" | 5919 AM
- Subject: `Gemini-Model-Availability`
- Title: `pi-dev-antigravity-3.1-fix`
- Status: `validated`

## 1. Intent
Create a dedicated `pi-dev` package and validate a practical fix for Antigravity model deprecation (`Gemini 3 Pro` -> `Gemini 3.1 Pro`).

## 2. Implementation
1. Added `Components/nix/pi-dev.nix` as a dedicated package variant.
2. Patched `pi` model catalog in the build Source to replace Antigravity model identifiers:
   - `gemini-3-pro-high` -> `gemini-3.1-pro-high`
   - `gemini-3-pro-low` -> `gemini-3.1-pro-low`
3. Patched coding-agent default model resolver for `google-antigravity` to `gemini-3.1-pro-high`.
4. Disabled `packages/ai` build-time model regeneration in `pi-dev` so patched model IDs are preserved in offline/Nix build contexts.
5. Exposed the new package and app in flake outputs (`packages.piDev`, `apps.pi-dev`).

## 3. Validation
- `nix flake show` evaluates successfully with the new package.
- `nix build .#piDev` succeeds.
- `./result/bin/pi --list-models antigravity` now lists:
  - `google-antigravity/gemini-3.1-pro-high`
  - `google-antigravity/gemini-3.1-pro-low`
- Runtime check succeeds:
  - `./result/bin/pi --model google-antigravity/gemini-3.1-pro-high -p "say ok"` -> `ok`

## 4. Outcome
`pi-dev` provides a working path for Antigravity users affected by Gemini 3 Pro removal, without requiring immediate upstream release changes.
