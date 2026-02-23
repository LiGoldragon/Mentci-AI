use anyhow::{Result, Context};
use mentci_box_lib::{Sandbox, SandboxConfig};

fn main() -> Result<()> {
    tracing_subscriber::fmt::init();

    let args: Vec<String> = std::env::args().skip(1).collect();
    
    if args.is_empty() || args.contains(&"--help".to_string()) || args.contains(&"-h".to_string()) {
        print_help();
        return Ok(());
    }

    let config = SandboxConfig::from_args(args).context("failed to parse sandbox arguments")?;
    let sandbox = Sandbox::from_config(config);
    
    sandbox.run()
}

fn print_help() {
    println!("mentci-box: Bootstrap and reliable sandbox executable for Mentci-AI.");
    println!("");
    println!("Usage: mentci-box [options] -- <command> [args...]");
    println!("  --workdir <path>      Working directory inside sandbox (default: cwd)");
    println!("  --home <path>         HOME inside sandbox (default: /tmp/mentci-box-home-<pid>)");
    println!("  --bind <src:dst>      Add writable bind mount");
    println!("  --ro-bind <src:dst>   Add read-only bind mount");
    println!("  --setenv <K=V>        Set environment variable inside sandbox");
    println!("  --share-net           Keep host network namespace (default: isolated)");
    println!("");
    println!("Example:");
    println!("  mentci-box --workdir . -- /bin/ls -la");
}
