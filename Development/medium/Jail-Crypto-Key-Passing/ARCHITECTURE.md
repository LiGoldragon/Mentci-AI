# Architecture: Jail Key Resolver and Signing/Push Path

## Components
- `execute` (new)
- `execute` (integration point)
- `execute` (integration point)
- `.mentci/runtime.json` (runtime authority)
- `gopass` backend (secret source)

## Runtime Sources
- `workspaceRoot`
- `policyPath`
- `MENTCI_GOPASS_PREFIX` (optional)
- `MENTCI_GOPASS_GIT_KEY_ENTRY` (optional override)
- `MENTCI_PRIVATE_KEY_PATH` (optional override)

## Deterministic Resolution
Order:
1. Explicit runtime/policy field.
2. Explicit env override.
3. Derived default:
   - entry: `mentci-ai/admin-agent`
   - key path: `<workspaceRoot>/.mentci/keys/git_ed25519`

## Sequence
1. Wrapper command starts (`mentci-commit` or `mentci-jj push/commit`).
2. Resolver ensures key file exists with strict permissions.
3. Resolver ensures public key and git signing config exist.
4. Wrapper executes jj/git operation with `GIT_SSH_COMMAND` referencing key.

## Failure Behavior
- Missing gopass entry: fail fast with non-secret error.
- Invalid key format: fail before commit/push.
- Permission mismatch: repair if possible, else fail.

## Observability
- Emit structured non-secret status:
  - resolved entry path
  - materialized key path
  - operation mode (`created`, `reused`, `repaired`)

## Non-Goals
- Multi-identity key routing.
- Long-term key generation/rotation lifecycle.
