# Testing Context: jail_key_setup

## Scope
Interactive setup for generating an Ed25519 keypair and storing it in deterministic `gopass` entries used by jail key materialization workflows.

## Manual Checks
- `bb Components/scripts/jail_key_setup/main.clj --help`
- `bb Components/scripts/jail_key_setup/main.clj`

## Expected Behavior
- Prompts for key path, comment, and gopass entry names.
- Generates an Ed25519 keypair with `ssh-keygen`.
- Stores private key in `gopass` entry `mentci-ai/admin-agent` by default.
- Optionally stores public key in `gopass`.
- Optionally removes temporary local key files after import.
