use anyhow::Result;
use mentci_box_lib::{Sandbox, SandboxConfig};

pub fn run_from_args(mut args: Vec<String>) -> Result<()> {
    if args.first().map(String::as_str) == Some("sandbox") {
        args.remove(0);
    }
    let config = SandboxConfig::from_args(args)?;
    let sandbox = Sandbox::from_config(config);
    sandbox.run()
}
