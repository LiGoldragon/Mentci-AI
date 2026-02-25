pub mod mcp_capnp {
    include!(concat!(env!("OUT_DIR"), "/mcp_capnp.rs"));
}

use mcp_capnp::{structural_edit_request};
use anyhow::{Context, Result};
use std::io::{self, BufRead, Write};
use serde_json::{json, Value};
use ast_grep_core::{AstGrep};
use ast_grep_language::{SupportLang};

struct StructuralEditor;

impl StructuralEditor {
    fn perform_edit(&self, req: structural_edit_request::Reader) -> Result<String> {
        let file_path = req.get_file_path()?.to_string()?;
        let pattern_str = req.get_pattern()?.to_string()?;
        let rewrite_str = req.get_rewrite()?.to_string()?;
        let language_name = req.get_language()?.to_string()?;

        let lang: SupportLang = match language_name.as_str() {
            "rust" => SupportLang::Rust,
            _ => anyhow::bail!("Unsupported language: {}", language_name),
        };

        let source_code = std::fs::read_to_string(&file_path)
            .with_context(|| format!("Failed to read {}", file_path))?;

        let grep = AstGrep::new(&source_code, lang);
        
        // Find all matches
        let matches: Vec<_> = grep.root().find_all(pattern_str.as_str()).collect();
        if matches.is_empty() {
            return Ok("No matches found for the structural pattern.".to_string());
        }

        // Apply rewrites
        let mut new_code = source_code.clone();
        let mut offset = 0isize;

        for m in matches {
            let edit = m.replace(pattern_str.as_str(), rewrite_str.as_str()).context("Failed to generate edit")?;
            let start = edit.position;
            let end = start + edit.deleted_length;
            
            let actual_start = (start as isize + offset) as usize;
            let actual_end = (end as isize + offset) as usize;
            
            let inserted_text = String::from_utf8(edit.inserted_text)?;
            
            new_code.replace_range(actual_start..actual_end, &inserted_text);
            offset += inserted_text.len() as isize - edit.deleted_length as isize;
        }

        std::fs::write(&file_path, new_code)?;
        Ok("Structural edit applied successfully.".to_string())
    }
}

struct McpHandler {
    editor: StructuralEditor,
}

impl McpHandler {
    fn new() -> Self {
        Self {
            editor: StructuralEditor,
        }
    }

    fn handle_request(&self, request: Value) -> Result<Value> {
        let id = request.get("id").cloned();
        let method = request.get("method").and_then(|v| v.as_str()).unwrap_or("");

        match method {
            "initialize" => Ok(json!({
                "jsonrpc": "2.0",
                "id": id,
                "result": {
                    "serverInfo": { "name": "mentci-ast-mcp", "version": "0.1.0" },
                    "capabilities": { "tools": {} }
                }
            })),
            "tools/list" => Ok(json!({
                "jsonrpc": "2.0",
                "id": id,
                "result": {
                    "tools": [
                        {
                            "name": "structural_edit",
                            "description": "Edits a file structurally using ast-grep patterns.",
                            "inputSchema": {
                                "type": "object",
                                "properties": {
                                    "filePath": { "type": "string" },
                                    "language": { "type": "string" },
                                    "pattern": { "type": "string" },
                                    "rewrite": { "type": "string" }
                                },
                                "required": ["filePath", "language", "pattern", "rewrite"]
                            }
                        }
                    ]
                }
            })),
            "tools/call" => {
                let params = request.get("params").context("Missing params")?;
                let name = params.get("name").and_then(|v| v.as_str()).context("Missing tool name")?;
                let args = params.get("arguments").context("Missing arguments")?;

                if name == "structural_edit" {
                    let mut message = capnp::message::Builder::new_default();
                    {
                        let mut req_builder = message.init_root::<structural_edit_request::Builder>();
                        req_builder.set_file_path(args.get("filePath").and_then(|v| v.as_str()).unwrap_or(""));
                        req_builder.set_language(args.get("language").and_then(|v| v.as_str()).unwrap_or("rust"));
                        req_builder.set_pattern(args.get("pattern").and_then(|v| v.as_str()).unwrap_or(""));
                        req_builder.set_rewrite(args.get("rewrite").and_then(|v| v.as_str()).unwrap_or(""));
                    }

                    let req_reader = message.get_root_as_reader::<structural_edit_request::Reader>()?;
                    let result = self.editor.perform_edit(req_reader);

                    let text = match result {
                        Ok(msg) => msg,
                        Err(e) => format!("Error: {}", e),
                    };

                    Ok(json!({
                        "jsonrpc": "2.0",
                        "id": id,
                        "result": {
                            "content": [{ "type": "text", "text": text }]
                        }
                    }))
                } else {
                    anyhow::bail!("Unknown tool: {}", name);
                }
            }
            _ => anyhow::bail!("Unsupported method: {}", method),
        }
    }

    fn run(&self) -> Result<()> {
        let stdin = io::stdin();
        let mut stdout = io::stdout();

        for line in stdin.lock().lines() {
            let line = line?;
            if line.trim().is_empty() { continue; }

            match serde_json::from_str::<Value>(&line) {
                Ok(request) => {
                    if let Ok(response) = self.handle_request(request) {
                        writeln!(stdout, "{}", response.to_string())?;
                    }
                }
                Err(e) => eprintln!("Failed to parse request: {}", e),
            }
        }
        Ok(())
    }
}

fn main() -> Result<()> {
    let handler = McpHandler::new();
    handler.run()
}
