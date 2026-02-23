use anyhow::{Result, Context};
use mentci_box_lib::{Sandbox, BoxRequest};
use std::path::PathBuf;

fn main() -> Result<()> {
    tracing_subscriber::fmt::init();

    let args: Vec<String> = std::env::args().skip(1).collect();
    
    if args.is_empty() || args.contains(&"--help".to_string()) || args.contains(&"-h".to_string()) {
        print_help();
        return Ok(());
    }

    let request_path = PathBuf::from(&args[0]);
    let request = BoxRequest::from_capnp(&request_path).context("failed to load box request from capnp")?;
    let config = request.to_sandbox_config()?;
    let sandbox = Sandbox::from_config(config);
    
    sandbox.run()
}

fn print_help() {
    println!("mentci-box: Bootstrap and reliable sandbox executable for Mentci-AI.");
    println!("");
    println!("Usage: mentci-box <request.capnp>");
    println!("");
    println!("Example:");
    println!("  mentci-box request.capnp");
}
