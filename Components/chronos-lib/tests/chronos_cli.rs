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
fn default_output_matches_am_second_for_unix_epoch() {
    let default_out = run_chronos(&["--unix", "0"]);
    let explicit_out = run_chronos(&["--unix", "0", "--format", "am", "--precision", "second"]);
    assert_eq!(default_out, explicit_out);
    assert_eq!(default_out, "5863.10.11.10.13");
}
