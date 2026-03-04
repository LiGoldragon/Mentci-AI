# Dev into Main Preview Report

## Scope

Assess what rebasing `dev` onto `main` in the primary `Mentci-AI--dev` repository would introduce.

## Baseline

- `dev`: `d65e4b44` — intent: Aggressively shift VersionOne operational init from EDN to Cozo
- `main`: `e8157b0a` — session: Update codex-cli-nix input and verify build

## Commits in `dev` not in `main`

1. `b3cd305a` — add VersionOne samskara-centered architecture workspace
2. `a6090679` — implement and test VersionOne CozoScript examples in samskarad
3. `d18b7f0f` — stop overriding EDITOR in nix dev shell
4. `17c5a9e2` — add aski-cozo bridge report and delimiter search findings
5. `537a742d` — document multi-db handshake outcome model in VersionOne
6. `4d2407c6` — consolidate active research into VersionOne for repo-splitting direction
7. `ecdeb0d6` — parameterize agent DVCS operational intent in VersionOne
8. `d65e4b44` — aggressively shift VersionOne operational init from EDN to Cozo

## Aggregate diffstat (`main..dev`)

- **67 files changed**
- **1357 insertions**, **7 deletions**

Primary areas touched:

- `VersionOne/` workspace (new architecture, protocol, data, research, templates)
- `Components/samskara/` (seed Cozo data and test support)
- `Components/mentci-aid/src/actors/root_guard.edn`
- `Components/nix/dev_shell.nix`
- Research artifacts under `Research/medium/*`
- Aski FS documentation and root contract references

## Build/Environment Validation

- `nix develop -c true` succeeded (dev shell derivation built).

## Risk / Merge Notes

1. **Volume risk:** large content addition under `VersionOne/`; low semantic conflict risk but high review surface.
2. **Data-format direction:** introduces Cozo-first operational-init movement; aligns with latest direction.
3. **Mainline impact:** includes shell/env behavior change (`EDITOR` override removal) that should be explicitly accepted.
4. **Independent AI repo:** `Components/AI` is currently a separate jj+git repo and not part of this `main..dev` diff.

## Recommendation

If rebasing/landing `dev` to `main` now, split review into two lanes:

- **Lane A:** VersionOne architecture + research additions
- **Lane B:** executable/runtime-impacting changes (`samskara`, `dev_shell.nix`, root guard)

This allows faster acceptance of documentation/protocol surfaces while containing operational-risk review.
