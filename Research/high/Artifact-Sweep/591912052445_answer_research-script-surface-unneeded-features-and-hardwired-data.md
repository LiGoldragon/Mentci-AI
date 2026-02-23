# Agent Report

- Chronography: `12.5.24.45 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Artifact-Sweep`
- Title: `research-script-surface-unneeded-features-and-hardwired-data`

## Prompt

research scripts that are not even needed, or have unecessary features or hard-wired data

## Agent Answer

Script-surface audit completed for `execute`.

Primary conclusion:
1. The script layer has several **useful core controls** (`validate_scripts`, `reference_guard`, `session_finalize`, `launcher`), but also includes **legacy or overlapping utilities** with weak runtime ownership.
2. The highest-risk issues are not only dead scripts, but **hard-wired paths/contracts** and **feature duplication**.

## Findings

### High Priority (act soon)

1. `execute`
- Issue: hard-wired stale Rust manifest path: `--manifest-path Components/Cargo.toml`.
- Evidence: script bootstrap argv uses `Components/Cargo.toml`, but repository now builds from `Components/mentci-aid/Cargo.toml`.
- Impact: admin shell bootstrap path can fail or drift from real runtime surface.
- Action: fix path immediately or deprecate until corrected.

2. `execute`
- Issue: overlapping session-state workflow duplicates `session_finalize` responsibilities and writes local mutable file `.mentci/session_state.json`.
- Evidence: `Core/ContextualSessionProtocol.md` marks it as optional, while `session_finalize` + `session_guard` are authoritative closure tools.
- Impact: dual protocol surfaces increase session inconsistency risk.
- Action: mark as legacy/optional explicitly and stop treating as primary flow.

3. `execute` vs `execute`
- Issue: significant overlap in source-manifest parsing, whitelist handling, and mount/materialization behavior.
- Evidence: both implement config discovery from `.attrs.json`/`jailConfig` and use `Core/agent-sources.edn`.
- Impact: duplicated behavior surface and drift risk.
- Action: converge to one canonical implementation path (prefer launcher-owned logic + thin wrappers if needed).

### Medium Priority (simplify next)

1. `execute`
- Issue: optional network-style package discovery (`nix search`, `cargo search`) with low coupling to core stabilization goals.
- Impact: likely non-essential operational surface; adds maintenance/test overhead.
- Action: downgrade to explicit utility status or remove from active sweep/testing expectations.

2. `execute`
- Issue: bespoke bookmark creation UX with random UUID prefix and direct `jj new` orchestration.
- Impact: may conflict with stricter session protocol flow and increase branch/bookmark churn.
- Action: narrow scope or fold into documented `jj` workflow tools.

3. `execute`
- Issue: hard-wired allowlists and heuristic contract checks (`defprotocol`, `impl`, `(main`) rather than policy data loading.
- Impact: guard drift requires script edits for policy evolution.
- Action: externalize allowlist/contract config to EDN policy file.

4. `execute`
- Issue: hard-wired roots and allowlist exceptions in code.
- Impact: policy updates require code changes; exception growth is brittle.
- Action: externalize roots/allowlist patterns to one policy artifact.

### Low Priority (keep as-is for now)

1. `execute`
- Hard-wired constants (`mentci-ai/admin-agent`, `public_key`, `/tmp/mentci_admin_ed25519`) are mostly intentional by design contract.
- Action: retain, but ensure docs consistently describe canonical-only behavior.

2. `execute`
- Appears still wired through `Components/nix/mk-shell.nix` builder path.
- Action: keep until Nix integration is redesigned.

## Recommended Realignment Plan (Scripts Only)

1. **Converge mount logic**
- Choose `launcher` as canonical Sources provisioning engine.
- Convert `sources_mounter` and `sources_remount` into thin wrappers or retire.

2. **Converge session tooling**
- Keep `session_finalize` + `session_guard` as primary.
- Move `session_metadata` to explicit legacy support status (or remove).

3. **De-hardwire policy guards**
- Move `validate_scripts` and `reference_guard` roots/allowlists into a shared EDN policy in `Core/` or `Library/specs/`.

4. **Cull optional utilities from core path**
- Reclassify `tool_discoverer` and `intent` as optional utilities, not core pipeline controls.

## Keep / Simplify / Remove Candidate Table

1. Keep (core): `launcher`, `session_finalize`, `session_guard`, `validate_scripts`, `reference_guard`, `subject_unifier`.
2. Simplify: `sources_mounter`, `sources_remount`, `validate_scripts`, `reference_guard`.
3. Reclassify/optional: `tool_discoverer`, `intent`, `session_metadata`.
4. Fix-or-deprecate: `admin_shell` (stale manifest path).

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
