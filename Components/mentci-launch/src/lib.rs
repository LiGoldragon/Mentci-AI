pub mod atom_filesystem_capnp {
    include!(concat!(env!("OUT_DIR"), "/atom_filesystem_capnp.rs"));
}

pub mod mentci_capnp {
    include!(concat!(env!("OUT_DIR"), "/mentci_capnp.rs"));
}

use anyhow::{bail, Context, Result};
use capnp::message::ReaderOptions;
use capnp::serialize_packed;
use std::path::{Path, PathBuf};
use std::process::Command;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct EnvironmentEntry {
    pub key: String,
    pub value: String,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LaunchMode {
    Terminal,
    Service,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LaunchSystemdTarget {
    UserScope,
    UserService,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum AgentInterface {
    PiTypescript,
    PiRust,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct LaunchRequest {
    pub run_id: String,
    pub box_request_capnp_path: PathBuf,
    pub working_directory: PathBuf,
    pub launch_mode: LaunchMode,
    pub systemd_target: LaunchSystemdTarget,
    pub terminal_program: String,
    pub terminal_args: Vec<String>,
    pub service_unit_name: String,
    pub environment: Vec<EnvironmentEntry>,
    pub agent_interface: AgentInterface,
    pub test_profile: String,
}

impl LaunchRequest {
    pub fn from_capnp(path: &Path) -> Result<Self> {
        let file = std::fs::File::open(path)
            .with_context(|| format!("failed to open launch request {:?}", path))?;
        let mut reader = std::io::BufReader::new(file);
        let message = serialize_packed::read_message(&mut reader, ReaderOptions::new())
            .with_context(|| format!("failed to decode launch request {:?}", path))?;
        let request = message
            .get_root::<mentci_capnp::mentci_launch_request::Reader<'_>>()
            .context("failed to read MentciLaunchRequest root")?;

        let launch_mode = match request.get_launch_mode().context("missing launchMode")? {
            mentci_capnp::MentciLaunchMode::Terminal => LaunchMode::Terminal,
            mentci_capnp::MentciLaunchMode::Service => LaunchMode::Service,
        };

        let systemd_target = match request
            .get_systemd_target()
            .context("missing systemdTarget")?
        {
            mentci_capnp::MentciLaunchSystemdTarget::UserScope => LaunchSystemdTarget::UserScope,
            mentci_capnp::MentciLaunchSystemdTarget::UserService => {
                LaunchSystemdTarget::UserService
            }
        };

        let agent_interface = match request
            .get_agent_interface()
            .context("missing agentInterface")?
        {
            mentci_capnp::MentciAgentInterface::PiTypescript => AgentInterface::PiTypescript,
            mentci_capnp::MentciAgentInterface::PiRust => AgentInterface::PiRust,
        };

        let terminal_args = request
            .get_terminal_args()
            .context("missing terminalArgs")?
            .iter()
            .map(|arg| {
                arg.and_then(|text| {
                    text.to_str()
                        .map(|v| v.to_string())
                        .map_err(capnp::Error::from)
                })
            })
            .collect::<capnp::Result<Vec<String>>>()
            .context("failed to decode terminalArgs")?;

        let environment = request
            .get_environment()
            .context("missing environment")?
            .iter()
            .map(|entry| {
                let key = entry
                    .get_key()
                    .and_then(|v| v.to_str().map(|s| s.to_string()).map_err(capnp::Error::from))?;
                let value = entry
                    .get_value()
                    .and_then(|v| v.to_str().map(|s| s.to_string()).map_err(capnp::Error::from))?;
                Ok(EnvironmentEntry { key, value })
            })
            .collect::<capnp::Result<Vec<EnvironmentEntry>>>()
            .context("failed to decode environment entries")?;

        Ok(Self {
            run_id: request
                .get_run_id()
                .and_then(|v| v.to_str().map(|s| s.to_string()).map_err(capnp::Error::from))
                .context("missing runId")?,
            box_request_capnp_path: PathBuf::from(
                request
                    .get_box_request_capnp_path()
                    .and_then(|v| {
                        v.to_str()
                            .map(|s| s.to_string())
                            .map_err(capnp::Error::from)
                    })
                    .context("missing boxRequestCapnpPath")?,
            ),
            working_directory: PathBuf::from(
                request
                    .get_working_directory()
                    .and_then(|v| {
                        v.to_str()
                            .map(|s| s.to_string())
                            .map_err(capnp::Error::from)
                    })
                    .context("missing workingDirectory")?,
            ),
            launch_mode,
            systemd_target,
            terminal_program: request
                .get_terminal_program()
                .and_then(|v| v.to_str().map(|s| s.to_string()).map_err(capnp::Error::from))
                .context("missing terminalProgram")?,
            terminal_args,
            service_unit_name: request
                .get_service_unit_name()
                .and_then(|v| v.to_str().map(|s| s.to_string()).map_err(capnp::Error::from))
                .context("missing serviceUnitName")?,
            environment,
            agent_interface,
            test_profile: request
                .get_test_profile()
                .and_then(|v| v.to_str().map(|s| s.to_string()).map_err(capnp::Error::from))
                .context("missing testProfile")?,
        })
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct LaunchPlan {
    pub program: String,
    pub args: Vec<String>,
    pub working_directory: PathBuf,
}

impl LaunchPlan {
    pub fn from_request(request: &LaunchRequest) -> Result<Self> {
        let mut args = vec![
            "--user".to_string(),
            format!("--working-directory={}", request.working_directory.display()),
        ];

        for env in &request.environment {
            args.push(format!("--setenv={}={}", env.key, env.value));
        }

        match request.launch_mode {
            LaunchMode::Terminal => {
                if request.systemd_target != LaunchSystemdTarget::UserScope {
                    bail!("terminal launch mode requires userScope systemd target");
                }
                args.push("--scope".to_string());
                args.push("--".to_string());
                args.extend(Self::terminal_command_from_request(request));
            }
            LaunchMode::Service => {
                if request.systemd_target != LaunchSystemdTarget::UserService {
                    bail!("service launch mode requires userService systemd target");
                }

                if request.service_unit_name.trim().is_empty() {
                    bail!("serviceUnitName is required in service launch mode");
                }

                args.push(format!("--unit={}", request.service_unit_name));
                args.push("--collect".to_string());
                args.push("--".to_string());
                args.extend(Self::box_command_from_request(request));
            }
        }

        Ok(Self {
            program: "systemd-run".to_string(),
            args,
            working_directory: request.working_directory.clone(),
        })
    }

    fn terminal_command_from_request(request: &LaunchRequest) -> Vec<String> {
        let mut command = vec![request.terminal_program.clone()];

        if request.terminal_args.is_empty() {
            command.push("-e".to_string());
            command.extend(Self::box_command_from_request(request));
            return command;
        }

        command.extend(request.terminal_args.iter().cloned());
        command.extend(Self::box_command_from_request(request));
        command
    }

    fn box_command_from_request(request: &LaunchRequest) -> Vec<String> {
        vec![
            "mentci-box".to_string(),
            request.box_request_capnp_path.display().to_string(),
        ]
    }
}

pub trait LaunchRunner {
    fn launch_from_plan(&self, plan: &LaunchPlan) -> Result<()>;
}

pub struct SystemdRunner;

impl LaunchRunner for SystemdRunner {
    fn launch_from_plan(&self, plan: &LaunchPlan) -> Result<()> {
        let status = Command::new(&plan.program)
            .args(&plan.args)
            .current_dir(&plan.working_directory)
            .status()
            .with_context(|| {
                format!(
                    "failed to execute launch command: {} {:?}",
                    plan.program, plan.args
                )
            })?;

        if status.success() {
            return Ok(());
        }

        bail!(
            "launch command failed with exit code {:?}: {} {:?}",
            status.code(),
            plan.program,
            plan.args
        )
    }
}

pub struct MentciLaunch {
    pub request: LaunchRequest,
}

impl MentciLaunch {
    pub fn from_request(request: LaunchRequest) -> Self {
        Self { request }
    }

    pub fn to_plan(&self) -> Result<LaunchPlan> {
        LaunchPlan::from_request(&self.request)
    }

    pub fn run(&self) -> Result<()> {
        self.run_for(&SystemdRunner)
    }

    pub fn run_for(&self, runner: &dyn LaunchRunner) -> Result<()> {
        let plan = self.to_plan()?;
        runner.launch_from_plan(&plan)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn terminal_request() -> LaunchRequest {
        LaunchRequest {
            run_id: "run-1".to_string(),
            box_request_capnp_path: PathBuf::from("/tmp/box-request.capnp"),
            working_directory: PathBuf::from("/tmp/workdir"),
            launch_mode: LaunchMode::Terminal,
            systemd_target: LaunchSystemdTarget::UserScope,
            terminal_program: "foot".to_string(),
            terminal_args: Vec::new(),
            service_unit_name: String::new(),
            environment: vec![EnvironmentEntry {
                key: "PI_MODE".to_string(),
                value: "test".to_string(),
            }],
            agent_interface: AgentInterface::PiTypescript,
            test_profile: "interactive".to_string(),
        }
    }

    #[test]
    fn terminal_plan_uses_scope_and_foot_exec_default() {
        let request = terminal_request();
        let plan = LaunchPlan::from_request(&request).expect("build terminal plan");

        assert_eq!(plan.program, "systemd-run");
        assert!(plan.args.contains(&"--user".to_string()));
        assert!(plan.args.contains(&"--scope".to_string()));
        assert!(
            plan.args
                .contains(&"--working-directory=/tmp/workdir".to_string())
        );
        assert!(plan.args.contains(&"--setenv=PI_MODE=test".to_string()));
        assert!(plan.args.contains(&"foot".to_string()));
        assert!(plan.args.contains(&"-e".to_string()));
        assert!(plan.args.contains(&"mentci-box".to_string()));
        assert!(
            plan.args
                .contains(&"/tmp/box-request.capnp".to_string())
        );
    }

    #[test]
    fn terminal_mode_requires_user_scope_target() {
        let mut request = terminal_request();
        request.systemd_target = LaunchSystemdTarget::UserService;

        let error = LaunchPlan::from_request(&request).expect_err("must fail");
        let message = error.to_string();
        assert!(
            message.contains("terminal launch mode requires userScope"),
            "unexpected error: {message}"
        );
    }

    #[test]
    fn service_mode_requires_unit_name() {
        let mut request = terminal_request();
        request.launch_mode = LaunchMode::Service;
        request.systemd_target = LaunchSystemdTarget::UserService;
        request.service_unit_name = "   ".to_string();

        let error = LaunchPlan::from_request(&request).expect_err("must fail");
        let message = error.to_string();
        assert!(
            message.contains("serviceUnitName is required"),
            "unexpected error: {message}"
        );
    }

    #[test]
    fn service_plan_uses_user_unit_launch() {
        let mut request = terminal_request();
        request.launch_mode = LaunchMode::Service;
        request.systemd_target = LaunchSystemdTarget::UserService;
        request.service_unit_name = "mentci-launch-test.service".to_string();

        let plan = LaunchPlan::from_request(&request).expect("build service plan");
        assert_eq!(plan.program, "systemd-run");
        assert!(plan.args.contains(&"--user".to_string()));
        assert!(
            plan.args
                .contains(&"--unit=mentci-launch-test.service".to_string())
        );
        assert!(plan.args.contains(&"--collect".to_string()));
        assert!(plan.args.contains(&"mentci-box".to_string()));
    }

    #[derive(Default)]
    struct RecordingRunner {
        plans: std::sync::Mutex<Vec<LaunchPlan>>,
    }

    impl LaunchRunner for RecordingRunner {
        fn launch_from_plan(&self, plan: &LaunchPlan) -> Result<()> {
            self.plans.lock().expect("lock").push(plan.clone());
            Ok(())
        }
    }

    #[test]
    fn launch_runs_through_runner_contract() {
        let launcher = MentciLaunch::from_request(terminal_request());
        let runner = RecordingRunner::default();

        launcher.run_for(&runner).expect("runner should succeed");

        let plans = runner.plans.lock().expect("lock");
        assert_eq!(plans.len(), 1);
        assert_eq!(plans[0].program, "systemd-run");
    }
}
