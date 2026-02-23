# Guide: Developing with Mentci-Box

**Status:** Operational
**Subject:** `Mentci-Box-Migration`
**Subject Tier:** `high`

## 1. Introduction
`mentci-box` is the primary high-reliability isolation environment for Mentci-AI development. It ensures **Impeccability** by mounting the project's sensory substrate (`Sources/`) as read-only and isolating the execution from the host system using Bubblewrap.

## 2. Prerequisites
- **Nix** with flake support.
- **Bubblewrap (`bwrap`)** installed on the host system.
- **Jujutsu (`jj`)** for version control.

## 3. Getting Started

### 3.1 Enter the Dev Shell
First, enter the standard Mentci-AI development environment:
```bash
nix develop
```

### 3.2 Initialize the Jail
Use the orchestrator to provision the Sources:
```bash
execute launcher
```

### 3.3 Launch the Mentci-Box
The simplest way to start a sandboxed shell is using the `mentciBoxDefault` package:

```bash
# Using the default wrapper
nix run .#mentciBoxDefault
```

Alternatively, you can provide a custom Cap'n Proto request:
```bash
execute sandbox -- /bin/sh
```

## 4. Operational Protocols

### 4.1 Read-Only Sources
Inside the box, `Sources/` is read-only. You cannot edit files in `Sources/` directly. 
- **Intent:** To modify a component that is currently in `Sources/`, you must promote it to a sub-JJ tree or use the implementation-to-host shipping protocol.

### 4.2 Localized Indexing
When creating new subject directories or artifacts, always ensure an `index.edn` is present and uses **relative paths**. Verify your changes with:
```bash
execute unify
```

### 4.3 Atomic Commits
Every logical change performed within the box must be committed using the `intent:` prefix:
```bash
jj commit -m "intent: <your intention>"
```

## 5. Benefits of the Box
- **No Side Effects:** Your host filesystem is protected from accidental writes.
- **Deterministic Environment:** Every developer (and agent) operates in the same isolated namespace.
- **Fast Bootstrap:** The Rust-based `mentci-box` is significantly faster than previous Babashka-based implementations.

*The Great Work continues.*
