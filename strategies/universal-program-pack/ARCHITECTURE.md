# Architecture: Universal Program Pack

## Root Contract
A host repository mounts or vendors one root directory:
- `ProgramPack/`

## Internal Shape
- `ProgramPack/core/`
- `ProgramPack/programs/`
- `ProgramPack/strategies/`
- `ProgramPack/templates/`
- `ProgramPack/schemas/`

## Host Extension Contract
Each host repository declares one local extension root:
- `LocalPack/`

Local shape:
- `LocalPack/programs/`
- `LocalPack/strategies/`
- `LocalPack/templates/`

## Discovery Model
A small resolver loads assets in this order:
1. `LocalPack/**` (host override/extension)
2. `ProgramPack/**` (universal default)

This preserves universal defaults while allowing local specialization.

## Path Manifest
Both pack roots expose a canonical manifest:
- `ProgramPack/manifest.edn`
- `LocalPack/manifest.edn`

Manifest duties:
- Declare available programs and strategies.
- Declare stable IDs and canonical file paths.
- Declare compatibility/version metadata.
