# Mentci Box Isolation Protocol

- **Status:** Architectural Draft
- **Weight:** High (Author Directed)

## 1. Objective
Establish the isolation boundaries and operational constraints for Mentci boxes to ensure impeccability and prevent accidental damage to source substrates.

## 2. OS-Level Sandboxing
The primary isolation layer resides in an OS contract, currently implemented as a NixOS module.

### 2.1 Read-Only Sources
*   All source substrate (`Sources/`) is mounted read-only within the principal Mentci box.
*   Modification of the source tree is only possible through explicit commit/shipping protocols (`jj`).

### 2.2 Functional Restrictions
*   The Mentci admin does not have full system capabilities by default; it is restricted by the OS contract.
*   Every Mentci box is governed by a specific set of rules defining its permissible actions.

## 3. Renaming Economy
With fully specified Aski/Sema, renaming becomes "cheap." A single symbol change at the schema level results in a codebase-wide rename during translation, enabling fast architectural iteration without losing semantic history.
