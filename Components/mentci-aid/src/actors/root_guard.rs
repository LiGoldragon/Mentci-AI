use ractor::{Actor, ActorProcessingErr, ActorRef, RpcReplyPort};
use std::collections::HashSet;
use std::fs;

pub struct RootGuard;

#[derive(Debug)]
pub enum RootGuardMessage {
    Check(RpcReplyPort<Result<(), Vec<String>>>),
}

#[async_trait::async_trait]
impl Actor for RootGuard {
    type Msg = RootGuardMessage;
    type State = ();
    type Arguments = ();

    async fn pre_start(
        &self,
        _myself: ActorRef<Self::Msg>,
        _args: Self::Arguments,
    ) -> Result<Self::State, ActorProcessingErr> {
        Ok(())
    }

    async fn handle(
        &self,
        _myself: ActorRef<Self::Msg>,
        message: Self::Msg,
        _state: &mut Self::State,
    ) -> Result<(), ActorProcessingErr> {
        match message {
            RootGuardMessage::Check(reply) => {
                let errors = self.perform_check();
                if errors.is_empty() {
                    reply.send(Ok(()))?;
                } else {
                    reply.send(Err(errors))?;
                }
            }
        }
        Ok(())
    }
}

impl RootGuard {
    fn perform_check(&self) -> Vec<String> {
        let allowed_domain_dirs: HashSet<&str> = ["Sources", "Components", "Outputs", "Research", "Development", "Core", "Library"].into_iter().collect();
        let allowed_runtime_dirs: HashSet<&str> = [".git", ".jj", ".direnv", "target", ".mentci"].into_iter().collect();
        let allowed_top_files: HashSet<&str> = ["flake.nix", "flake.lock", ".gitignore", ".envrc", "AGENTS.md", "README.md", ".attrs.json", ".opencode.edn"].into_iter().collect();

        let mut errors = Vec::new();
        if let Ok(entries) = fs::read_dir(".") {
            for entry in entries.flatten() {
                let name = entry.file_name().to_string_lossy().to_string();
                let is_dir = entry.path().is_dir();

                if is_dir {
                    if !allowed_domain_dirs.contains(name.as_str()) && !allowed_runtime_dirs.contains(name.as_str()) && !name.starts_with(".jj_") {
                        errors.push(format!("unexpected top-level directory: {}", name));
                    }
                } else if !allowed_top_files.contains(name.as_str()) {
                    errors.push(format!("unexpected top-level file: {}", name));
                }
            }
        }
        errors
    }
}
