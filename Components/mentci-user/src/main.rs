use anyhow::{Context, Result};
use std::env;
use std::fs::File;
#[cfg(unix)]
use std::os::unix::process::CommandExt;
use std::process::Command;

use mentci_user::{mentci_user_capnp, load_local_config, resolve_secret};

fn main() -> Result<()> {
    let args: Vec<String> = env::args().collect();
    
    if args.len() < 3 {
        println!("Usage:");
        println!("  mentci-user exec <path_to_setup_bin> -- <command> [args...]");
        println!("  mentci-user export-env <path_to_setup_bin>");
        return Ok(());
    }

    let mode = &args[1];
    let bin_path = &args[2];
    
    // Read Capnp setup
    let mut file = File::open(bin_path).with_context(|| format!("Failed to open {}", bin_path))?;
    let message_reader = capnp::serialize::read_message(&mut file, capnp::message::ReaderOptions::new())?;
    let setup = message_reader.get_root::<mentci_user_capnp::user_setup_config::Reader>()?;
    
    let user_config_path = setup.get_user_config_path()?.to_string()?;
    let local_config = load_local_config(&user_config_path).unwrap_or_else(|_| {
        mentci_user::UserLocalConfig { secrets: vec![] }
    });

    let reqs = setup.get_required_env_vars()?;
    
    if mode == "export-env" {
        for req in reqs.iter() {
            let name = req.get_name()?.to_string()?;
            let mut method = req.get_default_method()?.to_string()?;
            let mut path = req.get_default_path()?.to_string()?;

            if let Some(over) = local_config.secrets.iter().find(|s| s.name == name) {
                method = over.method.clone();
                path = over.path.clone();
            }

            if let Ok(Some(val)) = resolve_secret(&method, &path) {
                let escaped_val = val.replace("'", "'\\''");
                println!("export {}='{}';", name, escaped_val);
            }
        }
        return Ok(());
    }

    if mode == "exec" {
        let separator_idx = args.iter().position(|a| a == "--")
            .context("Missing '--' separator before command")?;
            
        let cmd_args = &args[separator_idx + 1..];
        if cmd_args.is_empty() {
            anyhow::bail!("No command specified after '--'");
        }

        for req in reqs.iter() {
            let name = req.get_name()?.to_string()?;
            let mut method = req.get_default_method()?.to_string()?;
            let mut path = req.get_default_path()?.to_string()?;

            if let Some(over) = local_config.secrets.iter().find(|s| s.name == name) {
                method = over.method.clone();
                path = over.path.clone();
            }

            if let Ok(Some(val)) = resolve_secret(&method, &path) {
                env::set_var(&name, val);
            } else {
                eprintln!("Warning: Failed to resolve secret for {}", name);
            }
        }

        // Execute the target command
        let mut cmd = Command::new(&cmd_args[0]);
        cmd.args(&cmd_args[1..]);
        
        #[cfg(unix)]
        {
            let err = cmd.exec();
            anyhow::bail!("Failed to exec {}: {}", cmd_args[0], err);
        }
        
        #[cfg(not(unix))]
        {
            let status = cmd.status()?;
            std::process::exit(status.code().unwrap_or(1));
        }
    }

    anyhow::bail!("Unknown mode: {}", mode);
}
