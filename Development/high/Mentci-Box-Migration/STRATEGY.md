# Strategy: Mentci Box Architecture

## 1. Objective
Establish the `mentci-box` as the standard isolation and process boundary for all execution within Mentci-AI, officially phasing out the generic "Nix Jail."

## 2. Core Philosophy
*   **The Actor's Domain:** Each `mentci-aid` daemon supervises a specific domain. To spawn a parallel task, it spawns a child `mentci-aid` inside a new `mentci-box`.
*   **Socket Topology:** Sockets are mounted directly into the `mentci-box` namespaces, allowing parent daemons to communicate with their children without exposing broader network/IPC attack surfaces.
*   **Jujutsu Bindings:** A `mentci-box` is inextricably linked to a specific Jujutsu working copy/bookmark (the "State Separation" rule). The box has read/write permissions only to that specific slice of the file tree.

## 3. Implementation Plan
1.  **Deprecate Nix Jails:** The legacy concept of dropping into a raw Nix shell is replaced by the `mentci-box` binary.
2.  **Socket Mounts:** Update the `Components/mentci-box-lib` to create a Unix Domain Socket directory (`.mentci/run/<box-id>/`) and explicitly bind-mount that socket into the new namespace.
3.  **Daemon Sub-Process Execution:** Update `mentci-aid` so that when it receives a parallel task request, it forks itself inside a new `mentci-box`, establishing the socket connection back to the parent.
