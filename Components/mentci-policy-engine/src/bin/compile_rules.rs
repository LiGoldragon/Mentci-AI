use std::fs;
use std::env;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let workspace_root = env::current_dir()?;
    let skills_dir = workspace_root.join(".pi").join("skills");
    
    if !skills_dir.exists() {
        println!("No skills directory found at {}", skills_dir.display());
        return Ok(());
    }

    for entry in fs::read_dir(&skills_dir)? {
        let entry = entry?;
        let path = entry.path();
        
        if path.is_dir() {
            let skill_name = path.file_name().unwrap().to_string_lossy();
            let skill_md = path.join("SKILL.md");
            let runtime_edn = path.join("runtime.edn");
            
            if !skill_md.exists() {
                continue;
            }
            
            let mut compiled_rules = String::new();
            
            // 1. Read Core Authority
            let core_text = fs::read_to_string(&skill_md)?;
            compiled_rules.push_str(&format!("# TIER 1: CORE AUTHORITY ({})\n", skill_name));
            compiled_rules.push_str(&core_text);
            compiled_rules.push_str("\n\n");
            
            // 2. Read Adaptive Heuristics if they exist
            if runtime_edn.exists() {
                let heuristics_text = fs::read_to_string(&runtime_edn)?;
                if !heuristics_text.trim().is_empty() {
                    compiled_rules.push_str(&format!("# TIER 3: ADAPTIVE HEURISTICS ({})\n", skill_name));
                    compiled_rules.push_str(&heuristics_text);
                    compiled_rules.push_str("\n\n");
                }
            }
            
            let out_path = path.join("absolute_rules.md");
            fs::write(&out_path, compiled_rules)?;
            println!("Compiled Absolute Rules for skill: {}", skill_name);
        }
    }
    
    Ok(())
}
