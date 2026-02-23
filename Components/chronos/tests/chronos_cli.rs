use std::process::Command;

fn run_chronos(args: &[&str]) -> String {
    let output = Command::new(env!("CARGO_BIN_EXE_chronos"))
        .args(args)
        .output()
        .expect("run chronos");
    assert!(output.status.success(), "chronos command failed");
    String::from_utf8_lossy(&output.stdout).trim().to_string()
}

#[test]
fn chronos_am_output_is_stable_for_unix_epoch() {
    let out = run_chronos(&["--unix", "0", "--format", "am", "--precision", "second"]);
    assert_eq!(out, "5863.10.11.10.13");
}

#[test]
fn chronos_version_output_is_stable_for_unix_epoch() {
    let out = run_chronos(&["--unix", "0", "--format", "version", "--precision", "second"]);
    assert_eq!(out, "v-56.10.11.10.13");
}

#[test]
fn chronos_numeric_output_supports_minute_precision() {
    let out = run_chronos(&["--unix", "0", "--format", "numeric", "--precision", "minute"]);
    assert_eq!(out, "10.11.10 | 5863 AM");
}
