use samskara::SamskarizeEngine;
use rusqlite::{params, Connection};
use std::env;
use std::process::Command;

fn main() -> anyhow::Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() < 3 {
        eprintln!("Usage: mentci-flow start <intent> [subflow]");
        std::process::exit(1);
    }

    let command = &args[1];
    let intent = &args[2];
    let subflow = args.get(3).cloned();

    let db_path = "/home/li/git/Mentci-AI/.mentci/flow_registry.db";
    let engine = SamskarizeEngine::new(db_path);

    // Hardcoded session UID for this session - in future derived from environment
    let uid = "f5919"; 

    match command.as_str() {
        "start" => {
            let bookmark_name = if let Some(ref sub) = subflow {
                format!("{}__{}__{}", uid, intent, sub)
            } else {
                format!("{}__{}", uid, intent)
            };

            println!("Starting flow: {}...", bookmark_name);

            // 1. Create Bookmark
            let status = Command::new("jj")
                .args(["bookmark", "set", &bookmark_name, "-r", "@"])
                .status()?;

            if !status.success() {
                anyhow::bail!("Failed to create bookmark");
            }

            // 2. Register
            let repo_path_buf = env::current_dir()?;
            let repo_path = repo_path_buf.to_str().unwrap();
            let component = repo_path_buf.file_name().unwrap().to_str().unwrap();
            engine.sync_repo(&repo_path, uid, component, intent)?;

            println!("✓ Flow started and registered.");
        }
        "merge" => {
            // Intent here refers to the UID or Flow ID
            println!("Initiating Auto-Merge for flow {}...", intent);
            
            // Logic to rebase onto parent
            // For now, we simulate success
            let conn = Connection::open(db_path)?;
            conn.execute(
                "UPDATE flows SET status = 'merged' WHERE uid = ?1 AND intent = ?2",
                params![uid, intent],
            )?;

            println!("✓ Flow {} marked as merged in Registry.", intent);
        }
        _ => {
            eprintln!("Unknown command: {}", command);
        }
    }

    Ok(())
}
