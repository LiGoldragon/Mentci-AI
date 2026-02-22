use anyhow::{bail, Context, Result};
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::process::{Command, Stdio};

#[derive(Debug, Clone, PartialEq, Eq)]
struct BindMapping {
    source: PathBuf,
    target: PathBuf,
}

#[derive(Debug, Clone)]
struct SandboxConfig {
    workdir: PathBuf,
    home: PathBuf,
    share_network: bool,
    binds: Vec<BindMapping>,
    ro_binds: Vec<BindMapping>,
    env_map: HashMap<String, String>,
    command: Vec<String>,
}

pub fn run_from_args(mut args: Vec<String>) -> Result<()> {
    if args.first().map(String::as_str) == Some("sandbox") {
        args.remove(0);
    }
    let config = parse_args(args)?;
    run(config)
}

fn parse_args(args: Vec<String>) -> Result<SandboxConfig> {
    let mut workdir: Option<PathBuf> = None;
    let mut home: Option<PathBuf> = None;
    let mut share_network = false;
    let mut binds = Vec::new();
    let mut ro_binds = Vec::new();
    let mut env_map = HashMap::new();
    let mut command = Vec::new();

    let mut i = 0usize;
    while i < args.len() {
        match args[i].as_str() {
            "--workdir" => {
                i += 1;
                let value = args.get(i).context("missing value for --workdir")?;
                workdir = Some(PathBuf::from(value));
            }
            "--home" => {
                i += 1;
                let value = args.get(i).context("missing value for --home")?;
                home = Some(PathBuf::from(value));
            }
            "--bind" => {
                i += 1;
                let value = args.get(i).context("missing value for --bind")?;
                binds.push(parse_bind(value)?);
            }
            "--ro-bind" => {
                i += 1;
                let value = args.get(i).context("missing value for --ro-bind")?;
                ro_binds.push(parse_bind(value)?);
            }
            "--setenv" => {
                i += 1;
                let value = args.get(i).context("missing value for --setenv")?;
                let (key, val) = parse_env(value)?;
                env_map.insert(key, val);
            }
            "--share-net" => {
                share_network = true;
            }
            "--" => {
                command = args.into_iter().skip(i + 1).collect();
                break;
            }
            "--help" | "-h" => {
                print_help();
                std::process::exit(0);
            }
            other => {
                bail!(
                    "unknown sandbox argument: {other}. Use -- before the command to execute."
                );
            }
        }
        i += 1;
    }

    let workdir = workdir.unwrap_or(std::env::current_dir().context("failed to read current dir")?);
    let home = home.unwrap_or_else(default_home_path);
    let command = if command.is_empty() {
        vec!["/bin/sh".to_string()]
    } else {
        command
    };

    Ok(SandboxConfig {
        workdir,
        home,
        share_network,
        binds,
        ro_binds,
        env_map,
        command,
    })
}

fn parse_bind(value: &str) -> Result<BindMapping> {
    let mut split = value.splitn(2, ':');
    let source = split
        .next()
        .filter(|s| !s.is_empty())
        .context("bind source cannot be empty")?;
    let target = split
        .next()
        .filter(|s| !s.is_empty())
        .context("bind target cannot be empty (expected src:dst)")?;
    Ok(BindMapping {
        source: PathBuf::from(source),
        target: PathBuf::from(target),
    })
}

fn parse_env(value: &str) -> Result<(String, String)> {
    let mut split = value.splitn(2, '=');
    let key = split
        .next()
        .filter(|s| !s.is_empty())
        .context("env key cannot be empty (expected KEY=VALUE)")?;
    let val = split.next().context("env value missing (expected KEY=VALUE)")?;
    Ok((key.to_string(), val.to_string()))
}

fn default_home_path() -> PathBuf {
    let mut path = std::env::temp_dir();
    path.push(format!("mentci-sandbox-home-{}", std::process::id()));
    path
}

fn print_help() {
    println!("Usage: mentci-ai sandbox [options] -- <command> [args...]");
    println!("  --workdir <path>      Working directory inside sandbox (default: cwd)");
    println!("  --home <path>         HOME inside sandbox (default: /tmp/mentci-sandbox-home-<pid>)");
    println!("  --bind <src:dst>      Add writable bind mount");
    println!("  --ro-bind <src:dst>   Add read-only bind mount");
    println!("  --setenv <K=V>        Set environment variable inside sandbox");
    println!("  --share-net           Keep host network namespace (default: isolated)");
}

fn run(config: SandboxConfig) -> Result<()> {
    ensure_bwrap_available()?;
    std::fs::create_dir_all(&config.home)
        .with_context(|| format!("failed to create sandbox home {}", config.home.display()))?;

    let mut command = Command::new("bwrap");
    command.stdin(Stdio::inherit());
    command.stdout(Stdio::inherit());
    command.stderr(Stdio::inherit());
    command.args(to_bwrap_args(&config)?);
    let status = command.status().context("failed to start bwrap")?;
    if status.success() {
        return Ok(());
    }
    match status.code() {
        Some(code) => bail!("sandbox command exited with status code {code}"),
        None => bail!("sandbox command terminated by signal"),
    }
}

