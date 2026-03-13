# maisiliym component anchor

This directory is the Mentci-AI component anchor for the Maisiliym node/network truth source.

## Authority
- Upstream/source of truth: `github:LiGoldragon/maisiliym`
- Domain role: cluster/node/network truth consumed by CriomOS horizon and deployment generation

## Operator rule
Deployment agents should not use ad-hoc local checkout overrides such as `/home/li/git/maisiliym` in this lane. When an override is necessary, use the GitHub flake source form:

```bash
--override-input maisiliym github:LiGoldragon/maisiliym
```

## Notes
This anchor exists so Maisiliym is represented in the root component catalog even when the working tree is not managed as a checked-out submodule here.
