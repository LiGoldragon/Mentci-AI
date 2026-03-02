use anyhow::{Result, Context};
use cozo::{DbInstance, ScriptMutability};
use std::fs;

fn main() -> Result<()> {
    // 1. Initialize CozoDB
    let db = DbInstance::new("mem", "", Default::default()).map_err(|e| anyhow::anyhow!("{:?}", e))?;

    // 2. Define schema (separated)
    let schemas = [
        ":create component { name: String => rating: Int, description: String }",
        ":create agent_skill { name: String => path: String, description: String, push_branch: String }",
        ":create dependency { dependent: String, dependency: String }"
    ];
    for s in schemas {
        db.run_script(s, Default::default(), ScriptMutability::Mutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;
    }

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

    // 4. Insert basic skills
    let skill_script = r#"
        ?[name, path, description, push_branch] <- [
            ["committing", ".pi/skills/independent-developer/SKILL.md", "Skill for committing and pushing", "dev"]
        ];
        :put agent_skill { name => path, description, push_branch }
    "#;
    db.run_script(skill_script, Default::default(), ScriptMutability::Mutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;

    // 5. Query and print
    println!("--- Components ---");
    let components_res = db.run_script("?[name, rating, description] := *component{name, rating, description}", Default::default(), ScriptMutability::Immutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;
    println!("{:#?}", components_res);

    println!("--- Agent Skills ---");
    let skills_res = db.run_script("?[name, path, description, push_branch] := *agent_skill{name, path, description, push_branch}", Default::default(), ScriptMutability::Immutable).map_err(|e| anyhow::anyhow!("{:?}", e))?;
    println!("{:#?}", skills_res);

    Ok(())
}
