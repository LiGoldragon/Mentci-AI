use anyhow::Result;
use mentci_vcs::{Jujutsu, VCS};
use std::env;
use std::path::PathBuf;

fn main() -> Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        eprintln!("Usage: mentci-vcs <command> [args]");
        std::process::exit(1);
    }

    let repo_root = env::current_dir()?;
    let vcs = Jujutsu::new(repo_root);

    match args[1].as_str() {
        "commit" => {
            if args.len() < 3 {
                eprintln!("Usage: mentci-vcs commit <message>");
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
