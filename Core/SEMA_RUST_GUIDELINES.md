# Sema Object Style — Rust

This document defines the mandatory Sema object rules for Rust. The rules are
structural. Violations indicate category error, not style.

## Primary Rules

1. **Single Object In/Out**
   Every method accepts at most one explicit object argument and returns exactly
   one object. When multiple inputs or outputs are required, define a new object.

2. **Everything Is an Object**
   Reusable behavior belongs to named types or traits. Free functions exist only
   as orchestration shells in binaries.

3. **Schema Is Sema**
   Transmissible objects are defined in Sema schemas. Wire encodings are incidental.

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
