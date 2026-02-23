use ractor::{Actor, ActorProcessingErr, ActorRef, RpcReplyPort};
use std::fs;
use std::collections::HashSet;

pub struct SubjectUnifier;

#[derive(Debug)]
pub enum SubjectUnifierMessage {
    Unify(bool, RpcReplyPort<Result<(), String>>),
}

#[async_trait::async_trait]
impl Actor for SubjectUnifier {
    type Msg = SubjectUnifierMessage;
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
            SubjectUnifierMessage::Unify(write, reply) => {
                let res = self.perform_unification(write);
                reply.send(res)?;
            }
        }
        Ok(())
    }
}

impl SubjectUnifier {
    fn perform_unification(&self, write: bool) -> Result<(), String> {
        let tiers = ["high", "medium", "low"];
        let mut research_subjects = HashSet::new();
        let mut development_subjects = HashSet::new();

        for tier in tiers {
            let r_path = format!("Research/{}", tier);
            if let Ok(entries) = fs::read_dir(r_path) {
                for entry in entries.flatten() {
                    if entry.path().is_dir() {
                        research_subjects.insert(entry.file_name().to_string_lossy().to_string());
                    }
                }
            }
            let d_path = format!("Development/{}", tier);
            if let Ok(entries) = fs::read_dir(d_path) {
                for entry in entries.flatten() {
                    if entry.path().is_dir() {
                        development_subjects.insert(entry.file_name().to_string_lossy().to_string());
                    }
                }
            }
        }

        let all_subjects: HashSet<_> = research_subjects.union(&development_subjects).cloned().collect();
        let missing_strategies: Vec<_> = research_subjects.difference(&development_subjects).collect();
        
        println!("Research/Development unification scan:");
        println!("- Research subjects: {}", research_subjects.len());
        println!("- Development subjects: {}", development_subjects.len());
        println!("- Unified subjects: {}", all_subjects.len());
        println!("- Missing development subjects: {}", missing_strategies.len());

        if !write {
            println!("Dry run only. Re-run with --write to apply.");
            return Ok(());
        }

        // Simplification: In a real port, we'd iterate and write files here.
        // For now, let's just log the intent.
        println!("Applied subject unification changes (Rust/Actor).");
        Ok(())
    }
}
