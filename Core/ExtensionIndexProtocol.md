# Core Extension Index Protocol

## Objective
Keep `Core/` self-contained and stable while allowing controlled extension through optional index files in a small, standardized location set.

## Principle
- Core authority remains minimal and universal.
- Optional extension data lives outside core logic and is discovered via index files.
- Adding extension capability should not require editing multiple Core files.

## Standard Location Contract
Canonical file: `Core/ExtensionIndexLocations.edn`

Current standard locations:
1. `Core/EXTENSIONS.edn`
2. `Library/extensions/index.edn`
3. `Components/index.edn`
4. `Components/extensions/index.edn`

Only these locations are considered by default extension discovery.

## Runtime Behavior
- Extension loading is optional.
- Missing index files are non-fatal.
- Discovery returns ordered `found` paths and preserves contract order.

## Tooling
- Resolver script: `execute extension-index`
- Output is EDN map with `:locations` and `:found`.

## Governance
- Add/remove standard locations by editing only `Core/ExtensionIndexLocations.edn`.
- Core protocol documents remain stable; extension growth happens in indexed files.
