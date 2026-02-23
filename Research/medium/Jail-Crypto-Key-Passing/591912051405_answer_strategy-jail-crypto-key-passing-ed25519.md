# Agent Report

- Chronography: `12.5.14.5 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Jail-Crypto-Key-Passing`
- Title: `strategy-jail-crypto-key-passing-ed25519`

## Prompt

strategize a crypto-key passing mechanism for the jail which uses gopass to output a deterministic key path's secret (the private key) to the jail's private key location. I want to use a single key for signing commits and pushing to git remotes (ssh), and focus on new short algo (ed25519)


## Agent Answer

Created a dedicated strategy package for deterministic jail key passing with gopass and single-key Ed25519 usage for both SSH transport and commit signing.

Added:
- `Development/Jail-Crypto-Key-Passing/Strategy.md`
- `Development/Jail-Crypto-Key-Passing/ArchitectureMap.md`
- `Development/Jail-Crypto-Key-Passing/Roadmap.md`
- `Research/Jail-Crypto-Key-Passing/index.edn`

Core design choices:
- Deterministic gopass entry default: `mentci-ai/admin-agent`
- Private key target: `<workspaceRoot>/.mentci/keys/git_ed25519`
- Strict permissions: key dir `0700`, private key `0600`
- Same key used for:
  - `GIT_SSH_COMMAND` SSH remote auth
  - SSH-based commit signing (`git config gpg.format ssh` + signing key pub path)
- New proposed resolver script integration point:
  - `Components/scripts/jail_key_resolver/main.clj`

Also included rollout phases, policy extension shape, failure behavior, and test plan aligned with existing jail push SSH harness.


## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
