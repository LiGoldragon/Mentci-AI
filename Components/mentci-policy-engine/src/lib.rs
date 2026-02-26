#[derive(Debug, Clone, PartialEq, Eq)]
pub enum WorkflowPhase {
    Brainstorm,
    Plan,
    Execute,
    Verify,
    Review,
    Finish,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum DecisionKind {
    Allow,
    Warn,
    Block,
}

#[derive(Debug, Clone)]
pub struct PolicyInput {
    pub phase: WorkflowPhase,
    pub tool_name: String,
    pub file_path: Option<String>,
    pub has_plan_artifact: bool,
}

#[derive(Debug, Clone)]
pub struct PolicyDecision {
    pub kind: DecisionKind,
    pub code: &'static str,
    pub reason: String,
}

pub fn evaluate(input: &PolicyInput) -> PolicyDecision {
    let is_write = input.tool_name == "write"
        || input.tool_name == "edit"
        || input.tool_name == "structural_edit";

    if !is_write {
        return PolicyDecision {
            kind: DecisionKind::Allow,
            code: "ALLOW_NON_WRITE",
            reason: "non-write tool call".to_string(),
        };
    }

    if matches!(input.phase, WorkflowPhase::Brainstorm | WorkflowPhase::Plan) {
        if let Some(path) = &input.file_path {
            let is_docs = path.starts_with("docs/plans/")
                || path.starts_with("Research/")
                || path.starts_with("Development/");

            if !is_docs {
                return PolicyDecision {
                    kind: DecisionKind::Block,
                    code: "BLOCK_WRITE_THINKING_PHASE",
                    reason: format!(
                        "write blocked in {:?} phase for path {} (allowed: docs/plans/, Research/, Development/)",
                        input.phase, path
                    ),
                };
            }
        }
    }

    if matches!(input.phase, WorkflowPhase::Execute) && !input.has_plan_artifact {
        return PolicyDecision {
            kind: DecisionKind::Warn,
            code: "WARN_EXECUTE_WITHOUT_PLAN_ARTIFACT",
            reason: "execute-phase write without linked implementation artifact".to_string(),
        };
    }

    PolicyDecision {
        kind: DecisionKind::Allow,
        code: "ALLOW_WRITE",
        reason: "write allowed by current workflow policy".to_string(),
    }
}
