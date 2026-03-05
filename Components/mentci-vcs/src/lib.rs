use anyhow::{Context, Result};
use serde::Deserialize;
use std::collections::HashMap;
use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;

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
        let status = self
            .base_command()
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
        let status = self
            .base_command()
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
        let output = self
            .base_command()
            .arg("status")
            .output()
            .context("Failed to execute jj status")?;

        Ok(String::from_utf8_lossy(&output.stdout).to_string())
    }

    fn diff(&self) -> Result<String> {
        let output = self
            .base_command()
            .arg("diff")
            .output()
            .context("Failed to execute jj diff")?;

        Ok(String::from_utf8_lossy(&output.stdout).to_string())
    }
}

#[derive(Debug, Deserialize)]
pub struct ComponentRepoManifest {
    pub version: u32,
    pub components: Vec<ComponentRepoSpec>,
}

#[derive(Debug, Deserialize)]
pub struct ComponentRepoSpec {
    pub id: String,
    pub path: String,
    pub flake_input: String,
    pub repo: String,
    #[serde(default)]
    pub required_submodule: bool,
    #[serde(default)]
    pub required_flake_lock: bool,
}

impl ComponentRepoManifest {
    pub fn from_path(path: &Path) -> Result<Self> {
        let data = fs::read_to_string(path)
            .with_context(|| format!("Failed reading manifest at {}", path.display()))?;
        let manifest: ComponentRepoManifest =
            toml::from_str(&data).context("Failed parsing component manifest TOML")?;
        Ok(manifest)
    }
}

#[derive(Debug, Deserialize)]
pub struct FlakeLock {
    pub nodes: HashMap<String, FlakeNode>,
}

#[derive(Debug, Deserialize)]
pub struct FlakeNode {
    pub inputs: Option<HashMap<String, FlakeInputRef>>,
    pub locked: Option<FlakeLocked>,
}

#[derive(Debug, Deserialize)]
#[serde(untagged)]
pub enum FlakeInputRef {
    Node(String),
    Path(Vec<String>),
}

#[derive(Debug, Deserialize)]
pub struct FlakeLocked {
    pub rev: Option<String>,
}

impl FlakeLock {
    pub fn from_path(path: &Path) -> Result<Self> {
        let data = fs::read_to_string(path)
            .with_context(|| format!("Failed reading flake lock at {}", path.display()))?;
        let lock: FlakeLock = serde_json::from_str(&data).context("Failed parsing flake.lock")?;
        Ok(lock)
    }

    pub fn lock_rev_for_input(&self, input: &str) -> Option<&str> {
        let root = self.nodes.get("root")?;
        let root_inputs = root.inputs.as_ref()?;
        let node_key = match root_inputs.get(input)? {
            FlakeInputRef::Node(key) => key.as_str(),
            FlakeInputRef::Path(path) => path.last()?.as_str(),
        };
        let node = self.nodes.get(node_key)?;
        node.locked.as_ref()?.rev.as_deref()
    }
}

#[derive(Debug)]
pub struct ComponentSplitStatus {
    pub id: String,
    pub path: String,
    pub repo: String,
    pub submodule_present: bool,
    pub flake_rev: Option<String>,
    pub required_submodule: bool,
    pub required_flake_lock: bool,
    pub violations: Vec<String>,
}

#[derive(Debug)]
pub struct ComponentSplitReport {
    pub statuses: Vec<ComponentSplitStatus>,
}

impl ComponentSplitReport {
    pub fn violation_count(&self) -> usize {
        self.statuses.iter().map(|x| x.violations.len()).sum()
    }
}

pub fn component_split_report(
    repo_root: &Path,
    manifest: &ComponentRepoManifest,
    flake_lock: &FlakeLock,
) -> Result<ComponentSplitReport> {
    component_split_report_with(repo_root, manifest, flake_lock, |root, path| {
        git_path_is_submodule(root, path)
    })
}

