# Subagents Loader Fix: Use TS Entrypoint Path Instead of Package Root

- **Solar:** `5919.12.18.52.43`
- **Programming:** `3wyybz4j`

## Symptom
Pi reported extension load failure:
- `Cannot find module '/home/li/.pi/pi-source/node_modules/@oh-my-pi/subagents'`

## Root cause
`@oh-my-pi/subagents` is published as an OMP plugin package and does not expose a root `pi` extension entry in `package.json` (`pi.extensions` absent at package root). 

Its actual extension factory is at:
- `@oh-my-pi/subagents/tools/index.ts`

## Fix
Updated wrapper extension argument to point directly at the extension file:
- from: `.../node_modules/@oh-my-pi/subagents`
- to: `.../node_modules/@oh-my-pi/subagents/tools/index.ts`

Updated Nix check to assert the TS entrypoint file exists.

## Validation
- `nix build .#checks.x86_64-linux.piWithExtensions` passes after fix.
