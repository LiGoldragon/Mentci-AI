use ractor::{Actor, ActorProcessingErr, ActorRef, RpcReplyPort};
use std::fs;
use std::path::Path;
use regex::Regex;

pub struct LinkGuard;

#[derive(Debug)]
pub enum LinkGuardMessage {
    Check(RpcReplyPort<Result<(), Vec<String>>>),
}

#[async_trait::async_trait]
impl Actor for LinkGuard {
    type Msg = LinkGuardMessage;
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
            LinkGuardMessage::Check(reply) => {
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

impl LinkGuard {
    fn perform_check(&self) -> Vec<String> {
        let roots = ["AGENTS.md", "Core", "Library", "Components", "Research", "Development"];
        let mut errors = Vec::new();
        
        let agents_regex = Regex::new(r"(?i)Library/architecture/AGENTS\.md").unwrap();
        let goals_regex = Regex::new(r"(?i)Library/architecture/HIGH_LEVEL_GOALS\.md").unwrap();
        let restart_regex = Regex::new(r"(?i)Library/guides/RestartContext\.md").unwrap();
        let inputs_regex = Regex::new(r#"(?i)`inputs/[^`\n]+`|"inputs/[^"\n]+""#).unwrap();

        let allowlist = [
            "Components/mentci-aid/src/actors/link_guard.rs",
            "Components/scripts/reference_guard/main.clj",
        ];

        for root in roots {
            let path = Path::new(root);
            if !path.exists() { continue; }
            
            for entry in walkdir::WalkDir::new(path).into_iter().flatten() {
                if entry.file_type().is_file() {
                    let path_str = entry.path().to_string_lossy().to_string();
                    if allowlist.iter().any(|&a| path_str.contains(a)) {
                        continue;
                    }

                    if let Ok(content) = fs::read_to_string(entry.path()) {
                        if agents_regex.is_match(&content) {
                            errors.push(format!("{}: forbidden reference to Library/architecture/AGENTS.md (use Core/AGENTS.md)", path_str));
                        }
                        if goals_regex.is_match(&content) {
                            errors.push(format!("{}: forbidden reference to Library/architecture/HIGH_LEVEL_GOALS.md (use Core/HIGH_LEVEL_GOALS.md)", path_str));
                        }
                        if restart_regex.is_match(&content) {
                            errors.push(format!("{}: forbidden reference to Library/guides/RestartContext.md (use Library/RestartContext.md)", path_str));
                        }
                        if inputs_regex.is_match(&content) {
                            if !content.contains("inputs/outputs") {
                                errors.push(format!("{}: lowercase inputs/ path detected", path_str));
                            }
                        }
                    }
                }
            }
        }
        errors
    }
}
