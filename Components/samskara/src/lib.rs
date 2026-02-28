use serde::{Deserialize, Serialize};
use rusqlite::{params, Connection};
use std::process::Command;

pub mod samskara_jj_capnp {
    include!(concat!(env!("OUT_DIR"), "/samskara_jj_capnp.rs"));
}

#[derive(Debug, Serialize, Deserialize)]
pub struct JjFlow {
    #[serde(rename = "changeId")]
    pub change_id: String,
    #[serde(rename = "commitId")]
    pub commit_id: String,
    #[serde(rename = "workingCopy")]
    pub working_copy: bool,
    pub conflict: bool,
    pub bookmarks: Vec<String>,
    pub parents: Vec<String>,
}

pub struct SamskarizeEngine {
    db_path: String,
}

impl SamskarizeEngine {
    pub fn new(db_path: &str) -> Self {
        Self {
            db_path: db_path.to_string(),
        }
    }

    pub fn sync_repo(&self, repo_path: &str, uid: &str, component: &str, intent: &str) -> anyhow::Result<()> {
        let output = Command::new("jj")
            .args(["log", "-r", "@", "--template"])
            .arg(include_str!("../templates/samskarize_jj.tmpl"))
            .arg("--no-graph")
            .current_dir(repo_path)
            .output()?;

        if !output.status.success() {
            anyhow::bail!("jj command failed: {}", String::from_utf8_lossy(&output.stderr));
        }

        let json_str = String::from_utf8(output.stdout)?;
        let flow: JjFlow = serde_json::from_str(&json_str)?;

        let conn = Connection::open(&self.db_path)?;
        conn.execute(
            "INSERT INTO flows (uid, component, intent, change_id, commit_id, repo_path, status)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, 'in-progress')
             ON CONFLICT(uid, component, intent, subflow) DO UPDATE SET
             change_id = excluded.change_id,
             commit_id = excluded.commit_id,
             timestamp = CURRENT_TIMESTAMP",
            params![
                uid,
                component,
                intent,
                flow.change_id,
                flow.commit_id,
                repo_path,
            ],
        )?;

        Ok(())
    }
}
