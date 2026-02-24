# Google Antigravity Research (as used by `pi`)

## Research Date
- Gregorian: 2026-02-24
- Solar: 5919.12.06.49.08

## Executive Summary
`google-antigravity` in `pi` is an OAuth-backed Google provider lane that shares Gemini CLI-style transport but diverges in endpoint, OAuth client profile, request metadata, and default model preference. In Google's `gemini-cli` repository, Antigravity is documented primarily as a supported IDE environment, not as a separately documented public Gemini API product.

## Primary Findings
1. In `pi`, `google-antigravity` is configured as a first-class provider identifier with OAuth onboarding and refresh handling.
2. `pi` routes antigravity traffic to `https://daily-cloudcode-pa.sandbox.googleapis.com`, while `google-gemini-cli` uses `https://cloudcode-pa.googleapis.com`.
3. Antigravity OAuth uses its own client ID/secret/redirect/scopes and an internal default project fallback (`rising-fact-p41fc`).
4. In request shaping, antigravity mode sets `request_type` to `agent` and user agent to `antigravity`.
5. `pi` default model preference differs by lane: `google-gemini-cli -> gemini-2.5-pro`; `google-antigravity -> gemini-3-pro-high`.
6. In Google's upstream `gemini-cli`, Antigravity appears prominently as an IDE integration target (with editor aliases like `agy` / `antigravity`) and environment detection hooks.

## Source Evidence
- `pi` auth constants and antigravity OAuth flow:
  - `/nix/store/nx7899p1qgirxahlh7l2swvf3v1f7wf7-source/src/auth.rs`
- `pi` Gemini provider endpoint selection and request shaping:
  - `/nix/store/nx7899p1qgirxahlh7l2swvf3v1f7wf7-source/src/providers/gemini.rs`
- `pi` provider metadata/defaults/models:
  - `/nix/store/nx7899p1qgirxahlh7l2swvf3v1f7wf7-source/src/provider_metadata.rs`
  - `/nix/store/nx7899p1qgirxahlh7l2swvf3v1f7wf7-source/src/app.rs`
  - `/nix/store/nx7899p1qgirxahlh7l2swvf3v1f7wf7-source/src/models.rs`
- Google Gemini CLI upstream (HEAD at research time: `d143a83d5b7ee487dcdf56276fe03b595215fb50`):
  - `docs/ide-integration/index.md`
  - `packages/core/src/ide/detect-ide.ts`
  - `packages/core/src/utils/editor.ts`
  - `packages/cli/src/ui/utils/terminalSetup.ts`

## Risk and Stability Assessment
- Endpoint naming (`sandbox`) plus specialized OAuth profile implies higher change risk than the standard `google` API-key lane.
- Public contractual API documentation for antigravity as a general GA provider lane is not clear from examined upstream docs.
- Operational recommendation: prefer `google` (API key) or `google-gemini-cli` for stable production behavior unless antigravity-specific behavior is required.

## Consolidation Map (Existing Gemini-Related Subjects)
- `Research/high/Gemini-Cli-Update`
- `Research/high/Gemini-Tui-Completion`
- `Development/medium/Gemini-Model-Availability`

## Persistence Rule (Applied)
For Gemini-related user questions, save an indexed research artifact under this subject with source-backed findings and cross-links to affected Gemini sub-subjects.
