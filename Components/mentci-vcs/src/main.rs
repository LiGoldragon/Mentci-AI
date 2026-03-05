use anyhow::{Context, Result};
use mentci_vcs::{
    component_split_report, ComponentRepoManifest, FlakeLock, Jujutsu, VCS,
};
use std::env;
use std::path::PathBuf;

fn main() -> Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        eprintln!("Usage: mentci-vcs <command> [args]");
        eprintln!("Commands: intent-start <task>, status, diff, commit <message>, component-split-status [--manifest <path>] [--flake-lock <path>]");
        std::process::exit(1);
    }

    let repo_root = env::current_dir()?;
    let vcs = Jujutsu::new(repo_root.clone());

    match args[1].as_str() {
        "intent-start" => {
            if args.len() < 3 {
                eprintln!("Usage: mentci-vcs intent-start <task>");
                std::process::exit(1);
            }
            let task = &args[2];
            println!("Starting intent: {}", task);
            vcs.new_with_message(&format!("intent: {}", task))?;
        }
        "commit" => {
            if args.len() < 3 {
                eprintln!("Usage: mentci-vcs commit <message>");
                std::process::exit(1);
            }
            vcs.commit(&args[2])?;
        }
        "status" => {
            let out = vcs.status()?;
            println!("{}", out);
        }
        "diff" => {
            let out = vcs.diff()?;
            println!("{}", out);
        }
        "component-split-status" => {
            run_component_split_status(&repo_root, &args[2..])?;
        }
        _ => {
            eprintln!("Unknown command: {}", args[1]);
            std::process::exit(1);
        }
    }

    Ok(())
}

fn run_component_split_status(repo_root: &std::path::Path, args: &[String]) -> Result<()> {
    let mut manifest_path = PathBuf::from("Components/contracts/rust-component-repos.toml");
    let mut flake_lock_path = PathBuf::from("flake.lock");

    let mut i = 0;
    while i < args.len() {
        match args[i].as_str() {
            "--manifest" => {
                let value = args.get(i + 1).context("--manifest requires a value")?;
                manifest_path = PathBuf::from(value);
                i += 2;
            }
            "--flake-lock" => {
                let value = args.get(i + 1).context("--flake-lock requires a value")?;
                flake_lock_path = PathBuf::from(value);
                i += 2;
            }
            unknown => {
                anyhow::bail!("Unknown argument for component-split-status: {unknown}");
            }
        }
    }

    let manifest = ComponentRepoManifest::from_path(&repo_root.join(&manifest_path))?;
    let lock = FlakeLock::from_path(&repo_root.join(&flake_lock_path))?;
    let report = component_split_report(repo_root, &manifest, &lock)?;

    println!(
        "component split status: {} components, {} violations",
        report.statuses.len(),
        report.violation_count()
    );

    for status in &report.statuses {
        let lock_rev = status.flake_rev.as_deref().unwrap_or("-");
        let submodule_mark = if status.submodule_present { "yes" } else { "no" };
        if status.violations.is_empty() {
            println!(
                "[ok] {} path={} submodule={} lock_rev={}",
                status.id, status.path, submodule_mark, lock_rev
            );
        } else {
            println!(
                "[violation] {} path={} submodule={} lock_rev={} issues={}",
                status.id,
                status.path,
                submodule_mark,
                lock_rev,
                status.violations.join("; ")
            );
        }
    }

    if report.violation_count() > 0 {
        anyhow::bail!("component split policy violations detected");
    }

    Ok(())
}
