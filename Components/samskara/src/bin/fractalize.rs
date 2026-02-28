use std::process::Command;
use std::fs;
use std::path::Path;
use samskara::SamskarizeEngine;

fn main() -> anyhow::Result<()> {
    let components_dir = Path::new("Components");
    let fractal_root = Path::new("/home/li/git/mentci-fractal/components");
    fs::create_dir_all(fractal_root)?;

    let db_path = "/home/li/git/Mentci-AI/.mentci/flow_registry.db";
    let engine = SamskarizeEngine::new(db_path);

    let uid = "f5919"; // Session UID

    for entry in fs::read_dir(components_dir)? {
        let entry = entry?;
        let path = entry.path();
        if path.is_dir() {
            let component_name = path.file_name().unwrap().to_str().unwrap();
            let target_dir = fractal_root.join(component_name);

            println!("Fractalizing {} -> {:?}...", component_name, target_dir);

            // 1. Copy or Sync
            if !target_dir.exists() {
                fs::create_dir_all(&target_dir)?;
            }
            
            // Using rsync to exclude .jj and other noise
            let status = Command::new("rsync")
                .args(["-a", "--exclude", ".jj", "--exclude", "target"])
                .arg(format!("{}/", path.display()))
                .arg(format!("{}/", target_dir.display()))
                .status()?;

            if !status.success() {
                eprintln!("Failed to sync {}", component_name);
                continue;
            }

            // 2. Initialize jj
            if !target_dir.join(".jj").exists() {
                Command::new("jj")
                    .arg("git")
                    .arg("init")
                    .arg(".")
                    .current_dir(&target_dir)
                    .status()?;
            }

            // 3. Register in Saṃskāra
            engine.sync_repo(target_dir.to_str().unwrap(), uid, component_name, "initial-fractal")?;
        }
    }

    println!("✓ All components fractalized and registered.");
    Ok(())
}
