use anyhow::{Context, Result};
use mentci_launch::{LaunchRequest, MentciLaunch};
use std::path::PathBuf;

fn main() -> Result<()> {
    let args: Vec<String> = std::env::args().skip(1).collect();

    if args.is_empty() || args.contains(&"--help".to_string()) || args.contains(&"-h".to_string()) {
        print_help();
        return Ok(());
    }

    let request_path = PathBuf::from(&args[0]);
    let request = LaunchRequest::from_capnp(&request_path)
        .with_context(|| format!("failed to load launch request from {}", request_path.display()))?;

    let launch = MentciLaunch::from_request(request);
    launch.run()
}

fn print_help() {
    println!("mentci-launch: systemd launcher for Mentci-Box terminal and service sessions.");
    println!();
    println!("Usage: mentci-launch <launch-request.capnp>");
    println!();
    println!("The launch request must be a MentciLaunchRequest Cap'n Proto envelope.");
}
