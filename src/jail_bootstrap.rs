use anyhow::{bail, Context, Result};
use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;

#[derive(Debug, Clone)]
struct BootstrapConfig {
    repo_root: PathBuf,
    outputs_dir: String,
    output_name: String,
    working_bookmark: String,
    target_bookmark: String,
    commit_message: Option<String>,
}

pub fn run_from_args(mut args: Vec<String>) -> Result<()> {
    if args.first().map(String::as_str) == Some("bootstrap") {
        args.remove(0);
    }
    let cfg = parse_args(args)?;
    run(cfg)
}

fn parse_args(args: Vec<String>) -> Result<BootstrapConfig> {
    let mut repo_root = std::env::current_dir().context("failed to read current directory")?;
    let mut outputs_dir = "Outputs".to_string();
    let mut output_name = "mentci-ai".to_string();
    let mut working_bookmark = "dev".to_string();
    let mut target_bookmark = "jailCommit".to_string();
    let mut commit_message: Option<String> = None;

    let mut i = 0usize;
    while i < args.len() {
        match args[i].as_str() {
            "--repo-root" => {
                i += 1;
                let value = args.get(i).context("missing value for --repo-root")?;
                repo_root = PathBuf::from(value);
            }
            "--outputs-dir" => {
                i += 1;
                let value = args.get(i).context("missing value for --outputs-dir")?;
                outputs_dir = value.clone();
            }
            "--output-name" => {
                i += 1;
                let value = args.get(i).context("missing value for --output-name")?;
                output_name = value.clone();
            }
            "--working-bookmark" => {
                i += 1;
                let value = args.get(i).context("missing value for --working-bookmark")?;
                working_bookmark = value.clone();
            }
            "--target-bookmark" => {
                i += 1;
                let value = args.get(i).context("missing value for --target-bookmark")?;
                target_bookmark = value.clone();
            }
            "--commit-message" => {
                i += 1;
                let value = args.get(i).context("missing value for --commit-message")?;
                commit_message = Some(value.clone());
            }
            "--help" | "-h" => {
                print_help();
                return Ok(BootstrapConfig {
                    repo_root,
                    outputs_dir,
                    output_name,
                    working_bookmark,
                    target_bookmark,
                    commit_message,
                });
            }
            other => bail!("unknown argument: {other}"),
        }
        i += 1;
    }

    Ok(BootstrapConfig {
        repo_root,
        outputs_dir,
        output_name,
        working_bookmark,
        target_bookmark,
        commit_message,
    })
}

fn print_help() {
    println!("Usage: mentci-ai job/jails [bootstrap] [options]");
    println!("  --repo-root <path>           Repository root (default: cwd)");
    println!("  --outputs-dir <name>         Outputs directory (default: Outputs)");
    println!("  --output-name <name>         Output workspace name (default: mentci-ai)");
    println!("  --working-bookmark <name>    Working bookmark (default: dev)");
    println!("  --target-bookmark <name>     Commit target bookmark (default: jailCommit)");
    println!("  --commit-message <message>   Optional commit message for immediate jail commit");
}

fn run(cfg: BootstrapConfig) -> Result<()> {
    if cfg.target_bookmark == cfg.working_bookmark {
        bail!(
            "target bookmark '{}' must differ from working bookmark '{}'",
            cfg.target_bookmark,
            cfg.working_bookmark
        );
    }

    let repo_root = fs::canonicalize(&cfg.repo_root)
        .with_context(|| format!("failed to canonicalize repo root {:?}", cfg.repo_root))?;
    if !repo_root.join(".jj").exists() {
        bail!("repo root {:?} is not a jj repository", repo_root);
    }

    ensure_revision_exists(&repo_root, &cfg.working_bookmark)?;

    let outputs_root = repo_root.join(&cfg.outputs_dir);
    fs::create_dir_all(&outputs_root)
        .with_context(|| format!("failed to create outputs dir {:?}", outputs_root))?;

    let workspace_path = outputs_root.join(&cfg.output_name);
    let workspace_name = format!("output-{}", sanitize_workspace_name(&cfg.output_name));

    if !workspace_path.join(".jj").exists() {
        run_jj(
            &repo_root,
            &[
                "workspace",
                "add",
                workspace_path
                    .to_str()
                    .context("workspace path contains invalid utf-8")?,
                "--name",
                &workspace_name,
                "--revision",
                &cfg.working_bookmark,
            ],
        )
        .context("failed to create output workspace")?;
    }

    if let Some(message) = &cfg.commit_message {
        run_jj(&workspace_path, &["describe", "-m", message])
            .context("failed to describe workspace commit")?;
        run_jj(
            &workspace_path,
            &["bookmark", "set", &cfg.target_bookmark, "-r", "@"],
        )
        .context("failed to set target bookmark from workspace")?;
    }

    println!("MENTCI_REPO_ROOT={}", repo_root.display());
    println!("MENTCI_WORKSPACE={}", workspace_path.display());
    println!("MENTCI_WORKING_BOOKMARK={}", cfg.working_bookmark);
    println!("MENTCI_COMMIT_TARGET={}", cfg.target_bookmark);
    Ok(())
}

fn sanitize_workspace_name(value: &str) -> String {
    value
        .chars()
        .map(|c| {
            if c.is_ascii_alphanumeric() || c == '-' || c == '_' {
                c
            } else {
                '-'
            }
        })
        .collect()
}

fn ensure_revision_exists(repo_root: &Path, revset: &str) -> Result<()> {
    run_jj(
        repo_root,
        &["log", "-r", revset, "-n", "1", "--no-graph", "-T", "commit_id"],
    )
    .with_context(|| format!("working bookmark/revset '{revset}' does not exist"))?;
    Ok(())
}

fn run_jj(repo_root: &Path, args: &[&str]) -> Result<()> {
    let output = Command::new("jj")
        .args(args)
        .arg("-R")
        .arg(repo_root)
        .output()
        .with_context(|| format!("failed to run jj {:?}", args))?;
    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        let stdout = String::from_utf8_lossy(&output.stdout);
        bail!(
            "jj {:?} failed (code {}): {}{}",
            args,
            output.status.code().unwrap_or(-1),
            stdout,
            stderr
        );
    }
    Ok(())
}
