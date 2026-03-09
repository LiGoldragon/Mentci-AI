use mentci_cozo::{
    compile_handoff_to_cozoscript, compile_message_intent_to_cozoscript, HandoffContract,
    LogicalConstraint, MessageIntentContract, TechnicalEnvelope,
};

#[test]
fn compiles_common_handoff_contract_to_cozoscript() {
    let contract = HandoffContract {
        goal: "enforce runtime bookmark contract".into(),
        scope: vec!["Core/VersionControlProtocol.md".into()],
        out_of_scope: vec!["binary artifacts".into()],
        constraints: vec![
            LogicalConstraint {
                field: "targetBookmark".into(),
                op: "eq".into(),
                value: "MENTCI_TARGET_BOOKMARK".into(),
            },
            LogicalConstraint {
                field: "jjRevset".into(),
                op: "not_contains".into(),
                value: "all()".into(),
            },
        ],
        verification_commands: vec!["jj status".into(), "jj bookmark list".into()],
    };

    let script = compile_handoff_to_cozoscript("task-123", &contract).expect("compile");

    assert!(script.contains("*handoff_goal"));
    assert!(script.contains("*handoff_constraint"));
    assert!(script.contains("targetBookmark"));
    assert!(script.contains("not_contains"));
}

#[test]
fn rejects_unsupported_constraint_operator() {
    let contract = HandoffContract {
        goal: "test".into(),
        scope: vec![],
        out_of_scope: vec![],
        constraints: vec![LogicalConstraint {
            field: "x".into(),
            op: "regex".into(),
            value: ".*".into(),
        }],
        verification_commands: vec![],
    };

    let err = compile_handoff_to_cozoscript("task-123", &contract).unwrap_err();
    assert!(err.contains("unsupported operator"));
}

#[test]
fn compiles_message_intent_without_technical_section() {
    let contract = MessageIntentContract {
        goals: vec![
            "stabilize parallel subagent dispatch".into(),
            "keep user-visible prompts concise".into(),
        ],
        restraints: vec![
            "do not use unbounded jj revsets".into(),
            "do not mutate extension internals".into(),
        ],
        scope: vec!["/home/li/git/Mentci-AI--dev/.pi/skills/subagent-driven-development/SKILL.md".into()],
        out_of_scope: vec!["provider API key rotation".into()],
        verification_commands: vec!["cargo test -p mentci-cozo".into()],
        technical: None,
    };

    let script = compile_message_intent_to_cozoscript("msg-100", &contract).expect("compile");

    assert!(script.contains("::create message_goal"));
    assert!(script.contains("::create message_restraint"));
    assert!(script.contains("*message_goal{message_id: \"msg-100\""));
    assert!(script.contains("*message_restraint{message_id: \"msg-100\""));
    assert!(!script.contains("*handoff_constraint"));
}

#[test]
fn compiles_message_intent_with_technical_constraints() {
    let contract = MessageIntentContract {
        goals: vec!["enforce branch target contract".into()],
        restraints: vec!["no all() in revsets".into()],
        scope: vec![],
        out_of_scope: vec![],
        verification_commands: vec!["execute jj-preflight --json".into()],
        technical: Some(TechnicalEnvelope {
            constraints: vec![LogicalConstraint {
                field: "targetBookmark".into(),
                op: "eq".into(),
                value: "MENTCI_TARGET_BOOKMARK".into(),
            }],
        }),
    };

    let script = compile_message_intent_to_cozoscript("msg-200", &contract).expect("compile");

    assert!(script.contains("::create handoff_constraint"));
    assert!(script.contains("*handoff_constraint{message_id: \"msg-200\""));
}

#[test]
fn rejects_blank_message_id() {
    let contract = MessageIntentContract {
        goals: vec!["x".into()],
        restraints: vec![],
        scope: vec![],
        out_of_scope: vec![],
        verification_commands: vec![],
        technical: None,
    };

    let err = compile_message_intent_to_cozoscript("   ", &contract).unwrap_err();
    assert!(err.contains("message_id cannot be empty"));
}

#[test]
fn rejects_empty_goals() {
    let contract = MessageIntentContract {
        goals: vec![],
        restraints: vec![],
        scope: vec![],
        out_of_scope: vec![],
        verification_commands: vec![],
        technical: None,
    };

    let err = compile_message_intent_to_cozoscript("msg-300", &contract).unwrap_err();
    assert!(err.contains("goals cannot be empty"));
}
