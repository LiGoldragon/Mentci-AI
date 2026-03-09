# mentci-cozo (MVP)

Light MVP component for expressing agent handoff constraints as compact CozoScript.

## Message-first API (UI-visible first)

Primary modeling now starts from markdown-visible intent fields:
- `goals[]`
- `restraints[]`
- `scope[]`
- `out_of_scope[]`
- `verification_commands[]`

Technical constraints are optional and can be attached later.

## Rust APIs
- `compile_message_intent_to_cozoscript(message_id, &MessageIntentContract)`
- `compile_handoff_to_cozoscript(task_id, &HandoffContract)` (backward-compatible)
- `validate_constraints(&[LogicalConstraint])`

## Message-first example

```rust
use mentci_cozo::{
    compile_message_intent_to_cozoscript, LogicalConstraint, MessageIntentContract,
    TechnicalEnvelope,
};

let contract = MessageIntentContract {
    goals: vec!["stabilize parallel subagent orchestration".into()],
    restraints: vec!["avoid unbounded jj revsets".into()],
    scope: vec!["/home/li/git/Mentci-AI--dev/.pi/skills/subagent-driven-development/SKILL.md".into()],
    out_of_scope: vec!["provider key management".into()],
    verification_commands: vec!["cargo test -p mentci-cozo".into()],
    technical: Some(TechnicalEnvelope {
        constraints: vec![LogicalConstraint {
            field: "targetBookmark".into(),
            op: "eq".into(),
            value: "MENTCI_TARGET_BOOKMARK".into(),
        }],
    }),
};

let script = compile_message_intent_to_cozoscript("msg-001", &contract)?;
```

## Supported operators (MVP)
- `eq`
- `neq`
- `contains`
- `not_contains`
- `exists`
