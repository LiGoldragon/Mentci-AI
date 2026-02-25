use anyhow::Result;
use mentci_user::{get_secret, load_config};

fn main() -> Result<()> {
    let args: Vec<String> = std::env::args().collect();
    
    if args.len() > 1 && args[1] == "export-env" {
        if let Ok(config) = load_config() {
            for secret in config.secrets {
                if let Ok(Some(val)) = get_secret(&secret.name) {
                    // Properly escape the single quotes for bash eval
                    let escaped_val = val.replace("'", "'\\''");
                    println!("export {}='{}';", secret.name, escaped_val);
                }
            }
        }
    } else {
        println!("Mentci-AI User Config Utility");
        println!("Usage: mentci-user export-env");
    }

    Ok(())
}
