use anyhow::{bail, Context, Result};
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::process::{Command, Stdio};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct BindMapping {
    pub source: PathBuf,
    pub target: PathBuf,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SandboxConfig {
    pub workdir: PathBuf,
    pub home: PathBuf,
    pub share_network: bool,
    pub binds: Vec<BindMapping>,
    pub ro_binds: Vec<BindMapping>,
    pub env_map: HashMap<String, String>,
    pub command: Vec<String>,
}

pub struct Sandbox {
    config: SandboxConfig,
}

impl Sandbox {
    pub fn from_config(config: SandboxConfig) -> Self {
        Self { config }
    }

    pub fn run(self) -> Result<()> {
        ensure_bwrap_available()?;
        std::fs::create_dir_all(&self.config.home)
            .with_context(|| format!("failed to create sandbox home {}", self.config.home.display()))?;

        let mut command = Command::new("bwrap");
        command.stdin(Stdio::inherit());
        command.stdout(Stdio::inherit());
        command.stderr(Stdio::inherit());
        command.args(self.to_bwrap_args()?);
        
        let status = command.status().context("failed to start bwrap")?;
        if status.success() {
            return Ok(());
        }
        match status.code() {
            Some(code) => bail!("sandbox command exited with status code {code}"),
            None => bail!("sandbox command terminated by signal"),
        }
    }

    fn to_bwrap_args(&self) -> Result<Vec<String>> {
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

        if !self.config.share_network {
            args.push("--unshare-net".to_string());
        }

        append_base_ro_binds(&mut args);
        append_ro_bind(&mut args, &self.config.workdir, &self.config.workdir);
        append_bind(&mut args, &self.config.workdir, &self.config.workdir);
        args.push("--chdir".to_string());
        args.push(self.config.workdir.display().to_string());

        args.push("--dir".to_string());
        args.push(self.config.home.display().to_string());
        args.push("--setenv".to_string());
        args.push("HOME".to_string());
        args.push(self.config.home.display().to_string());
        
        let user_value = std::env::var("USER").unwrap_or_else(|_| "mentci-box".to_string());
        args.push("--setenv".to_string());
        args.push("USER".to_string());
        args.push(user_value);
        
        if let Ok(path) = std::env::var("PATH") {
            args.push("--setenv".to_string());
            args.push("PATH".to_string());
            args.push(path);
        }
        
        for (key, value) in &self.config.env_map {
            args.push("--setenv".to_string());
            args.push(key.clone());
            args.push(value.clone());
        }

        for mapping in &self.config.ro_binds {
            if !mapping.source.exists() {
                bail!("ro-bind source path does not exist: {}", mapping.source.display());
            }
            append_ro_bind(&mut args, &mapping.source, &mapping.target);
        }
        for mapping in &self.config.binds {
            if !mapping.source.exists() {
                bail!("bind source path does not exist: {}", mapping.source.display());
            }
            append_bind(&mut args, &mapping.source, &mapping.target);
        }

        args.push("--".to_string());
        args.extend(self.config.command.iter().cloned());

        Ok(args)
    }
}

impl SandboxConfig {
    pub fn from_args(args: Vec<String>) -> Result<Self> {
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
                other => {
                    bail!("unknown sandbox argument: {other}. Use -- before the command to execute.");
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
}

fn parse_bind(value: &str) -> Result<BindMapping> {
    let mut split = value.splitn(2, ':');
    let source = split.next().filter(|s| !s.is_empty()).context("bind source cannot be empty")?;
    let target = split.next().filter(|s| !s.is_empty()).context("bind target cannot be empty (expected src:dst)")?;
    Ok(BindMapping {
        source: PathBuf::from(source),
        target: PathBuf::from(target),
    })
}

fn parse_env(value: &str) -> Result<(String, String)> {
    let mut split = value.splitn(2, '=');
    let key = split.next().filter(|s| !s.is_empty()).context("env key cannot be empty (expected KEY=VALUE)")?;
    let val = split.next().context("env value missing (expected KEY=VALUE)")?;
    Ok((key.to_string(), val.to_string()))
}

fn default_home_path() -> PathBuf {
    let mut path = std::env::temp_dir();
    path.push(format!("mentci-box-home-{}", std::process::id()));
    path
}

fn ensure_bwrap_available() -> Result<()> {
    let available = Command::new("sh")
        .args(["-lc", "command -v bwrap >/dev/null 2>&1"])
        .status()
        .map(|s| s.success())
        .unwrap_or(false);
    if available {
        Ok(())
    } else {
        bail!("bubblewrap (bwrap) is not available in PATH")
    }
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
