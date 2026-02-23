# Aski-FS: Filesystem Specification DSL

**Status:** Draft / Specification
**Date:** 2026-02-20
**Context:** Declarative filesystem topology for Mentci-AI

## 1. Purpose
Aski-FS defines filesystem topology and intent as data. The DSL captures **structure**, **durability**, and **semantic roles** without imperative steps. It is designed for EDN-based tooling, deterministic compilation, and Capnp export.

## 2. Delimiter-Driven Type Sugar
Aski-FS follows the Aski delimiter rules:
- `{}` denotes **struct-like** nodes (directory or file with fields).
- `[]` denotes **enum-like** nodes (variant sets or choices).

A macro expands surface syntax into fully annotated EDN for validation and Capnp emission.

## 3. Core Constructs
### 3.1 Filesystem Root
The root is a single map with a `:root` key.

```edn
{:root {:path "/home/li/git/Mentci-AI"}}
```

### 3.2 Directories
Directories are structs with `:kind :dir` and nested `:children`.

```edn
{:root
 {:path "/repo"
  :kind :dir
  :children
  {"docs" {:kind :dir}
   "src"  {:kind :dir}}}}
```

### 3.3 Files
Files are structs with `:kind :file` and optional attributes.

```edn
{"README.md" {:kind :file
              :role :entrypoint
              :durability :stable}}
```

### 3.4 Durability
Durability follows the project capitalization rules and is explicit:
- `:immutable` (ALL_CAPS)
- `:stable` (PascalCase)
- `:mutable` (lowercase)

### 3.5 Roles
Roles declare meaning, not behavior. Example roles:
- `:entrypoint`
- `:spec`
- `:schema`
- `:workflow`
- `:tooling`

## 4. Canonical EDN Expansion
Surface sugar is normalized into canonical EDN with explicit type tags and ids.

```edn
{:namespace :mentci
 :types
 [{:name :FsNode
   :kind :struct
   :fields [{:id 0 :name :path :type :Text}
            {:id 1 :name :kind :type :FsKind}
            {:id 2 :name :durability :type :Durability}
            {:id 3 :name :role :type :Role}
            {:id 4 :name :children :type [:map :Text :FsNode]}]}
  {:name :FsKind
   :kind :enum
   :values [:dir :file]}
  {:name :Durability
   :kind :enum
   :values [:immutable :stable :mutable]}
  {:name :Role
   :kind :enum
   :values [:entrypoint :spec :schema :workflow :tooling]}]}
```

## 5. Example: Minimal Repo Shape
```edn
{:root
 {:path "/repo"
  :kind :dir
  :children
  {"docs" {:kind :dir
           :children {"specs" {:kind :dir}}}
   "src" {:kind :dir}
   "Components/mentci-aid/Cargo.toml" {:kind :file :role :entrypoint :durability :stable}}}}
```

## 6. Capnp Emission Rules
1. Preserve ordering for field ids and enum ordinals.
2. Enforce constraints in validation (e.g., allowed roles, durability values).
3. Use explicit `:map` representations for child collections.

## 7. Related Specs
- Aski DSL Guidelines: `Library/specs/ASKI_DSL_GUIDELINES.md`
- Aski-Flow: `Library/specs/ASKI_FLOW_DSL.md`

## 8. Nix Namespace Convention
For the `nix/` namespace, each exported attribute maps 1:1 to a file basename.

Convention:
- `nix/<name>.nix` defines attribute `<name>`
- `nix/default.nix` composes the namespace by importing per-file modules

Examples:
- `nix/mentci_ai.nix` -> `mentci_ai`
- `nix/common_packages.nix` -> `common_packages`
- `nix/jail_sources.nix` -> `jail_sources`
