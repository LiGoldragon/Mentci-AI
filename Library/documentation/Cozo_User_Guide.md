# Cozo User Guide (Mentci-AI)

**Status:** Active  
**Audience:** Mentci developers building Rust + Cap'n Proto + Cozo data pipelines.  
**Scope:** Practical CozoScript usage, transaction/mutation patterns, and Mentci authority boundaries.

---

## 1. What Cozo Is

Cozo is a **transactional relational database** with a **Datalog-style query language** called **CozoScript**.

For Mentci work, think of Cozo as:
- a declarative logic/query substrate for structured facts,
- strong at joins, recursion, graph-shaped data, and projections,
- embeddable in Rust (`cozo` crate).

Canonical upstream docs:
- https://docs.cozodb.org/en/latest/
- https://docs.cozodb.org/en/latest/tutorial.html
- https://docs.cozodb.org/en/latest/queries.html
- https://docs.cozodb.org/en/latest/stored.html
- https://docs.rs/cozo

---

## 2. CozoScript Basics (Quick Reference)

A script is made of named rules. The rule named `?` is the returned relation.

### 2.1 Rule kinds
- `:=` inline rule (pure query logic)
- `<~` fixed rule (utility/algorithm invocation)
- `<-` constant-rule sugar

### 2.2 Stored relation access
- Positional: `*rel[a, b, c]`
- Named: `*rel{colA: a, colB: b}`

### 2.3 Set semantics
Relations are sets (deduplicated rows). If a derivation yields the same row multiple times, only one is retained.

---

## 3. Minimal Working Examples

### 3.1 Create relation + insert data
```cozoscript
?[cluster, name, species, size, trust] <- [
  ["maisiliym", "ouranos", "edge", 3, 2],
  ["maisiliym", "klio", "center", 2, 3]
]
:create node {cluster: String, name: String => species: String, size: Int, trust: Int}
```

### 3.2 Query primary data
```cozoscript
?[cluster, name, species, size, trust] := *node{cluster, name, species, size, trust}
:order cluster, name
```

### 3.3 Derive convenience fields
```cozoscript
?[name, is_max, is_trusted] :=
  *node{name, size, trust},
  is_max = (size == 3),
  is_trusted = (trust >= 2)
:order name
```

---

## 4. Mutations and Transactions

Common mutation options:
- `:create`
- `:replace`
- `:put` (upsert)
- `:rm`
- `:insert` (error if key exists)
- `:update`
- `:delete`
- `:ensure` / `:ensure_not` (commit-time consistency guards)

Each script runs as a transaction. You can chain multiple query blocks:

```cozoscript
{ ?[c,n,s,z,t] <- [["maisiliym","ouranos","edge",3,2]]; :put node {cluster: c, name: n => species: s, size: z, trust: t} }
{ ?[name] := *node{name, size}, size == 3; :assert some }
```

If any block errors, the whole script rolls back.

---

## 5. Can Cozo Derive Data with Programmed Logic?

Yes.

Cozo is very suitable for deriving data such as:
- size predicates (`isMax` / `sizedAtLeast`),
- species flags,
- trust thresholds,
- graph reachability and recursive projections.

Cozo can also run triggers via `::set_triggers`.

However, triggers are not a substitute for cryptographic authority boundaries.

---

## 6. Mentci Authority Boundary (Critical)

For Mentci/CriomOS-style architecture, use this split:

1. **Cap'n Proto = primary data only**  
   Keep only canonical facts (e.g., species, size, trust, machine facts).

2. **Rust = authoritative derivation + verification**  
   Rust checker/engine computes derived fields and performs signature/quorum checks.

3. **Cozo = storage + declarative projection layer**  
   Cozo may re-derive convenience/query views, but does not replace cryptographic trust authority.

This keeps signatures and state acceptance deterministic and auditable.

---

## 7. Primary vs Derived Data Policy (CriomOS alignment)

### Primary (canonical) examples
- `species`
- `size`
- `trust`
- `machine` facts
- user/domain/node declarations

### Derived (computed) examples
- `isMax` / `sizedAtLeast.max`
- `typeIs.*`
- role predicates (`isBuilder`, `isDispatcher`, `isNixCache`)
- convenience projection fields for Nix/consumer output

Policy:
- store/sign the **primary** layer as authority,
- compute **derived** layer via deterministic logic,
- treat derived fields as reproducible projections, not source truth.

---

## 8. Rust Embedding Notes

Basic embedding shape (from `cozo` crate docs):

```rust
use cozo::*;

let db = DbInstance::new("mem", "", Default::default()).unwrap();
let script = "?[a] := a in [1, 2, 3]";
let result = db.run_script(script, Default::default(), ScriptMutability::Immutable).unwrap();
```

Feature notes:
- `storage-sqlite` and `graph-algo` are common defaults.
- `storage-rocksdb` is relevant for higher concurrency/perf needs.

See: https://docs.rs/cozo

---

## 9. Practical Recommendation for New Mentci Components

When introducing a Cozo-backed component:
1. Define primary schema first (Cap'n Proto / canonical structs).
2. Implement deterministic derivation in Rust.
3. Add Cozo relations for storage/query surfaces.
4. Keep mutation rules explicit (`:put`, `:ensure`, `:assert`).
5. Only expose derived fields at boundaries where needed (e.g., JSON for Nix).

This preserves Logic/Data separation and clean trust boundaries while retaining Cozo's expressive query power.
