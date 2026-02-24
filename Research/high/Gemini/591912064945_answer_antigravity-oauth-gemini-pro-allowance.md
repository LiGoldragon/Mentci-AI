# Antigravity OAuth and Gemini Pro Allowance

## Research Date
- Gregorian: 2026-02-24
- Solar: 5919.12.06.49.45

## Question
Will `google-antigravity` in `pi` give allowance from a Gemini Pro subscription through OAuth?

## Short Answer
Likely yes for Gemini CLI-style OAuth quotas, but not guaranteed as a formal public contract specific to `google-antigravity`.

## Evidence
1. Gemini CLI official docs state Gemini CLI has OAuth usage allowances for individual Google accounts and higher limits for Google AI Pro/Ultra and paid Code Assist plans.
2. The same docs state workspace/web app plans do not apply to API usage that powers Gemini CLI.
3. In `pi`, `google-antigravity` and `google-gemini-cli` both run through Google CLI OAuth-style auth and transport, with project-scoped OAuth payloads (`{token, projectId}`) and Cloud Code Assist endpoints.

## Interpretation
- If your account has Google AI Pro/Ultra and is set up for Gemini CLI OAuth, allowance behavior should generally follow Gemini CLI OAuth quota policy.
- `google-antigravity` is an internal/specialized lane in `pi` (sandbox endpoint and special request metadata), so exact entitlement behavior may differ and should be verified empirically.

## Practical Guidance
- Use `/login google-antigravity` and test with `/quota` in `pi` to confirm effective limits.
- If limits do not reflect expected subscription tier, test `/login google-gemini-cli` and compare.
- For contractual/stable billing semantics, use standard Google API key provider (`google`) where quotas are explicitly tied to API billing.

## Sources
- Gemini CLI authentication: https://github.com/google-gemini/gemini-cli/blob/main/docs/cli/authentication.md
- Gemini CLI quota and pricing: https://github.com/google-gemini/gemini-cli/blob/main/docs/cli/quota-and-pricing.md
- Gemini CLI docs website: https://gemini-cli.xyz/docs/quota-and-pricing/
- `pi` auth/provider implementation:
  - `/nix/store/nx7899p1qgirxahlh7l2swvf3v1f7wf7-source/src/auth.rs`
  - `/nix/store/nx7899p1qgirxahlh7l2swvf3v1f7wf7-source/src/providers/gemini.rs`
