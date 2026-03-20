# Jailed Devshell User-Config Environment — Implementation Notes

## Scope

Implemented a first-pass environment realization path that keeps `Components/mentci-user` as the runtime authority for devshell user environment shaping.

## Implemented

- Added a component-local profile sidecar:
  - `Components/mentci-user/data/user-profile.json`
- Extended `mentci-user` runtime support to load:
  - required env vars from setup config,
  - component-local profile env entries,
  - local user overrides,
  - shell variables,
  - PATH additions with deduplication.
- Added `mentci-user-run` wrapper in the Nix package set so child commands can be launched through:
  - `mentci-user exec "$MENTCI_USER_SETUP_BIN" -- <command>`
- Pointed the devshell at the local `Components/mentci-user` source and exported:
  - `MENTCI_USER_SETUP_BIN`
  - `MENTCI_USER_PROFILE_JSON`

## Verification

Verified with:

- `cargo test` in `Components/mentci-user`
- `nix develop . --command env | rg '^MENTCI_USER_'`
- `nix develop . --command bash -lc 'mentci-user-run env | ...'`

Observed result:

- devshell exports `MENTCI_USER_PROFILE_ACTIVE=1`
- `mentci-user-run` realizes the configured environment for child commands
- PATH additions are not duplicated on nested re-entry

## Deliberate First-Pass Constraints

- Profile authority is currently JSON, not EDN/Cap'n Proto synchronized profile data.
- The setup binary remains unchanged; profile discovery is runtime-path based via `MENTCI_USER_PROFILE_JSON` or adjacent file lookup.
- This preserves a working path without introducing a new setup-bin sync tool in the same prompt.

## Follow-Up

- Add proper text/binary synchronization for the richer profile contract.
- If jail isolation must materialize secrets without outer-shell hydration, define an explicit in-jail secret access contract rather than relying on outer environment state.
