use rusqlite::{params, Connection};
use std::env;
use std::process::Command;

fn main() -> anyhow::Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        eprintln!("Usage: samskara-verify <intent>");
        std::process::exit(1);
    }

    let intent = &args[1];
    let db_path = "/home/li/git/Mentci-AI/.mentci/flow_registry.db";
    let uid = "f5919";

    println!("Verifying flow {}...", intent);

    // 1. Run Check
    let status = Command::new("cargo")
        .arg("check")
        .status()?;

    if status.success() {
        println!("✓ Cargo check passed.");
        
        let conn = Connection::open(db_path)?;
        conn.execute(
            "UPDATE flows SET status = 'success' WHERE uid = ?1 AND intent = ?2",
            params![uid, intent],
        )?;

        println!("✓ Flow {} marked as success in Registry.", intent);
    } else {
        anyhow::bail!("Cargo check failed. Flow remains in-progress.");
    }

    Ok(())
}
