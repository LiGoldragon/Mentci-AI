pub mod atom_filesystem_capnp {
    include!(concat!(env!("OUT_DIR"), "/atom_filesystem_capnp.rs"));
}
pub mod mentci_capnp {
    include!(concat!(env!("OUT_DIR"), "/mentci_capnp.rs"));
}

use mentci_capnp::dig_request;
use anyhow::Result;

pub struct DigEngine;

impl DigEngine {
    pub fn new() -> Self {
        Self
    }

    pub fn execute(&self, req: dig_request::Reader) -> Result<String> {
        let targets = req.get_targets()?;
        let query = req.get_query()?.to_string()?;
        let strategy = req.get_strategy()?;

        // Placeholder for the actual parsing logic
        // This will be replaced by tree-sitter / ast-grep / jsonpath execution
        println!("Digging using strategy {:?} with query: {}", strategy, query);
        for target in targets.iter() {
            println!("Target: {}", target?.to_string()?);
        }

        Ok("Dig complete".to_string())
    }
}

fn main() -> Result<()> {
    // This binary will act as an MCP endpoint or a direct CLI 
    // taking a DigRequest Cap'n Proto envelope from stdin.
    println!("mentci-dig component scaffolded.");
    Ok(())
}
