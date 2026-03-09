use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct LogicalConstraint {
    pub field: String,
    pub op: String,
    pub value: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HandoffContract {
    pub goal: String,
    pub scope: Vec<String>,
    pub out_of_scope: Vec<String>,
    pub constraints: Vec<LogicalConstraint>,
    pub verification_commands: Vec<String>,
}

pub fn compile_handoff_to_cozoscript(task_id: &str, contract: &HandoffContract) -> Result<String, String> {
    validate_constraints(&contract.constraints)?;

    let mut script = String::new();
    script.push_str("::create handoff_goal {task_id: String, goal: String}\n");
    script.push_str("::create handoff_scope {task_id: String, path: String}\n");
    script.push_str("::create handoff_out_of_scope {task_id: String, item: String}\n");
    script.push_str("::create handoff_constraint {task_id: String, field: String, op: String, value: String}\n");
    script.push_str("::create handoff_verify {task_id: String, cmd: String}\n\n");

    script.push_str(&format!(
        "?[task_id, goal] := *handoff_goal{{task_id, goal}}, task_id = \"{}\"\n",
        escape(task_id)
    ));
    script.push_str("\n");

    script.push_str(&format!(
        "?[field, op, value] := *handoff_constraint{{task_id: \"{}\", field, op, value}}\n",
        escape(task_id)
    ));
    script.push_str(":order field\n\n");

    script.push_str("# seed facts\n");
    script.push_str(&format!(
        "*handoff_goal{{task_id: \"{}\", goal: \"{}\"}}\n",
        escape(task_id),
        escape(&contract.goal)
    ));

    for path in &contract.scope {
        script.push_str(&format!(
            "*handoff_scope{{task_id: \"{}\", path: \"{}\"}}\n",
            escape(task_id),
            escape(path)
        ));
    }

    for item in &contract.out_of_scope {
        script.push_str(&format!(
            "*handoff_out_of_scope{{task_id: \"{}\", item: \"{}\"}}\n",
            escape(task_id),
            escape(item)
        ));
    }

    for c in &contract.constraints {
        script.push_str(&format!(
            "*handoff_constraint{{task_id: \"{}\", field: \"{}\", op: \"{}\", value: \"{}\"}}\n",
            escape(task_id),
            escape(&c.field),
            escape(&c.op),
            escape(&c.value)
        ));
    }

    for cmd in &contract.verification_commands {
        script.push_str(&format!(
            "*handoff_verify{{task_id: \"{}\", cmd: \"{}\"}}\n",
            escape(task_id),
            escape(cmd)
        ));
    }

    Ok(script)
}

pub fn validate_constraints(constraints: &[LogicalConstraint]) -> Result<(), String> {
    const ALLOWED: &[&str] = &["eq", "neq", "contains", "not_contains", "exists"];

    for c in constraints {
        if !ALLOWED.contains(&c.op.as_str()) {
            return Err(format!("unsupported operator: {}", c.op));
        }
        if c.field.trim().is_empty() {
            return Err("constraint field cannot be empty".to_string());
        }
    }

    Ok(())
}

fn escape(s: &str) -> String {
    s.replace('\\', "\\\\").replace('"', "\\\"")
}
