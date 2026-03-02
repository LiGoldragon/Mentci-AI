# Strategy: User Configuration Architecture

## 1. Goal
Establish a standardized, secure, and git-ignored method for managing user-specific settings and secrets (e.g., API keys, gopass paths) within the Mentci-AI ecosystem.

## 2. Component: `mentci-user`
A dedicated Rust library (`Components/mentci-user`) is established to serve as the unified interface for retrieving user secrets. 

### Core Features:
*   **Structured Sidecar:** Reads user configuration from `.mentci/user.json` (git-ignored).
*   **Pluggable Methods:** Supports multiple secret retrieval methods:
    *   `gopass`: Executes `gopass show <path>` to retrieve the secret.
    *   `env`: Reads the secret from a local environment variable.
    *   `literal`: Reads the secret directly from the JSON (useful for non-sensitive local flags).
*   **Cap'n Proto Integration:** The schema is defined in `Components/schema/user.capnp` to ensure future compatibility with the SEMA binary object format.

## 3. Implementation: `mentci-stt` Integration
The `mentci-stt` utility now leverages `mentci-user` to automatically retrieve the `GEMINI_API_KEY`. This removes the need for the user to manually export environment variables before running transcription.

## 4. Nix Integration
Both `mentci-stt` and `mentci-user` (as a utility/library) are packaged via Nix and exposed in the `devShell`. This ensures a seamless, "batteries-included" developer experience upon entering the repository environment.

## 5. Security Protocol
The file `/.mentci/` is already part of the repository's `.gitignore`. All user-specific secrets and configurations must reside within this directory to prevent accidental cryptographic leakage into the public history.
