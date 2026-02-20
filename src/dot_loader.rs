use anyhow::{Result, Context};
use dot_parser::ast::Graph as AstGraph;
use dot_parser::canonical::Graph as CanonicalGraph;
use std::collections::HashMap;
use std::convert::TryFrom;

#[derive(Debug, Clone)]
pub struct Node {
    pub id: String,
    pub label: Option<String>,
    pub prompt: Option<String>,
    pub shape: Option<String>, // Defines Handler Type
    pub node_type: Option<String>, // Explicit override
    pub attributes: HashMap<String, String>,
}

#[derive(Debug, Clone)]
pub struct Edge {
    pub from: String,
    pub to: String,
    pub label: Option<String>,
    pub condition: Option<String>,
    pub weight: Option<i32>,
    pub attributes: HashMap<String, String>,
}

#[derive(Debug, Clone)]
pub struct Graph {
    pub id: Option<String>,
    pub goal: Option<String>,
    pub nodes: HashMap<String, Node>,
    pub edges: Vec<Edge>,
    pub attributes: HashMap<String, String>,
}

pub struct DotLoader;

impl DotLoader {
    pub fn parse(content: &str) -> Result<Graph> {
        // Parse into AST
        let ast_graph = AstGraph::try_from(content)
            .map_err(|e| anyhow::anyhow!("Failed to parse DOT: {}", e))?;

        // Convert to Canonical Graph for easier processing
        let canonical_graph = CanonicalGraph::from(ast_graph);

        let mut nodes = HashMap::new();
        let mut edges = Vec::new();
        let mut graph_attrs = HashMap::new();
        let graph_id = Some(canonical_graph.name.to_string());

        // Process Attributes (Graph level)
        // Canonical graph might flatten attributes differently.
        // Let's assume we can access them if needed, but for now focus on nodes/edges.

        // Process Nodes
        for c_node in canonical_graph.nodes.0 {
            let id = c_node.id.to_string();
            let mut attrs = HashMap::new();
            
            for (k, v) in c_node.attr.0 {
                attrs.insert(k.to_string(), v.to_string().trim_matches('"').to_string());
            }

            let label = attrs.get("label").cloned();
            let prompt = attrs.get("prompt").cloned();
            let shape = attrs.get("shape").cloned();
            let node_type = attrs.get("type").cloned();

            nodes.insert(id.clone(), Node {
                id,
                label,
                prompt,
                shape,
                node_type,
                attributes: attrs,
            });
        }

        // Process Edges
        for c_edge in canonical_graph.edges.0 {
            let from = c_edge.from.to_string();
            let to = c_edge.to.to_string();
            
            let mut attrs = HashMap::new();
            for (k, v) in c_edge.attr.0 {
                attrs.insert(k.to_string(), v.to_string().trim_matches('"').to_string());
            }

            let label = attrs.get("label").cloned();
            let condition = attrs.get("condition").cloned();
            let weight = attrs.get("weight").and_then(|w| w.parse().ok());

            edges.push(Edge {
                from,
                to,
                label,
                condition,
                weight,
                attributes: attrs,
            });
        }
        
        // Extract goal from attributes if possible (Graph attributes might be tricky in canonical)
        // For now, ignore graph attrs or try to find them if canonical exposes them.
        // It seems canonical might not expose top-level graph attributes directly in a simple map?
        // We will skip graph attrs for now unless critical.
        let goal = None;

        Ok(Graph {
            id: graph_id,
            goal,
            nodes,
            edges,
            attributes: graph_attrs,
        })
    }
}