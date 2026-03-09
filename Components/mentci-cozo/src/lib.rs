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

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TechnicalEnvelope {
    pub constraints: Vec<LogicalConstraint>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MessageIntentContract {
    pub goals: Vec<String>,
    pub restraints: Vec<String>,
    pub scope: Vec<String>,
    pub out_of_scope: Vec<String>,
    pub verification_commands: Vec<String>,
    pub technical: Option<TechnicalEnvelope>,
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

pub fn compile_message_intent_to_cozoscript(
    message_id: &str,
    contract: &MessageIntentContract,
) -> Result<String, String> {
    if message_id.trim().is_empty() {
        return Err("message_id cannot be empty".to_string());
    }

    if contract.goals.is_empty() || contract.goals.iter().all(|g| g.trim().is_empty()) {
        return Err("goals cannot be empty".to_string());
    }

    if let Some(technical) = &contract.technical {
        validate_constraints(&technical.constraints)?;
    }

    let mut script = String::new();
    script.push_str("::create message_goal {message_id: String, goal: String}\n");
    script.push_str("::create message_restraint {message_id: String, restraint: String}\n");
    script.push_str("::create message_scope {message_id: String, path: String}\n");
    script.push_str("::create message_out_of_scope {message_id: String, item: String}\n");
    script.push_str("::create message_verify {message_id: String, cmd: String}\n");

    if contract.technical.is_some() {
        script.push_str("::create handoff_constraint {message_id: String, field: String, op: String, value: String}\n");
    }

    script.push_str("\n# seed facts\n");

    for goal in &contract.goals {
        if goal.trim().is_empty() {
            continue;
        }
        script.push_str(&format!(
            "*message_goal{{message_id: \"{}\", goal: \"{}\"}}\n",
            escape(message_id),
            escape(goal)
        ));
    }

    for restraint in &contract.restraints {
        if restraint.trim().is_empty() {
            continue;
        }
        script.push_str(&format!(
            "*message_restraint{{message_id: \"{}\", restraint: \"{}\"}}\n",
            escape(message_id),
            escape(restraint)
        ));
    }

    for path in &contract.scope {
        if path.trim().is_empty() {
            continue;
        }
        script.push_str(&format!(
            "*message_scope{{message_id: \"{}\", path: \"{}\"}}\n",
            escape(message_id),
            escape(path)
        ));
    }

    for item in &contract.out_of_scope {
        if item.trim().is_empty() {
            continue;
        }
        script.push_str(&format!(
            "*message_out_of_scope{{message_id: \"{}\", item: \"{}\"}}\n",
            escape(message_id),
            escape(item)
        ));
    }

    for cmd in &contract.verification_commands {
        if cmd.trim().is_empty() {
            continue;
        }
        script.push_str(&format!(
            "*message_verify{{message_id: \"{}\", cmd: \"{}\"}}\n",
            escape(message_id),
            escape(cmd)
        ));
    }

    if let Some(technical) = &contract.technical {
        for c in &technical.constraints {
            script.push_str(&format!(
                "*handoff_constraint{{message_id: \"{}\", field: \"{}\", op: \"{}\", value: \"{}\"}}\n",
                escape(message_id),
                escape(&c.field),
                escape(&c.op),
                escape(&c.value)
            ));
        }
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
