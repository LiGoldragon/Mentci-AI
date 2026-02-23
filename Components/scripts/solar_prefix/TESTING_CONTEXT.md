# Testing Context: solar_prefix

## Purpose
Emit the canonical solar response prefix in executable form.

## Command
- `bb Components/scripts/solar_prefix/main.clj`

## Contract
- Prints `solar: <AnnoMundi>.<zodiac>.<degree>.<minute>.<second>`
- Prints one blank line immediately after the solar line.
- Resolves solar time via:
  1. `chronos --format am --precision second`
  2. fallback `cargo run --quiet --manifest-path Components/chronos/Cargo.toml --bin chronos -- --format am --precision second`
