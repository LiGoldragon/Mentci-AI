# Sema Object Style — Rust

This document defines the mandatory Sema object rules for Rust. The rules are
structural. Violations indicate category error, not style.

## Primary Rules

1. **Single Object In/Out**
   Every method accepts at most one explicit object argument and returns exactly
   one object. When multiple Sources or outputs are required, define a new object.

2. **Everything Is an Object**
   Reusable behavior belongs to named types or traits. Free functions exist only
   as orchestration shells in binaries.

3. **Schema Is Sema**
   Transmissible objects are defined in Sema schemas. Wire encodings are incidental.

4. **Single Owner (Actor Model)**
   Every object has a single owner concept (inspired by Erlang/actor model). This
   prevents data racing and double ownership problems at the semantic layer,
   aligning with Rust's borrow checker.

5. **Logic-Data Separation (Sidecar Pattern)**
   Implementation files must not contain hardcoded paths, regexes, or numeric 
   constants. All such data must be loaded from an external structured file
   (Sidecar) or passed in via a typed message. Favor `capnp` and `edn` for
   external data.

6. **Init Envelope Purity (Very High Importance)**
   Runtime launch and initialization configuration must arrive as one Cap'n Proto
   init message object. Environment variables are not used as domain-state inputs.

## Actor-First Concurrency

All multi-step symbolic transformations, long-running orchestrations, and
concurrent task executions must be implemented as supervised actors.

1. **Canonical Framework:** Use the `ractor` actor framework for all supervised
   flows. 
2. **Symbolic Messaging:** Communication between actors must occur via typed
   messages defined as Sema Objects.
3. **Supervision Trees:** Any actor spawning a sub-task must supervise its
   lifecycle, adhering to the "Russian Doll" recursion model.
4. **State Sovereignty:** An actor's internal state is private and only
   modifiable via its explicit message handlers. Shared-state via `Arc<Mutex<T>>`
   is discouraged in favor of message-passing.

## Naming and Ontology

*   `PascalCase` denotes objects (types and traits).
*   `snake_case` denotes flow (methods, fields).
*   Avoid suffixes that restate objecthood (`Object`, `Type`, `Entity`, `Model`).

## Trait-Domain Rule

Any behavior in the semantic domain of an existing trait must be expressed as a
trait implementation. Inherent methods are not used to bypass trait domains.

```rust
use core::str::FromStr;

impl FromStr for Message {
    type Err = MessageParseError;

    fn from_str(input: &str) -> Result<Self, Self::Err> {
        // ...
    }
}
```

## Direction Encodes Action

Prefer `from_*`, `to_*`, `into_*`. Avoid verbs like `read`, `write`, `load`, `save`
when direction already conveys meaning.

## Single Object In/Out Examples

```rust
struct TransformInput { a: A, b: B }
struct TransformOutput { c: C }

impl TransformOutput {
    pub fn from_input(input: TransformInput) -> Self {
        // ...
    }
}
```

## Documentation Protocol

Documentation is impersonal, timeless, and precise. Document only non-boilerplate
behavior.
