use mentci_launch::mentci_capnp;
use mentci_launch::{
    AgentInterface, LaunchMode, LaunchRequest, LaunchSystemdTarget, MentciLaunch,
};
use std::io::BufWriter;
use std::path::Path;

fn write_request(path: &Path) {
    let mut message = capnp::message::Builder::new_default();
    {
        let mut root = message.init_root::<mentci_capnp::mentci_launch_request::Builder<'_>>();
        root.set_run_id("run-capnp");
        root.set_box_request_capnp_path("/tmp/mentci-box-request.capnp");
        root.set_working_directory("/tmp/mentci-launch");
        root.set_launch_mode(mentci_capnp::MentciLaunchMode::Terminal);
        root.set_systemd_target(mentci_capnp::MentciLaunchSystemdTarget::UserScope);
        root.set_terminal_program("alacritty");
        root.set_service_unit_name("");
        root.set_agent_interface(mentci_capnp::MentciAgentInterface::PiTypescript);
        root.set_test_profile("interactive");

        let mut terminal_args = root.reborrow().init_terminal_args(1);
        terminal_args.set(0, "--class=Mentci");

        let mut environment = root.reborrow().init_environment(1);
        {
            let mut entry = environment.reborrow().get(0);
            entry.set_key("PI_SESSION");
            entry.set_value("test");
        }
    }

    let file = std::fs::File::create(path).expect("create capnp file");
    let mut writer = BufWriter::new(file);
    capnp::serialize_packed::write_message(&mut writer, &message).expect("write capnp file");
}

#[test]
fn launch_request_decodes_capnp_and_builds_plan() {
    let temp = tempfile::tempdir().expect("tempdir");
    let request_path = temp.path().join("launch-request.capnp");
    write_request(&request_path);

    let request = LaunchRequest::from_capnp(&request_path).expect("decode request");
    assert_eq!(request.run_id, "run-capnp");
    assert_eq!(request.launch_mode, LaunchMode::Terminal);
    assert_eq!(request.systemd_target, LaunchSystemdTarget::UserScope);
    assert_eq!(request.agent_interface, AgentInterface::PiTypescript);
    assert_eq!(request.environment.len(), 1);
    assert_eq!(request.terminal_args, vec!["--class=Mentci".to_string()]);

    let launcher = MentciLaunch::from_request(request);
    let plan = launcher.to_plan().expect("build launch plan");
    assert_eq!(plan.program, "systemd-run");
    assert!(plan.args.contains(&"foot".to_string()));
    assert!(plan.args.contains(&"mentci-box".to_string()));
}
