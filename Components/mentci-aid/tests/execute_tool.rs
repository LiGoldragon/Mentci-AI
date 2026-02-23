use std::fs;
use std::os::unix::fs::PermissionsExt;
use std::path::{Path, PathBuf};
use std::process::Command;

fn create_fake_bb(path: &Path, body: &str) -> PathBuf {
    let bb_path = path.join("bb");
    fs::write(&bb_path, format!("#!/bin/sh\nset -eu\n{body}\n")).expect("write fake bb");
    let mut perms = fs::metadata(&bb_path).expect("bb metadata").permissions();
    perms.set_mode(0o755);
    fs::set_permissions(&bb_path, perms).expect("set fake bb mode");
    bb_path
}

fn create_script_root(path: &Path, name: &str) -> PathBuf {
    let scripts = path.join("scripts");
    fs::create_dir_all(scripts.join(name)).expect("create script dir");
    fs::write(
        scripts.join(name).join("main.clj"),
        format!("(println \"{name}\")\n"),
    )
    .expect("write script main");
    scripts
}

#[test]
fn execute_list_uses_scripts_root_and_ignores_non_entry_dirs() {
    let temp = tempfile::tempdir().expect("tempdir");
    let scripts = temp.path().join("scripts");
    fs::create_dir_all(scripts.join("alpha")).expect("create alpha dir");
    fs::create_dir_all(scripts.join("beta")).expect("create beta dir");
    fs::create_dir_all(scripts.join("lib")).expect("create lib dir");
    fs::write(scripts.join("alpha/main.clj"), "(println \"alpha\")\n").expect("write alpha");
    fs::write(scripts.join("beta/main.clj"), "(println \"beta\")\n").expect("write beta");
    fs::write(scripts.join("lib/types.clj"), "{}\n").expect("write lib");

    let output = Command::new(env!("CARGO_BIN_EXE_execute"))
        .arg("list")
        .env("MENTCI_SCRIPTS_DIR", &scripts)
        .output()
        .expect("execute list");

    assert!(output.status.success(), "expected success");
    let stdout = String::from_utf8_lossy(&output.stdout);
    assert!(stdout.contains("alpha"), "expected alpha in list: {stdout}");
    assert!(stdout.contains("beta"), "expected beta in list: {stdout}");
    assert!(!stdout.contains("lib"), "did not expect lib in list: {stdout}");
}

#[test]
fn execute_reports_unknown_script() {
    let temp = tempfile::tempdir().expect("tempdir");
    let scripts = temp.path().join("scripts");
    fs::create_dir_all(scripts.join("alpha")).expect("create alpha dir");
    fs::write(scripts.join("alpha/main.clj"), "(println \"alpha\")\n").expect("write alpha");

    let output = Command::new(env!("CARGO_BIN_EXE_execute"))
        .arg("does-not-exist")
        .env("MENTCI_SCRIPTS_DIR", &scripts)
        .output()
        .expect("execute unknown");

    assert!(!output.status.success(), "expected failure");
    let stderr = String::from_utf8_lossy(&output.stderr);
    assert!(
        stderr.contains("unknown script"),
        "expected unknown script error, got: {stderr}"
    );
}

#[test]
fn execute_forwards_script_path_and_args_to_bb() {
    let temp = tempfile::tempdir().expect("tempdir");
    let scripts = create_script_root(temp.path(), "alpha");
    let out_file = temp.path().join("bb-args.txt");
    let bb_bin = create_fake_bb(temp.path(), "printf \"%s\\n\" \"$@\" > \"$EXECUTE_TEST_OUT\"");

    let output = Command::new(env!("CARGO_BIN_EXE_execute"))
        .arg("alpha")
        .arg("--flag")
        .arg("value")
        .env("MENTCI_SCRIPTS_DIR", &scripts)
        .env("MENTCI_BB_BIN", &bb_bin)
        .env("EXECUTE_TEST_OUT", &out_file)
        .output()
        .expect("execute alpha");

    assert!(output.status.success(), "expected success");
    let args = fs::read_to_string(&out_file).expect("read bb args");
    assert!(
        args.contains("alpha/main.clj"),
        "expected script path in bb args, got: {args}"
    );
    assert!(args.contains("--flag"), "expected forwarded flag, got: {args}");
    assert!(args.contains("value"), "expected forwarded value, got: {args}");
}

#[test]
fn execute_propagates_bb_exit_code() {
    let temp = tempfile::tempdir().expect("tempdir");
    let scripts = create_script_root(temp.path(), "alpha");
    let bb_bin = create_fake_bb(temp.path(), "exit 17");

    let output = Command::new(env!("CARGO_BIN_EXE_execute"))
        .arg("alpha")
        .env("MENTCI_SCRIPTS_DIR", &scripts)
        .env("MENTCI_BB_BIN", &bb_bin)
        .output()
        .expect("execute alpha");

    assert_eq!(output.status.code(), Some(17));
}

#[test]
fn execute_help_mentions_env_overrides() {
    let temp = tempfile::tempdir().expect("tempdir");
    let scripts = create_script_root(temp.path(), "alpha");

    let output = Command::new(env!("CARGO_BIN_EXE_execute"))
        .arg("--help")
        .env("MENTCI_SCRIPTS_DIR", &scripts)
        .output()
        .expect("execute help");

    assert!(output.status.success(), "expected success");
    let stdout = String::from_utf8_lossy(&output.stdout);
    assert!(
        stdout.contains("MENTCI_SCRIPTS_DIR"),
        "expected scripts env in help: {stdout}"
    );
    assert!(
        stdout.contains("MENTCI_BB_BIN"),
        "expected bb env in help: {stdout}"
    );
}
