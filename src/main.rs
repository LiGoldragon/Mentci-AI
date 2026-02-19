use std::path::PathBuf;
use std::process::Command;
use anyhow::Result;
use std::collections::HashMap;
use tracing::{info, error, debug};

pub mod atom_filesystem_capnp {
    include!(concat!(env!("OUT_DIR"), "/atom_filesystem_capnp.rs"));
}

pub mod mentci_capnp {
    include!(concat!(env!("OUT_DIR"), "/mentci_capnp.rs"));
}

/// # The Execution Environment Abstraction
pub trait ExecutionEnvironment {
    fn read_file(&self, path: &PathBuf) -> Result<String>;
    fn write_file(&self, path: &PathBuf, content: &str) -> Result<()>;
    fn exec_command(&self, command: &str) -> Result<ExecResult>;
    fn working_directory(&self) -> PathBuf;
}

/// # Execution Result
pub struct ExecResult {
    pub stdout: String,
    pub stderr: String,
    pub exit_code: i32,
}

/// # Local Execution Environment
pub struct LocalExecutionEnvironment {
    pub workdir: PathBuf,
}

impl LocalExecutionEnvironment {
    pub fn from_path(path: PathBuf) -> Self {
        Self { workdir: path }
    }
}

impl ExecutionEnvironment for LocalExecutionEnvironment {
    fn read_file(&self, path: &PathBuf) -> Result<String> {
        let full_path = self.workdir.join(path);
        Ok(std::fs::read_to_string(full_path)?)
    }

    fn write_file(&self, path: &PathBuf, content: &str) -> Result<()> {
        let full_path = self.workdir.join(path);
        if let Some(parent) = full_path.parent() {
            std::fs::create_dir_all(parent)?;
        }
        std::fs::write(full_path, content)?;
        Ok(())
    }

    fn exec_command(&self, command: &str) -> Result<ExecResult> {
        let output = Command::new("bash")
            .arg("-c")
            .arg(command)
            .current_dir(&self.workdir)
            .output()?;

        Ok(ExecResult {
            stdout: String::from_utf8_lossy(&output.stdout).to_string(),
            stderr: String::from_utf8_lossy(&output.stderr).to_string(),
            exit_code: output.status.code().unwrap_or(-1),
        })
    }

    fn working_directory(&self) -> PathBuf {
        self.workdir.clone()
    }
}

/// # Task Context
/// Bundles shared state and environment into a single object for handler execution.
/// Adheres to Sema Rule 3: Single Object In/Out.
pub struct TaskContext<'a> {
    pub context: &'a mut Context,
    pub env: &'a dyn ExecutionEnvironment,
}

/// # Handler
pub trait Handler {
    fn execute(&self, task: &mut TaskContext) -> Result<Outcome>;
}

/// # Context
pub struct Context {
    pub values: HashMap<String, String>,
}

impl Context {
    pub fn from_empty() -> Self {
        Self {
            values: HashMap::new(),
        }
    }
}

/// # Outcome
/// Result of executing a node handler, aligned with the Attractor spec.
pub struct Outcome {
    pub status: StageStatus,
    pub notes: Option<String>,
    pub context_updates: HashMap<String, String>,
    pub preferred_label: Option<String>,
}

#[derive(Debug, PartialEq)]
pub enum StageStatus {
    Success,
    PartialSuccess,
    Fail,
    Retry,
    Skipped,
}

/// # Node and Edge definitions for the Graph
pub struct Node {
    pub id: String,
    pub handler: Box<dyn Handler>,
}

pub struct Edge {
    pub from: String,
    pub to: String,
    pub label: Option<String>,
    pub condition: Option<String>,
}

pub struct Graph {
    pub nodes: HashMap<String, Node>,
    pub edges: Vec<Edge>,
    pub start_node: String,
}

/// # Routing Context
/// Bundles parameters for edge selection logic.
pub struct RoutingContext<'a> {
    pub current_node_id: &'a str,
    pub outcome: &'a Outcome,
    pub context: &'a Context,
}

/// # Pipeline Engine
pub struct PipelineEngine {
    pub graph: Graph,
    pub env: Box<dyn ExecutionEnvironment>,
}

impl PipelineEngine {
    pub fn from_graph(graph: Graph, env: Box<dyn ExecutionEnvironment>) -> Self {
        Self { graph, env }
    }

    pub fn run(&mut self) -> Result<()> {
        let mut context = Context::from_empty();
        let mut current_node_id = self.graph.start_node.clone();

        loop {
            let node = self.graph.nodes.get(&current_node_id).ok_or_else(|| anyhow::anyhow!("Node not found"))?;
            info!("Executing node: {}", node.id);

            let mut task = TaskContext {
                context: &mut context,
                env: self.env.as_ref(),
            };

            let outcome = node.handler.execute(&mut task)?;
            info!("Node {} finished with status: {:?}", node.id, outcome.status);

            // Apply context updates
            for (k, v) in outcome.context_updates {
                context.values.insert(k, v);
            }

            if current_node_id == "exit" || outcome.status == StageStatus::Fail {
                break;
            }

            let routing = RoutingContext {
                current_node_id: &current_node_id,
                outcome: &outcome,
                context: &context,
            };

            let next_node_id = self.select_next_node(routing)?;
            current_node_id = next_node_id;
        }

        Ok(())
    }

    fn select_next_node(&self, routing: RoutingContext) -> Result<String> {
        for edge in &self.graph.edges {
            if edge.from == routing.current_node_id {
                if let Some(ref preferred) = routing.outcome.preferred_label {
                    if edge.label.as_ref() == Some(preferred) {
                        return Ok(edge.to.clone());
                    }
                } else {
                    return Ok(edge.to.clone());
                }
            }
        }
        Err(anyhow::anyhow!("No next node found"))
    }
}

/// # Mock Start Handler
struct StartHandler;
impl Handler for StartHandler {
    fn execute(&self, _task: &mut TaskContext) -> Result<Outcome> {
        Ok(Outcome { 
            status: StageStatus::Success, 
            notes: Some("Start node executed successfully".to_string()),
            context_updates: HashMap::new(),
            preferred_label: None 
        })
    }
}

fn main() {
    tracing_subscriber::fmt::init();
    info!("Mentci-AI initialized.");
    
    let workdir = std::env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let env = Box::new(LocalExecutionEnvironment::from_path(workdir));
    
    let mut nodes = HashMap::new();
    nodes.insert("start".to_string(), Node { id: "start".to_string(), handler: Box::new(StartHandler) });
    nodes.insert("exit".to_string(), Node { id: "exit".to_string(), handler: Box::new(StartHandler) });
    
    let edges = vec![
        Edge { from: "start".to_string(), to: "exit".to_string(), label: None, condition: None },
    ];
    
    let graph = Graph {
        nodes,
        edges,
        start_node: "start".to_string(),
    };
    
    let mut engine = PipelineEngine::from_graph(graph, env);
    if let Err(e) = engine.run() {
        error!("Pipeline execution failed: {}", e);
    } else {
        info!("Pipeline execution completed successfully.");
    }
}
