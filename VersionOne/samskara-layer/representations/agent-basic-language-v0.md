# Agent Basic Language (ABL/0) — Symbolic Communication Seed

Status: draft
Purpose: save the full symbolic mirror and derive a smaller language for agent-to-agent communication.

## 1) Saved mirror (basis artifact)

This is the preserved symbolic mirror used as the source for ABL/0.

```clj
::frame<mentci.answer.cozo-spec.v1>{

  *(topic :cozo/spec-authority)
  *(intent :confirm+qualify+propose)

  *{claims
    ?[id type polarity proposition] := [
      [spec-feasible   :feasibility  :positive   :cozo-spec-in-cozo-aligned]
      [cozo-strength   :capability   :positive   :cozo-provides-relations+facts+rules]
      [cozo-limit      :boundary     :bounded    :cozo-weaker-than-rust-hard-enum-typing]
      [enum-strategy   :directive    :prescribe  :enum-like-as-token+allowed-set+validation-query]
      [target-shape    :directive    :prescribe  :canonical-clause-with-polarity+modality]
      [integration     :architecture :prescribe  :rust-compiler-guard-not-sole-semantic-authority]
    ]
  }

  *{schema
    *(:relation message-clause){
      :keys  [id message-id]
      :attrs [kind polarity modality target rationale]
    }

    *(:relation allowed-polarity){ :domain [:positive :negative] }
    *(:relation allowed-modality){ :domain [:forbidden :avoid :discourage :require] }
  }

  *{validation
    ?[id reason] := [
      ?[id] <- *(:relation message-clause{ id id polarity p })
      ![ok] <- *(:relation allowed-polarity{ value p })
      => [id :invalid-polarity]
    ]

    ?[id reason] := [
      ?[id] <- *(:relation message-clause{ id id modality m })
      ![ok] <- *(:relation allowed-modality{ value m })
      => [id :invalid-modality]
    ]
  }

  *{decision
    <architecture.layering>[
      :semantic-authority.primary :cozo
      :compiler-guard.secondary   :rust/mentci-cozo
      :ui-surface.primary         :intent-clauses
      :technical-constraints      :attach-later
    ]
  }
}
```

---

## 2) ABL/0: smaller language concept for agent communication

ABL/0 keeps five blocks only:
- `meta`
- `goals`
- `restraints`
- `checks`
- `decision`

### 2.1 Canonical message form

```clj
::msg<agent.intent.v0>{
  *{meta
    *(from :planner)
    *(to :task)
    *(about :parallel-subagent-reliability)
  }

  *{goals
    ?[id polarity modality proposition] := [
      [g1 :positive :require :return-non-empty-agent-output]
      [g2 :positive :require :preserve-user-visible-intent-shape]
    ]
  }

  *{restraints
    ?[id polarity modality proposition] := [
      [r1 :negative :forbidden :use-unbounded-jj-revsets]
      [r2 :negative :avoid :modify-runtime-extension-internals]
    ]
  }

  *{checks
    ?[id cmd expected] := [
      [c1 :cargo-test-mentci-cozo :pass]
      [c2 :workspace-diagnostics :zero-errors]
    ]
  }

  *{decision
    <state.outcome>[
      :status :proposed
      :next   :execute
    ]
  }
}
```

### 2.2 Field semantics

- `polarity`: `:positive | :negative`
- `modality`:
  - positive lane: `:require | :prefer`
  - negative lane: `:forbidden | :avoid | :discourage`
- `proposition`: symbol path (not sentence text) expressing normalized intent.

---

## 3) Syntax decisions and interpretation rules

### 3.1 `::msg<type>{ ... }`
Decision: use `::` for envelope declarations and `<...>` for semantic type.
Interpretation: one parse root per message; `<type>` is the decoder key.

### 3.2 `*{subtype ... }`
Decision: subtype scoping uses `*{}`.
Interpretation: keys inside inherit subtype context; avoids prefix repetition (`claim/x`, `claim/y`).

### 3.3 `*(key :value)`
Decision: atomic assertions use `*()`.
Interpretation: single edge/fact in AST form.

### 3.4 `?[cols...] := [[...]]`
Decision: tabular clause payloads use query-like tuple notation.
Interpretation: fixed column order contract; each row is one intent atom.

### 3.5 `:[keyword]` symbols over free strings
Decision: use keywords/symbols as primary meaning units.
Interpretation: symbols are canonical vocabulary and machine-comparable; natural language is projection-only.

### 3.6 `<packet.name>[ ... ]`
Decision: packet vectors model compact state envelopes.
Interpretation: order-insensitive key/value pairs unless explicitly declared ordered.

### 3.7 `![]` (negated membership checks)
Decision: reserve `!` for violation-detection contexts.
Interpretation: `![ok] <- allowed-set` means lookup guard failed in current derivation branch.

### 3.8 `=> [ ... ]` derivation output
Decision: emit normalized result tuples with `=>`.
Interpretation: right side is produced fact/event for downstream agents or UI.

---

## 4) Minimal parser contract (ABL/0)

A valid ABL/0 message must have:
1. Exactly one `::msg<...>{...}` root.
2. Required subtypes: `meta`, `goals`, `restraints`.
3. At least one goal row.
4. For each restraint row: `polarity` must be `:negative`.
5. `modality` must belong to allowed vocabulary.

Violation projection:
- `:invalid-root`
- `:missing-required-subtype`
- `:empty-goals`
- `:invalid-polarity`
- `:invalid-modality`

---

## 5) Why this is smaller than markdown

- Removes rhetorical glue words.
- Reuses subtype context (`*{claims ...}` style) instead of repeating prefixes.
- Encodes intent as symbols, not full prose sentences.
- Keeps technical detail attachable in separate packets.
