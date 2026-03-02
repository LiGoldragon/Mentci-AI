use anyhow::{Result, Context};
use cozo::{DbInstance, ScriptMutability};
use std::fs;
use walkdir::WalkDir;
use std::path::Path;

fn main() -> Result<()> {
    // 1. Initialize CozoDB with persistence in .samskara
    if !Path::new(".samskara").exists() {
        fs::create_dir_all(".samskara")?;
    }
    let db = DbInstance::new("sqlite", ".samskara/samskara.db", Default::default()).map_err(|e| anyhow::anyhow!("{:?}", e))?;

    // 2. Define schema (separated)
    let schemas = [
        ":create component { name: String => rating: Int, description: String }",
        ":create agent_skill { name: String => path: String, description: String, push_branch: String }",
        ":create dependency { dependent: String, dependency: String }",
        ":create config_file { path: String => component: String, ext: String }"
    ];
    for s in schemas {
        // Ignore "relation already exists" errors for now by matching on error text or just suppressing
        let _ = db.run_script(s, Default::default(), ScriptMutability::Mutable);
    }

    // Clear old data for a fresh state (in a real daemon, you'd manage state)
    let _ = db.run_script(":rm component { name }", Default::default(), ScriptMutability::Mutable);
    let _ = db.run_script(":rm config_file { path }", Default::default(), ScriptMutability::Mutable);


    // 3. Scan Components/ directory and insert into DB
    let mut insert_script = String::from("?[name, rating, description] <- [\n");
    let components_dir = fs::read_dir("Components").context("Failed to read Components directory")?;
    
    let mut count = 0;
    for entry in components_dir {
        let entry = entry?;
        let path = entry.path();
        if path.is_dir() {
            if let Some(name) = path.file_name().and_then(|n| n.to_str()) {
                // Default rating to 1
                insert_script.push_str(&format!("    [\"{}\", 1, \"Component {}\"],\n", name, name));
                count += 1;
            }
        }
    }
    insert_script.push_str("];\n:put component { name => rating, description }");

    if count > 0 {
        db.run_script(&insert_script, Default::default(), ScriptMutability::Mutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;
    }

    // 4. Scan for EDN and Capnp files and associate them with components
    let mut files_insert = String::from("?[path, component, ext] <- [\n");
    let mut files_count = 0;
    
    for entry in WalkDir::new("Components").into_iter().filter_map(|e| e.ok()) {
        let p = entry.path();
        if p.is_file() {
            if let Some(ext) = p.extension().and_then(|e| e.to_str()) {
                if ext == "edn" || ext == "capnp" {
                    let path_str = p.to_string_lossy().replace("\\", "/");
                    
                    // extract component name (e.g. Components/mentci-stt/...)
                    let parts: Vec<&str> = path_str.split('/').collect();
                    let comp_name = if parts.len() > 1 { parts[1] } else { "unknown" };
                    
                    files_insert.push_str(&format!("    [\"{}\", \"{}\", \"{}\"],\n", path_str, comp_name, ext));
                    files_count += 1;
                }
            }
        }
    }
    for entry in WalkDir::new("Core").into_iter().filter_map(|e| e.ok()) {
        let p = entry.path();
        if p.is_file() {
            if let Some(ext) = p.extension().and_then(|e| e.to_str()) {
                if ext == "edn" {
                    let path_str = p.to_string_lossy().replace("\\", "/");
                    files_insert.push_str(&format!("    [\"{}\", \"Core\", \"{}\"],\n", path_str, ext));
                    files_count += 1;
                }
            }
        }
    }
    
    files_insert.push_str("];\n:put config_file { path => component, ext }");

    if files_count > 0 {
        db.run_script(&files_insert, Default::default(), ScriptMutability::Mutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;
    }

    // 5. Query and print
    println!("--- Components ---");
    let components_res = db.run_script("?[name, rating, description] := *component{name, rating, description}", Default::default(), ScriptMutability::Immutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;
    println!("{:#?}", components_res);

    println!("--- Config & Spec Files ---");
    let files_res = db.run_script("?[path, component, ext] := *config_file{path, component, ext}", Default::default(), ScriptMutability::Immutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;
    println!("{:#?}", files_res);

    Ok(())
}