pub fn component_split_report_with<F>(
    repo_root: &Path,
    manifest: &ComponentRepoManifest,
    flake_lock: &FlakeLock,
    mut is_submodule: F,
) -> Result<ComponentSplitReport>
where
    F: FnMut(&Path, &str) -> Result<bool>,
{
    let mut statuses = Vec::with_capacity(manifest.components.len());

    for component in &manifest.components {
        let submodule_present = is_submodule(repo_root, &component.path)?;
        let flake_rev = flake_lock
            .lock_rev_for_input(&component.flake_input)
            .map(ToOwned::to_owned);

        let mut violations = Vec::new();
        if component.required_submodule && !submodule_present {
            violations.push("required submodule missing".to_owned());
        }
        if component.required_flake_lock && flake_rev.is_none() {
            violations.push("required flake.lock input missing or unlocked".to_owned());
        }

        statuses.push(ComponentSplitStatus {
            id: component.id.clone(),
            path: component.path.clone(),
            repo: component.repo.clone(),
            submodule_present,
            flake_rev,
            required_submodule: component.required_submodule,
            required_flake_lock: component.required_flake_lock,
            violations,
        });
    }

    Ok(ComponentSplitReport { statuses })
}

fn git_path_is_submodule(repo_root: &Path, path: &str) -> Result<bool> {
    let output = Command::new("git")
        .current_dir(repo_root)
        .arg("ls-files")
        .arg("--stage")
        .arg("--")
        .arg(path)
        .output()
        .with_context(|| format!("Failed to run git ls-files for {path}"))?;

    if !output.status.success() {
        return Ok(false);
    }

    let stdout = String::from_utf8_lossy(&output.stdout);
    let Some(first_line) = stdout.lines().next() else {
        return Ok(false);
    };

    let Some(mode) = first_line.split_whitespace().next() else {
        return Ok(false);
    };

    Ok(mode == "160000")
}

#[cfg(test)]
mod tests {
    use super::*;
    use tempfile::NamedTempFile;

    #[test]
    fn manifest_parses_components() {
        let mut file = NamedTempFile::new().expect("temp file");
        let manifest_data = r#"
version = 1

[[components]]
id = "mentci-aid"
path = "Components/mentci-aid"
flake_input = "mentci-aid-src"
repo = "git@github.com:LiGoldragon/mentci-aid.git"
required_submodule = true
required_flake_lock = true
"#;
        std::io::Write::write_all(&mut file, manifest_data.as_bytes()).expect("write manifest");

        let manifest = ComponentRepoManifest::from_path(file.path()).expect("parse manifest");
        assert_eq!(manifest.version, 1);
        assert_eq!(manifest.components.len(), 1);
        assert_eq!(manifest.components[0].id, "mentci-aid");
        assert!(manifest.components[0].required_submodule);
    }

    #[test]
    fn flake_lock_resolves_root_input_revision() {
        let mut file = NamedTempFile::new().expect("temp file");
        let lock_data = r#"
{
  "nodes": {
    "root": {
      "inputs": {
        "mentci-aid-src": "aidNode"
      }
    },
    "aidNode": {
      "locked": {
        "rev": "abc123"
      }
    }
  }
}
"#;
        std::io::Write::write_all(&mut file, lock_data.as_bytes()).expect("write lock");

        let lock = FlakeLock::from_path(file.path()).expect("parse lock");
        assert_eq!(lock.lock_rev_for_input("mentci-aid-src"), Some("abc123"));
    }

    #[test]
    fn strict_requirements_produce_violations() {
        let manifest = ComponentRepoManifest {
            version: 1,
            components: vec![ComponentRepoSpec {
                id: "mentci-aid".into(),
                path: "Components/mentci-aid".into(),
                flake_input: "mentci-aid-src".into(),
                repo: "git@github.com:LiGoldragon/mentci-aid.git".into(),
                required_submodule: true,
                required_flake_lock: true,
            }],
        };

        let lock = FlakeLock {
            nodes: HashMap::from([(
                "root".into(),
                FlakeNode {
                    inputs: Some(HashMap::new()),
                    locked: None,
                },
            )]),
        };

        let report = component_split_report_with(Path::new("."), &manifest, &lock, |_root, _path| {
            Ok(false)
        })
        .expect("report");

        assert_eq!(report.violation_count(), 2);
        assert_eq!(report.statuses.len(), 1);
        assert_eq!(report.statuses[0].violations.len(), 2);
    }
}
