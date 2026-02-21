use anyhow::{Context, Result};
use dot_parser::ast::{Graph, NodeStmt, Stmt};
use std::collections::{HashMap, HashSet};

pub struct AttractorValidator;

#[derive(Debug)]
pub struct ValidationResult {
    pub is_valid: bool,
    pub errors: Vec<String>,
    pub node_count: usize,
    pub edge_count: usize,
}

impl AttractorValidator {
    pub fn validate(graph_str: &str) -> Result<ValidationResult> {
        let graph = dot_parser::parser::parse(graph_str)
            .map_err(|e| anyhow::anyhow!("DOT Parse Error: {}", e))?;

        let mut errors = Vec::new();
        let mut nodes = HashSet::new();
        let mut edges = Vec::new();
        let mut has_start = false;
        let mut has_exit = false;

        // Iterate over the FIRST graph found (Attractor usually defines one digraph)
        if let Some(g) = graph.first() {
            for stmt in &g.stmts {
                match stmt {
                    Stmt::NodeStmt(n) => {
                        let id = n.node.id.as_str().to_string();
                        nodes.insert(id.clone());
                        if id == "start" { has_start = true; }
                        if id == "exit" { has_exit = true; }
                        
                        // Check for required attributes based on potential type
                        // This is a basic check; real implementation would read attributes
                    }
                    Stmt::EdgeStmt(e) => {
                        // Simplified edge counting
                        edges.push(e);
                    }
                    _ => {}
                }
            }
        }

        if !has_start {
            errors.push("Missing required node: 'start'".to_string());
        }
        if !has_exit {
            errors.push("Missing required node: 'exit'".to_string());
        }

        if nodes.is_empty() {
            errors.push("Graph is empty".to_string());
        }

        Ok(ValidationResult {
            is_valid: errors.is_empty(),
            errors,
            node_count: nodes.len(),
            edge_count: edges.len(),
        })
    }
}
