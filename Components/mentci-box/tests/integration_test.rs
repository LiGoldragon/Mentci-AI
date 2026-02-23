use std::process::Command;
use std::fs::File;
use std::io::Write;
use tempfile::tempdir;

#[test]
fn test_mentci_box_environment() {
    let dir = tempdir().unwrap();
    
    // Find repo root
    let root_output = Command::new("git")
        .args(["rev-parse", "--show-toplevel"])
        .output()
        .expect("failed to find repo root");
    let repo_root = String::from_utf8_lossy(&root_output.stdout).trim().to_string();
    
    let schema_path = format!("{}/Components/schema/mentci_box.capnp", repo_root);
    let request_txt = dir.path().join("request.txt");
    let request_bin = dir.path().join("request.bin");
    let script_path = format!("{}/Components/mentci-box/tests/internal_test.sh", repo_root);

    let mut file = File::create(&request_txt).unwrap();
    writeln!(file, r#"(
  worktreePath = "{}",
  homePath = "/tmp/mentci-box-test-home",
  shareNetwork = false,
  fetchSources = true,
  prepareComponents = true,
  binds = [],
  roBinds = [],
  user = "mentci-box",
  command = ["sh", "{}"],
  env = [
    (key = "EXPECTED_REMOTE", value = "git@github.com:LiGoldragon/Mentci-AI.git")
  ]
)"#, repo_root, script_path).unwrap();

    // Encode
    let status = Command::new("capnp")
        .args(["encode", "--packed", &schema_path, "MentciBoxRequest"])
        .stdin(File::open(&request_txt).unwrap())
        .stdout(File::create(&request_bin).unwrap())
        .status()
        .expect("failed to execute capnp encode");
    assert!(status.success());

    // Run mentci-box
    let status = Command::new("cargo")
        .args(["run", "--quiet", "--", request_bin.to_str().unwrap()])
        .status()
        .expect("failed to execute mentci-box via cargo");
    
    assert!(status.success());
}
