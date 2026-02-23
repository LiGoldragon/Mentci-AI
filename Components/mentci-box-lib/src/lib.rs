pub mod mentci_box_capnp {
    include!(concat!(env!("OUT_DIR"), "/mentci_box_capnp.rs"));
}

use anyhow::{bail, Context, Result};
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::process::{Command, Stdio};
use serde::{Deserialize, Serialize};
use capnp::message::ReaderOptions;
use capnp::serialize_packed;

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
    pub command: Vec<String>,
    pub env_map: HashMap<String, String>,
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
            append_ro_bind(&mut args, &mapping.source, &mapping.target);
        }
        for mapping in &self.config.binds {
            append_bind(&mut args, &mapping.source, &mapping.target);
        }

        args.push("--".to_string());
        args.extend(self.config.command.iter().cloned());

        Ok(args)
    }
}

pub struct BoxRequest {
    pub worktree_path: PathBuf,
    pub home_path: PathBuf,
    pub share_network: bool,
    pub fetch_sources: bool,
    pub prepare_components: bool,
    pub binds: Vec<BindMapping>,
    pub ro_binds: Vec<BindMapping>,
}

impl BoxRequest {
    pub fn from_capnp(path: &Path) -> Result<Self> {
        let file = std::fs::File::open(path)?;
        let mut reader = std::io::BufReader::new(file);
        let message = serialize_packed::read_message(&mut reader, ReaderOptions::new())?;
        let request = message.get_root::<mentci_box_capnp::mentci_box_request::Reader<'_>>()?;

        let mut binds = Vec::new();
        for b in request.get_binds()? {
            binds.push(BindMapping {
                source: PathBuf::from(b.get_source()?.to_str()?),
                target: PathBuf::from(b.get_target()?.to_str()?),
            });
        }

        let mut ro_binds = Vec::new();
        for b in request.get_ro_binds()? {
            ro_binds.push(BindMapping {
                source: PathBuf::from(b.get_source()?.to_str()?),
                target: PathBuf::from(b.get_target()?.to_str()?),
            });
        }

        Ok(Self {
            worktree_path: PathBuf::from(request.get_worktree_path()?.to_str()?),
            home_path: PathBuf::from(request.get_home_path()?.to_str()?),
            share_network: request.get_share_network(),
            fetch_sources: request.get_fetch_sources(),
            prepare_components: request.get_prepare_components(),
            binds,
            ro_binds,
        })
    }

    pub fn to_sandbox_config(self) -> Result<SandboxConfig> {
        let env_map = HashMap::new();
        // Prepare logic here (fetch sources etc) - for now just skeleton
        
        Ok(SandboxConfig {
            workdir: self.worktree_path,
            home: self.home_path,
            share_network: self.share_network,
            binds: self.binds,
            ro_binds: self.ro_binds,
            command: vec!["/bin/sh".to_string()],
            env_map,
        })
    }
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
