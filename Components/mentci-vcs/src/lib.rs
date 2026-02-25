use anyhow::{Result, Context};
use std::process::Command;
use std::path::PathBuf;

pub trait VCS {
    fn commit(&self, message: &str) -> Result<()>;
    fn new_with_message(&self, message: &str) -> Result<()>;
    fn status(&self) -> Result<String>;
    fn diff(&self) -> Result<String>;
}

pub struct Jujutsu {
    repo_root: PathBuf,
    config_path: PathBuf,
}

impl Jujutsu {
    pub fn new(repo_root: PathBuf) -> Self {
        let config_path = repo_root.join(".mentci/jj-project-config.toml");
        Self {
            repo_root,
            config_path,
        }
    }

    fn base_command(&self) -> Command {
        let mut cmd = Command::new("jj");
        cmd.current_dir(&self.repo_root);
        cmd.env("JJ_CONFIG", &self.config_path);
        cmd.env("PAGER", "cat");
        cmd.env("CI", "true");
        cmd
    }
}

impl VCS for Jujutsu {
    fn commit(&self, message: &str) -> Result<()> {
        let status = self.base_command()
            .arg("commit")
            .arg("-m")
            .arg(message)
            .status()
            .context("Failed to execute jj commit")?;

        if !status.success() {
            anyhow::bail!("jj commit failed with exit code: {:?}", status.code());
        }
        Ok(())
    }

    fn new_with_message(&self, message: &str) -> Result<()> {
        let status = self.base_command()
            .arg("new")
            .arg("-m")
            .arg(message)
            .status()
            .context("Failed to execute jj new")?;

        if !status.success() {
            anyhow::bail!("jj new failed with exit code: {:?}", status.code());
        }
        Ok(())
    }

    fn status(&self) -> Result<String> {
        let output = self.base_command()
            .arg("status")
            .output()
            .context("Failed to execute jj status")?;

        Ok(String::from_utf8_lossy(&output.stdout).to_string())
    }

    fn diff(&self) -> Result<String> {
        let output = self.base_command()
            .arg("diff")
            .output()
            .context("Failed to execute jj diff")?;

        Ok(String::from_utf8_lossy(&output.stdout).to_string())
    }
}
