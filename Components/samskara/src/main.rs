use samskara::SamskarizeEngine;
use std::env;

fn main() -> anyhow::Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() < 4 {
        eprintln!("Usage: samskara <repo_path> <uid> <intent>");
        std::process::exit(1);
    }

    let repo_path = &args[1];
    let uid = &args[2];
    let intent = &args[3];

    // Note: In Level 5, we externalize data. Path to DB should be in config.
    // For now, using the canonical relative path.
    // Note: In Level 5, we externalize data.
    // Using the absolute anchor for the migration.
    let db_path = "/home/li/git/Mentci-AI/.mentci/flow_registry.db";
    let engine = SamskarizeEngine::new(db_path);

    println!("Samskarizing {} [UID: {}, Intent: {}]...", repo_path, uid, intent);
    engine.sync_repo(repo_path, uid, "unknown", intent)?;
    println!("✓ Flow registered in SQLite.");

    Ok(())
}
