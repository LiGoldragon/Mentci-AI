use anyhow::{Result, Context};
use dot_parser::ast::{Graph as DotGraph, Stmt, NodeStmt, EdgeStmt, AttrStmt, EdgeRHS};
use std::collections::HashMap;

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
        let ast = dot_parser::parse(content)
            .map_err(|e| anyhow::anyhow!("Failed to parse DOT: {}", e))?;
        
        // We assume strictly one graph per file as per spec
        let dot_graph = ast.first().context("Empty DOT file")?;

        let mut nodes = HashMap::new();
        let mut edges = Vec::new();
        let mut graph_attrs = HashMap::new();
        let mut graph_id = None;

        if let DotGraph::DiGraph { id, stmts, .. } = dot_graph {
            if let Some(id) = id {
                graph_id = Some(id.to_string());
            }

            for stmt in stmts {
                match stmt {
                    Stmt::Attribute(AttrStmt::Graph(attrs)) => {
                        for attr in attrs {
                            if let (dot_parser::ast::Id::Plain(k), dot_parser::ast::Id::Escaped(v)) = (&attr.0, &attr.1) {
                                graph_attrs.insert(k.clone(), v.trim_matches('"').to_string());
                            }
                        }
                    }
                    Stmt::Node(NodeStmt { node, attr }) => {
                        let id = node.id.to_string();
                        let mut attrs = HashMap::new();
                        if let Some(attr_list) = attr {
                            for a in attr_list {
                                if let (dot_parser::ast::Id::Plain(k), dot_parser::ast::Id::Escaped(v)) = (&a.0, &a.1) {
                                    attrs.insert(k.clone(), v.trim_matches('"').to_string());
                                }
                            }
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
                    Stmt::Edge(EdgeStmt { node, next, attr }) => {
                        let from = node.id.to_string();
                        // Handle chain: A -> B -> C
                        // `next` is a Vec of EdgeRHS. 
                        // We iterate through them to create edges.
                        // Wait, dot-parser structure might be slightly different.
                        // Let's assume standard simple edges first.
                        
                        let mut current_from = from;
                        
                        for rhs in next {
                            if let EdgeRHS::Node(target_node) = rhs {
                                let to = target_node.id.to_string();
                                
                                let mut attrs = HashMap::new();
                                if let Some(attr_list) = &attr {
                                    for a in attr_list {
                                        if let (dot_parser::ast::Id::Plain(k), dot_parser::ast::Id::Escaped(v)) = (&a.0, &a.1) {
                                            attrs.insert(k.clone(), v.trim_matches('"').to_string());
                                        }
                                    }
                                }

                                let label = attrs.get("label").cloned();
                                let condition = attrs.get("condition").cloned();
                                let weight = attrs.get("weight").and_then(|w| w.parse().ok());

                                edges.push(Edge {
                                    from: current_from.clone(),
                                    to: to.clone(),
                                    label,
                                    condition,
                                    weight,
                                    attributes: attrs,
                                });
                                
                                current_from = to;
                            }
                        }
                    }
                    _ => {} // Ignore subgraphs etc for now
                }
            }
        } else {
             return Err(anyhow::anyhow!("Only directed graphs (digraph) are supported"));
        }

        let goal = graph_attrs.get("goal").cloned();

        Ok(Graph {
            id: graph_id,
            goal,
            nodes,
            edges,
            attributes: graph_attrs,
        })
    }
}