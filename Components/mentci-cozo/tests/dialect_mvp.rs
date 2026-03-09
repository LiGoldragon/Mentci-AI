use mentci_cozo::{compile_handoff_to_cozoscript, HandoffContract, LogicalConstraint};

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
