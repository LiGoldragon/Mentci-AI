# Pi-with-extensions missing pi-ai root files

## Summary
- pi-with-extensions fails because the pi-ai package expects root-level files (bedrock-provider.js, oauth.js) that are not installed by the Nix packaging.
- The pi-ai `package.json` exports `./bedrock-provider` and `./oauth` mapped to root files, but the current Nix install only copies `dist/` and `package.json`.

## Evidence
- Runtime error: `Cannot find module .../pi-ai/bedrock-provider.js`.
- Nix store inspection shows only `dist/` and `package.json` under `@mariozechner/pi-ai`.
- `package.json` exports:
  - `"./bedrock-provider": { "import": "./bedrock-provider.js" }`
  - `"./oauth": { "import": "./oauth.js" }`
- `dist/` contains `bedrock-provider.js` and `oauth.js`, but they are not present at the package root.

## Root Cause
`Components/nix/pi.nix` copies only `packages/ai/dist` and `packages/ai/package.json`, omitting the root-level files required by `pi-ai`'s export map.

## Fix Strategy
Copy the root-level exports from `packages/ai/dist` into the pi-ai package root during `installPhase`:
- `bedrock-provider.js`, `bedrock-provider.d.ts`
- `oauth.js`, `oauth.d.ts`
