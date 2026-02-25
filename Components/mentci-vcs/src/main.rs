use anyhow::{Result, Context};
use mentci_vcs::{Jujutsu, VCS};
use std::env;

fn main() -> Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        eprintln!("Usage: mentci-jj <command> [args]");
        eprintln!("Commands: intent-start <task>, status, diff, commit <message>");
        std::process::exit(1);
    }

    let repo_root = env::current_dir()?;
    let vcs = Jujutsu::new(repo_root);

    match args[1].as_str() {
        "intent-start" => {
            if args.len() < 3 {
                eprintln!("Usage: mentci-jj intent-start <task>");
                std::process::exit(1);
            }
            let task = &args[2];
            println!("Starting intent: {}", task);
            vcs.new_with_message(&format!("intent: {}", task))?;
        }
        "commit" => {
            if args.len() < 3 {
                eprintln!("Usage: mentci-jj commit <message>");
                std::process::exit(1);
            }
            vcs.commit(&args[2])?;
        }
        "status" => {
            let out = vcs.status()?;
            println!("{}", out);
        }
        "diff" => {
            let out = vcs.diff()?;
            println!("{}", out);
        }
        _ => {
            eprintln!("Unknown command: {}", args[1]);
            std::process::exit(1);
        }
    }

    Ok(())
}