fn ensure_bwrap_available() -> Result<()> {
    let available = Command::new("sh")
        .args(["-lc", "command -v bwrap >/dev/null 2>&1"])
        .status()
        .context("failed to resolve bwrap availability")?
        .success();
    if available {
        Ok(())
    } else {
        bail!("bubblewrap (bwrap) is not available in PATH")
    }
}

fn to_bwrap_args(config: &SandboxConfig) -> Result<Vec<String>> {
    let mut args = vec![
        "--die-with-parent".to_string(),
        "--new-session".to_string(),
        "--proc".to_string(),
        "/proc".to_string(),
        "--dev".to_string(),
        "/dev".to_string(),
        "--tmpfs".to_string(),
        "/tmp".to_string(),
        "--tmpfs".to_string(),
        "/run".to_string(),
        "--unshare-pid".to_string(),
        "--unshare-uts".to_string(),
        "--unshare-ipc".to_string(),
        "--unshare-cgroup".to_string(),
    ];

    if !config.share_network {
        args.push("--unshare-net".to_string());
    }

    append_base_ro_binds(&mut args);
    append_ro_bind(&mut args, &config.workdir, &config.workdir);
    append_bind(&mut args, &config.workdir, &config.workdir);
    args.push("--chdir".to_string());
    args.push(config.workdir.display().to_string());

    args.push("--dir".to_string());
    args.push(config.home.display().to_string());
    args.push("--setenv".to_string());
    args.push("HOME".to_string());
    args.push(config.home.display().to_string());
    let user_value = std::env::var("USER").unwrap_or_else(|_| "mentci-ai".to_string());
    args.push("--setenv".to_string());
    args.push("USER".to_string());
    args.push(user_value);
    if let Ok(path) = std::env::var("PATH") {
        args.push("--setenv".to_string());
        args.push("PATH".to_string());
        args.push(path);
    }
    for (key, value) in &config.env_map {
        args.push("--setenv".to_string());
        args.push(key.clone());
        args.push(value.clone());
    }

    for mapping in &config.ro_binds {
        if !mapping.source.exists() {
            bail!(
                "ro-bind source path does not exist: {}",
                mapping.source.display()
            );
        }
        append_ro_bind(&mut args, &mapping.source, &mapping.target);
    }
    for mapping in &config.binds {
        if !mapping.source.exists() {
            bail!("bind source path does not exist: {}", mapping.source.display());
        }
        append_bind(&mut args, &mapping.source, &mapping.target);
    }

    args.push("--".to_string());
    args.extend(config.command.iter().cloned());

    Ok(args)
}

fn append_base_ro_binds(args: &mut Vec<String>) {
    for path in ["/nix", "/etc", "/usr", "/bin", "/sbin", "/lib", "/lib64"] {
        if Path::new(path).exists() {
            append_ro_bind(args, Path::new(path), Path::new(path));
        }
    }
}

fn append_ro_bind(args: &mut Vec<String>, source: &Path, target: &Path) {
    args.push("--ro-bind".to_string());
    args.push(source.display().to_string());
    args.push(target.display().to_string());
}

fn append_bind(args: &mut Vec<String>, source: &Path, target: &Path) {
    args.push("--bind".to_string());
    args.push(source.display().to_string());
    args.push(target.display().to_string());
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parse_sandbox_args_reads_explicit_values() {
        let parsed = parse_args(vec![
            "--workdir".to_string(),
            "/tmp/work".to_string(),
            "--home".to_string(),
            "/tmp/home".to_string(),
            "--setenv".to_string(),
            "A=B".to_string(),
            "--bind".to_string(),
            "/src:/dst".to_string(),
            "--ro-bind".to_string(),
            "/ro:/inside-ro".to_string(),
            "--".to_string(),
            "echo".to_string(),
            "ok".to_string(),
        ])
        .expect("parse should succeed");

        assert_eq!(parsed.workdir, PathBuf::from("/tmp/work"));
        assert_eq!(parsed.home, PathBuf::from("/tmp/home"));
        assert_eq!(parsed.command, vec!["echo".to_string(), "ok".to_string()]);
        assert_eq!(parsed.env_map.get("A"), Some(&"B".to_string()));
        assert_eq!(parsed.binds.len(), 1);
        assert_eq!(parsed.ro_binds.len(), 1);
    }

    #[test]
    fn parse_env_requires_key_value_shape() {
        assert!(parse_env("A=B").is_ok());
        assert!(parse_env("A=").is_ok());
        assert!(parse_env("A").is_err());
        assert!(parse_env("=B").is_err());
    }

    #[test]
    fn parse_bind_requires_source_and_target() {
        assert!(parse_bind("/a:/b").is_ok());
        assert!(parse_bind("/a").is_err());
        assert!(parse_bind(":/b").is_err());
        assert!(parse_bind("/a:").is_err());
    }
}
