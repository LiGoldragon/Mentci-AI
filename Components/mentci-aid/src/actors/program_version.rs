use ractor::{Actor, ActorProcessingErr, ActorRef, RpcReplyPort};
use sha2::{Sha256, Digest};
use std::fs;
use std::path::Path;
use walkdir::WalkDir;

pub struct ProgramVersion;

#[derive(Debug)]
pub enum ProgramVersionMessage {
    Get(RpcReplyPort<String>),
}

const JJ_ALPHABET: &str = "zkwpqrstnmvxlhgybjdf0123456789";

#[async_trait::async_trait]
impl Actor for ProgramVersion {
    type Msg = ProgramVersionMessage;
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
            ProgramVersionMessage::Get(reply) => {
                let version = self.calculate_version();
                reply.send(version)?;
            }
        }
        Ok(())
    }
}

impl ProgramVersion {
    fn calculate_version(&self) -> String {
        let core_path = if Path::new("Core").exists() { "Core" } else { "core" };
        let mut files: Vec<_> = WalkDir::new(core_path)
            .into_iter()
            .flatten()
            .filter(|e| e.file_type().is_file())
            .collect();
        files.sort_by_key(|e| e.path().to_path_buf());

        let mut hasher = Sha256::new();
        for file in files {
            if let Ok(content) = fs::read(file.path()) {
                hasher.update(content);
            }
        }
        let hash = hasher.finalize();
        self.encode_jj(&hash, 8)
    }

    fn encode_jj(&self, bytes: &[u8], length: usize) -> String {
        let mut big_int = num_bigint::BigUint::from_bytes_be(bytes);
        let base = num_bigint::BigUint::from(JJ_ALPHABET.len());
        let mut res = String::new();

        while big_int > num_bigint::BigUint::from(0u32) && res.len() < length {
            let rem = &big_int % &base;
            let idx = rem.to_u32_digits().first().cloned().unwrap_or(0) as usize;
            res.push(JJ_ALPHABET.chars().nth(idx).unwrap());
            big_int /= &base;
        }
        res.chars().rev().collect()
    }
}
