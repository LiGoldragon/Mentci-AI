use anyhow::{Context, Result};
use mentci_vcs::{
    component_split_report, sync_required_submodules, ComponentRepoManifest, FlakeLock, Jujutsu,
    VCS,
};
use std::env;
use std::path::PathBuf;

fn command_message(args: &[String], command: &str, placeholder: &str) -> Result<String> {
    if args.len() < 3 {
        anyhow::bail!("Usage: mentci-vcs {command} <{placeholder}>");
    }

    Ok(args[2..].join(" "))
}

fn main() -> Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        eprintln!("Usage: mentci-vcs <command> [args]");
        eprintln!("Commands: intent-start <task>, status, diff, commit <message>, component-split-status [--manifest <path>] [--flake-lock <path>], sync-required-submodules [--manifest <path>] [--apply]");
        std::process::exit(1);
    }

    let repo_root = env::current_dir()?;
    let vcs = Jujutsu::new(repo_root.clone());

    match args[1].as_str() {
        "intent-start" => {
            let task = command_message(&args, "intent-start", "task")?;
            println!("Starting intent: {}", task);
            vcs.new_with_message(&format!("intent: {}", task))?;
        }
        "commit" => {
            let message = command_message(&args, "commit", "message")?;
            vcs.commit(&message)?;
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
        "sync-required-submodules" => {
            run_sync_required_submodules(&repo_root, &args[2..])?;
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
        let submodule_rev = status.submodule_rev.as_deref().unwrap_or("-");
        let submodule_mark = if status.submodule_present { "yes" } else { "no" };
        if status.violations.is_empty() {
            println!(
                "[ok] {} path={} submodule={} submodule_rev={} lock_rev={}",
                status.id, status.path, submodule_mark, submodule_rev, lock_rev
            );
        } else {
            println!(
                "[violation] {} path={} submodule={} submodule_rev={} lock_rev={} issues={}",
                status.id,
                status.path,
                submodule_mark,
                submodule_rev,
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

fn run_sync_required_submodules(repo_root: &std::path::Path, args: &[String]) -> Result<()> {
    let mut manifest_path = PathBuf::from("Components/contracts/rust-component-repos.toml");
    let mut apply = false;

    let mut i = 0;
    while i < args.len() {
        match args[i].as_str() {
            "--manifest" => {
                let value = args.get(i + 1).context("--manifest requires a value")?;
                manifest_path = PathBuf::from(value);
                i += 2;
            }
            "--apply" => {
                apply = true;
                i += 1;
            }
            unknown => {
                anyhow::bail!("Unknown argument for sync-required-submodules: {unknown}");
            }
        }
    }

    let manifest = ComponentRepoManifest::from_path(&repo_root.join(&manifest_path))?;
    let report = if apply {
        sync_required_submodules(repo_root, &manifest)?
    } else {
        mentci_vcs::required_submodule_sync_report(repo_root, &manifest)?
    };

    println!(
        "required submodule sync: {} tracked, {} need sync, {} issues, mode={}",
        report.statuses.len(),
        report.needs_sync_count(),
        report.issue_count(),
        if apply { "apply" } else { "dry-run" }
    );

    for status in &report.statuses {
        let tracked_rev = status.tracked_rev.as_deref().unwrap_or("-");
        let working_rev = status.working_rev.as_deref().unwrap_or("-");
        if status.issues.is_empty() && !status.needs_sync {
            println!(
                "[ok] {} path={} tracked_rev={} working_rev={}",
                status.id, status.path, tracked_rev, working_rev
            );
        } else {
            let mut details = Vec::new();
            if status.needs_sync {
                details.push("checkout drift".to_owned());
            }
            details.extend(status.issues.iter().cloned());
            println!(
                "[sync] {} path={} tracked_rev={} working_rev={} issues={}",
                status.id,
                status.path,
                tracked_rev,
                working_rev,
                details.join("; ")
            );
        }
    }

    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn command_message_collects_multiple_words() {
        let args = vec![
            "mentci-vcs".to_owned(),
            "commit".to_owned(),
            "sync".to_owned(),
            "required".to_owned(),
            "subrepos".to_owned(),
        ];

        let message = command_message(&args, "commit", "message").expect("message");
        assert_eq!(message, "sync required subrepos");
    }

    #[test]
    fn command_message_requires_trailing_words() {
        let args = vec!["mentci-vcs".to_owned(), "commit".to_owned()];

        let error = command_message(&args, "commit", "message").expect_err("error");
        assert!(error.to_string().contains("Usage: mentci-vcs commit <message>"));
    }
}
