use ractor::{Actor, ActorProcessingErr, ActorRef, RpcReplyPort};
use std::fs;
use std::path::Path;
use regex::Regex;
use crate::mentci_box_capnp::link_guard_config;

pub struct LinkGuard;

#[derive(Debug)]
pub enum LinkGuardMessage {
    Check(RpcReplyPort<Result<(), Vec<String>>>),
    CheckWithConfig(Vec<u8>, RpcReplyPort<Result<(), Vec<String>>>),
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
                let errors = self.perform_check(None);
                reply.send(errors)?;
            }
            LinkGuardMessage::CheckWithConfig(data, reply) => {
                let errors = self.perform_check(Some(data));
                reply.send(errors)?;
            }
        }
        Ok(())
    }
}

impl LinkGuard {
    fn perform_check(&self, config_data: Option<Vec<u8>>) -> Result<(), Vec<String>> {
        let mut errors = Vec::new();

        let (roots, rules, allowlist): (Vec<String>, Vec<(String, Regex, String)>, Vec<String>) = if let Some(data) = config_data {
            let message = capnp::serialize_packed::read_message(
                &mut std::io::Cursor::new(data),
                capnp::message::ReaderOptions::new(),
            ).map_err(|e| vec![format!("failed to parse capnp config: {}", e)])?;
            
            let config = message.get_root::<link_guard_config::Reader<'_>>()
                .map_err(|e| vec![format!("failed to get config root: {}", e)])?;

            let roots = config.get_roots().map_err(|e| vec![e.to_string()])?
                .iter().map(|r| r.unwrap().to_str().unwrap().to_string()).collect();
            
            let mut rules_vec = Vec::new();
            for r in config.get_rules().map_err(|e| vec![e.to_string()])? {
                let name = r.get_name().unwrap().to_str().unwrap().to_string();
                let regex = Regex::new(r.get_regex().unwrap().to_str().unwrap()).unwrap();
                let msg = r.get_message().unwrap().to_str().unwrap().to_string();
                rules_vec.push((name, regex, msg));
            }

            let allowlist = config.get_allowlist().map_err(|e| vec![e.to_string()])?
                .iter().map(|a| a.unwrap().to_str().unwrap().to_string()).collect();

            (roots, rules_vec, allowlist)
        } else {
            // Default data
            let roots = vec!["AGENTS.md".to_string(), "Core".to_string(), "Library".to_string(), "Components".to_string(), "Research".to_string(), "Development".to_string()];
            let rules = vec![
                ("Agents".to_string(), Regex::new(r"(?i)Library/architecture/AGENTS\.md").unwrap(), "forbidden reference to Library/architecture/AGENTS.md (use Core/AGENTS.md)".to_string()),
                ("Goals".to_string(), Regex::new(r"(?i)Library/architecture/HIGH_LEVEL_GOALS\.md").unwrap(), "forbidden reference to Library/architecture/HIGH_LEVEL_GOALS.md (use Core/HIGH_LEVEL_GOALS.md)".to_string()),
                ("Restart".to_string(), Regex::new(r"(?i)Library/guides/RestartContext\.md").unwrap(), "forbidden reference to Library/guides/RestartContext.md (use Library/RestartContext.md)".to_string()),
                ("Inputs".to_string(), Regex::new(r#"(?i)`inputs/[^`\n]+`|"inputs/[^"\n]+""#).unwrap(), "lowercase inputs/ path detected".to_string()),
            ];
            let allowlist = vec!["src/actors/link_guard.rs".to_string(), "Components/scripts/reference_guard/main.clj".to_string()];
            (roots, rules, allowlist)
        };

        for root in roots {
            let path = Path::new(&root);
            if !path.exists() { continue; }
            
            for entry in walkdir::WalkDir::new(path).into_iter().flatten() {
                if entry.file_type().is_file() {
                    let path_str = entry.path().to_string_lossy().to_string();
                    if allowlist.iter().any(|a| path_str.contains(a)) {
                        continue;
                    }

                    if let Ok(content) = fs::read_to_string(entry.path()) {
                        for (_, regex, msg) in &rules {
                            if regex.is_match(&content) {
                                if msg.contains("inputs/") && content.contains("inputs/outputs") {
                                    continue;
                                }
                                errors.push(format!("{}: {}", path_str, msg));
                            }
                        }
                    }
                }
            }
        }

        if errors.is_empty() { Ok(()) } else { Err(errors) }
    }
}
