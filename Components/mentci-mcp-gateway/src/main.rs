use mentci_policy_engine::{evaluate, PolicyInput, WorkflowPhase};

fn main() {
    // Draft bootstrap only. Full MCP gateway wiring follows in next iteration.
    let decision = evaluate(&PolicyInput {
        phase: WorkflowPhase::Brainstorm,
        tool_name: "write".to_string(),
        file_path: Some("Components/example/src/lib.rs".to_string()),
        has_plan_artifact: false,
    });

    println!(
        "mentci-mcp-gateway draft: decision={} reason={}",
        decision.code, decision.reason
    );
}
