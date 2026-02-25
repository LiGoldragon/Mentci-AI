pub mod user_capnp {
    include!(concat!(env!("OUT_DIR"), "/user_capnp.rs"));
}

use std::fs;
use std::process::Command;
use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct SecretConfig {
    pub name: String,
    pub method: String, // "gopass", "env", "literal"
    pub path: String,   // gopass path, env var name, or literal value
}

#[derive(Serialize, Deserialize, Debug)]
pub struct UserConfig {
    pub secrets: Vec<SecretConfig>,
}

pub fn load_config() -> Result<UserConfig> {
    let config_path = ".mentci/user.json";
    if !std::path::Path::new(config_path).exists() {
        return Ok(UserConfig { secrets: vec![] });
    }
    let content = fs::read_to_string(config_path)
        .with_context(|| format!("Failed to read {}", config_path))?;
    let config: UserConfig = serde_json::from_str(&content)
        .with_context(|| format!("Failed to parse {}", config_path))?;
    Ok(config)
}

pub fn get_secret(name: &str) -> Result<Option<String>> {
    let config = load_config()?;
    for secret in config.secrets {
        if secret.name == name {
            match secret.method.as_str() {
                "gopass" => {
                    let output = Command::new("gopass")
                        .arg("show")
                        .arg(&secret.path)
                        .output()
                        .with_context(|| format!("Failed to execute gopass for {}", secret.path))?;
                    if output.status.success() {
                        let val = String::from_utf8(output.stdout)?
                            .lines()
                            .next()
                            .unwrap_or("")
                            .trim()
                            .to_string();
                        return Ok(Some(val));
                    } else {
                        anyhow::bail!("gopass failed: {}", String::from_utf8_lossy(&output.stderr));
                    }
                }
                "env" => {
                    return Ok(std::env::var(&secret.path).ok());
                }
                "literal" => {
                    return Ok(Some(secret.path));
                }
                _ => anyhow::bail!("Unknown secret method: {}", secret.method),
            }
        }
    }
    Ok(None)
}
