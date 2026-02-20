use std::fs;
use std::process::Command;

fn run_ok(cmd: &mut Command) {
    let output = cmd.output().expect("command should start");
    if !output.status.success() {
        panic!(
            "command failed:\nstatus={}\nstdout={}\nstderr={}",
            output.status,
            String::from_utf8_lossy(&output.stdout),
            String::from_utf8_lossy(&output.stderr)
        );
    }
}

fn run_capture(cmd: &mut Command) -> String {
    let output = cmd.output().expect("command should start");
    if !output.status.success() {
        panic!(
            "command failed:\nstatus={}\nstdout={}\nstderr={}",
            output.status,
            String::from_utf8_lossy(&output.stdout),
            String::from_utf8_lossy(&output.stderr)
        );
    }
    String::from_utf8_lossy(&output.stdout).to_string()
}

#[test]
fn bootstrap_creates_jail_commit_bookmark_from_output_workspace() {
    let temp = tempfile::tempdir().expect("tempdir");
    let repo = temp.path().join("repo");
    fs::create_dir_all(&repo).expect("create repo dir");

    run_ok(Command::new("jj").args(["git", "init"]).arg(&repo));
    run_ok(
        Command::new("jj")
            .args(["bookmark", "create", "dev", "-r", "@", "-R"])
            .arg(&repo),
    );

    let bin = env!("CARGO_BIN_EXE_mentci-bootstrap");
    run_ok(
        Command::new(bin)
            .args(["--repo-root"])
            .arg(&repo)
            .args([
                "--output-name",
                "mentci-ai",
                "--working-bookmark",
                "dev",
                "--target-bookmark",
                "jailCommit",
            ]),
    );

    let workspace = repo.join("Outputs").join("mentci-ai");
    assert!(
        workspace.join(".jj").exists(),
        "expected workspace .jj at {:?}",
        workspace
    );

    fs::write(workspace.join("jail.txt"), "wrapped by rust bootstrap\n").expect("write test change");

    run_ok(
        Command::new(bin)
            .args(["--repo-root"])
            .arg(&repo)
            .args([
                "--output-name",
                "mentci-ai",
                "--working-bookmark",
                "dev",
                "--target-bookmark",
                "jailCommit",
                "--commit-message",
                "intent: test jail commit",
            ]),
    );

    let bookmarks = run_capture(
        Command::new("jj")
            .args(["log", "-r", "jailCommit", "-n", "1", "--no-graph", "-T", "bookmarks", "-R"])
            .arg(&repo),
    );
    assert!(
        bookmarks.contains("jailCommit"),
        "expected jailCommit bookmark, got: {bookmarks}"
    );

    let description = run_capture(
        Command::new("jj")
            .args([
                "log",
                "-r",
                "jailCommit",
                "-n",
                "1",
                "--no-graph",
                "-T",
                "description",
                "-R",
            ])
            .arg(&repo),
    );
    assert!(
        description.contains("intent: test jail commit"),
        "unexpected description: {description}"
    );
}

#[test]
fn bootstrap_rejects_same_working_and_target_bookmarks() {
    let temp = tempfile::tempdir().expect("tempdir");
    let repo = temp.path().join("repo");
    fs::create_dir_all(&repo).expect("create repo dir");

    run_ok(Command::new("jj").args(["git", "init"]).arg(&repo));
    run_ok(
        Command::new("jj")
            .args(["bookmark", "create", "dev", "-r", "@", "-R"])
            .arg(&repo),
    );

    let bin = env!("CARGO_BIN_EXE_mentci-bootstrap");
    let output = Command::new(bin)
        .args(["--repo-root"])
        .arg(&repo)
        .args([
            "--working-bookmark",
            "dev",
            "--target-bookmark",
            "dev",
        ])
        .output()
        .expect("bootstrap command");

    assert!(!output.status.success(), "expected failure for same bookmarks");
    let stderr = String::from_utf8_lossy(&output.stderr);
    assert!(
        stderr.contains("must differ"),
        "expected bookmark mismatch error, got: {stderr}"
    );
}
