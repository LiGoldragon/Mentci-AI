# Nix signing key terminology and bootstrap guidance

## Intent
Record the exact Nix terminology and command surfaces needed for the CriomOS signing-key bootstrap redesign and the associated truth-model cleanup.

## Canonical Nix terminology
Official Nix terminology distinguishes between:

- `secret-key-files`
  - files containing the **secret/private signing keys** used by the Nix daemon to sign locally built paths
- `trusted-public-keys`
  - the **public keys** trusted when verifying signatures on substitutable paths

This means the current CriomOS naming is misleading:
- `preCriad` is acting as a **Nix secret signing key file path**
- `nixPreCriome` is acting as a **public signing key payload/truth field**

## Canonical commands
### Generate a binary-cache keypair
```sh
nix-store --generate-binary-cache-key <key-name> <secret-key-file> <public-key-file>
```

### Generate only a secret key (new CLI)
```sh
nix key generate-secret --key-name cache.example.org-1 > ./secret-key
```

### Derive public key from secret key
```sh
nix key convert-secret-to-public < ./secret-key
```

## NixOS/Nix guidance implication
The official docs expect signing keys to be generated and placed at the configured path before the cache/signing service is usable. Missing keys produce daemon/service errors; there is no documented default behavior where NixOS auto-generates signing keys at boot.

Therefore, a CriomOS service that generates the secret key if missing is a **custom bootstrap policy**, not a built-in NixOS behavior.

## Design implication for CriomOS
A good naming split is:
- runtime secret file path:
  - `nixSigningSecretKeyFile`
- runtime world-readable public key file:
  - `nixSigningPublicKeyFile`
- truth/public-key field in Criosphere / Maisiliym:
  - `nixSigningPublicKey` (or `nixTrustedPublicKey`)

The private key must remain runtime-local and never become part of Maisiliym truth.
Only the public key should round-trip back into node truth.

## Sources
- Nix `nix.conf` manual: `secret-key-files`, `trusted-public-keys`, `require-sigs`
- Nix `nix-store --generate-binary-cache-key`
- Nix `nix key generate-secret`
- Nix `nix key convert-secret-to-public`
- nix.dev binary cache setup tutorial
