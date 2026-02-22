# Strategy: Jail Crypto Key Passing

## Objective
Design a deterministic, auditable mechanism that loads one Ed25519 private key from `gopass` into the jail workspace so the same key can be used for:
- SSH authentication for git remote push/pull.
- Commit signing (SSH-based signing format).

## Constraints
- Private key must not be committed to VCS.
- Key materialization must happen inside jail/workspace runtime scope only.
- Key source must be deterministic (stable path in `gopass`).
- Existing jail policy/runtime flow (`.mentci/runtime.json`) remains the control plane.

## Deterministic Secret Contract
Canonical gopass entry path:
- `mentci-ai/admin-agent`

Defaults:
- `MENTCI_GOPASS_GIT_KEY_ENTRY=mentci-ai/admin-agent`
- Resolved default entry: `mentci-ai/admin-agent`

Optional companion entries:
- `mentci-ai/admin-agent-public`
- `mentci-ai/admin-agent-known-hosts`

## Materialization Target Contract
Private key destination (workspace-local):
- `<workspaceRoot>/.mentci/keys/git_ed25519`

Public key destination:
- `<workspaceRoot>/.mentci/keys/git_ed25519.pub`

Permissions:
- key dir: `0700`
- private key: `0600`
- public key: `0644`

## Flow Design
1. **Bootstrap Runtime**
- `job/jails bootstrap` produces `.mentci/runtime.json`.
- Runtime file carries `workspaceRoot`, `policyPath`, target bookmark, and key settings.

2. **Key Resolve Step (new script)**
- Proposed entrypoint: `Components/scripts/jail_key_resolver/main.clj`.
- Reads runtime + env.
- Resolves deterministic gopass path.
- Executes: `gopass show --password <entry>`.
- Writes private key to `<workspaceRoot>/.mentci/keys/git_ed25519` with strict permissions.
- Derives/writes public key with `ssh-keygen -y` if not supplied.

3. **Git/JJ Signing + SSH Env Setup**
- Sets workspace-local git config:
  - `git config gpg.format ssh`
  - `git config user.signingkey <workspaceRoot>/.mentci/keys/git_ed25519.pub`
  - `git config commit.gpgsign true`
- Exports SSH command for push/pull:
  - `GIT_SSH_COMMAND=ssh -i <workspaceRoot>/.mentci/keys/git_ed25519 -o IdentitiesOnly=yes -o StrictHostKeyChecking=accept-new`

4. **Commit/Push Integration**
- `mentci-jj` and `mentci-commit` wrappers call key resolver before `jj describe`/`jj git push` when key file missing.
- Key resolver is idempotent and only refreshes when forced.

## Security Model
- Never print secret in logs, errors, or report artifacts.
- Never write key outside `.mentci/keys/`.
- Add deny pattern in scans to fail if key-like content appears under tracked files.
- Optional cleanup mode to shred/unlink key on session completion for ephemeral environments.

## Policy Extension
Extend jail policy JSON with optional crypto block:
- `crypto.enabled`
- `crypto.gopassPrefix`
- `crypto.gopassPrivateEntry`
- `crypto.privateKeyPath`
- `crypto.requireSigning`

If missing, fallback to deterministic defaults.

## Testing Plan
1. Unit tests for path resolution and permission enforcement.
2. Integration test with local `sshd` (adapt `Components/tests/jail_push_ssh.rs`):
- Preload deterministic gopass secret.
- Verify push succeeds via SSH key loaded from `.mentci/keys/git_ed25519`.
3. Signing test:
- Create commit through workflow.
- Verify SSH signature present in resulting git commit metadata.

## Rollout
Phase 1:
- Implement resolver script + runtime/policy parsing.
- Integrate with `mentci-commit` and `mentci-jj push` paths.

Phase 2:
- Enable commit signing defaults in workspace-local git config.
- Add integration tests.

Phase 3:
- Enforce key resolver gate for remote push and signed commit flows.

## Acceptance Criteria
- Single deterministic gopass entry can provision the jail key.
- SSH push works using `.mentci/keys/git_ed25519`.
- Commit signing is enabled with SSH format and the same key identity.
- No private key material is tracked or leaked in repository artifacts.
