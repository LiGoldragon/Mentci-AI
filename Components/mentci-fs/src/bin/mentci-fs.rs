use anyhow::{Result, Context};
use mentci_fs::FsScanner;
use std::path::PathBuf;
use std::env;

fn main() -> Result<()> {
    let args: Vec<String> = env::args().skip(1).collect();
    
    if args.is_empty() || args.contains(&"--help".to_string()) || args.contains(&"-h".to_string()) {
        print_help();
        return Ok(());
    }

    let dir_path = PathBuf::from(&args[0]);
    let resolved_paths = FsScanner::scan_dir(&dir_path)
        .context(format!("failed to scan directory: {}", dir_path.display()))?;

    if resolved_paths.is_empty() {
        println!("no index.edn found or no entries defined in {}", dir_path.display());
    } else {
        for path in resolved_paths {
            println!("{}", path.display());
        }
    }

    Ok(())
}

fn print_help() {
    println!("mentci-fs: Concise localized filesystem indexing tool.");
    println!("");
    println!("usage:");
    println!("  mentci-fs <directory>");
    println!("");
    println!("options:");
    println!("  -h, --help    show help");
}
