
pub mod schema;
pub use schema::{atom_filesystem_capnp, mentci_capnp};
use schema::mentci_capnp::stt_request;
use std::io::Read;
use structopt::StructOpt;
use std::env;
use std::fs;
use base64::Engine;
use anyhow::{Context, Result};
use reqwest::Client;
use serde_json::json;

#[derive(Debug, StructOpt)]
#[structopt(name = "mentci-stt", about = "Mentci-AI Speech-to-Text via Gemini")]
struct Opt {
    #[structopt(short, long, parse(from_os_str))]
    file: Option<std::path::PathBuf>,
    
    #[structopt(short, long, parse(from_os_str))]
    capnp: Option<std::path::PathBuf>,
}

#[tokio::main]
async fn main() -> Result<()> {
    let opt = Opt::from_args();

    let api_key = if let Ok(key) = env::var("GEMINI_API_KEY") {
        key
    } else {
        mentci_user::get_secret("GEMINI_API_KEY")?
            .context("GEMINI_API_KEY not set and not found in mentci-user config")?
    };

    let mut audio_path;
    let mut model_name = "gemini-2.5-flash".to_string();
    let mut vocabulary = vec![];

    if let Some(capnp_path) = opt.capnp {
        let mut file = fs::File::open(&capnp_path)?;
        let mut buffer = Vec::new();
        file.read_to_end(&mut buffer)?;
        
        let mut slice = &buffer[..];
        let message_reader = capnp::serialize_packed::read_message(&mut slice, capnp::message::ReaderOptions::new())?;
        let request = message_reader.get_root::<stt_request::Reader>()?;
        
        audio_path = std::path::PathBuf::from(request.get_audio_path()?.to_string()?);
        model_name = request.get_model()?.to_string()?;
        
        for word in request.get_vocabulary()? {
            vocabulary.push(word?.to_string()?);
        }
    } else if let Some(file_path) = opt.file {
        audio_path = file_path;
        vocabulary = vec![
            "Mentci".to_string(),
            "mentci-aid".to_string(),
            "Mentci-Box".to_string(),
            "Aski".to_string(),
            "Lojix".to_string(),
            "Criome".to_string(),
            "SEMA".to_string(),
            "Rust".to_string(),
            "Clojure".to_string(),
            "Nix".to_string(),
            "Jujutsu (jj)".to_string(),
            "EDN".to_string(),
        ];
    } else {
        anyhow::bail!("Must provide either --file <audio_path> or --capnp <request_path>");
    }

    let audio_data = fs::read(&audio_path)
        .with_context(|| format!("Failed to read audio file: {:?}", audio_path))?;

    let encoded_audio = base64::engine::general_purpose::STANDARD.encode(audio_data);

    let client = Client::new();
    let url = format!(
        "https://generativelanguage.googleapis.com/v1beta/models/{}:generateContent?key={}",
        model_name, api_key
    );

    let vocab_str = vocabulary.join(", ");
    let prompt_text = format!(
        "You are an expert transcriber and linguist working on the Mentci-AI project (created by Li Goldragon). \
Please transcribe the following audio recording with extremely high fidelity. \
The recording may contain specialized vocabulary related to the project, including: {} \
\
Furthermore, it is critical that you pick up on emotional emphasis. If the speaker places heavy emphasis, passion, urgency, or hesitation on certain words or phrases, indicate that in the transcription using markdown (e.g., *italics* for light emphasis, **bold** for strong emphasis, [pauses] or [sighs] where appropriate). \
\
Return ONLY the high-fidelity transcription.",
        vocab_str
    );

    let payload = json!({
        "contents": [
            {
                "parts": [
                    { "text": prompt_text },
                    {
                        "inline_data": {
                            "mime_type": "audio/ogg; codecs=opus",
                            "data": encoded_audio
                        }
                    }
                ]
            }
        ]
    });

    let response = client
        .post(&url)
        .header("Content-Type", "application/json")
        .json(&payload)
        .send()
        .await
        .context("Failed to send request to Gemini API")?;

    if response.status().is_success() {
        let result: serde_json::Value = response.json().await?;
        if let Some(text) = result["candidates"][0]["content"]["parts"][0]["text"].as_str() {
            println!("{}", text);
            
            // Call chronos to get timestamp
            let chronos_output = std::process::Command::new("chronos")
                .args(&["--format", "am", "--precision", "second"])
                .output()
                .map_err(|e| anyhow::anyhow!("Failed to execute chronos: {}", e));
                
            match chronos_output {
                Ok(output) if output.status.success() => {
                    let solar_date = String::from_utf8_lossy(&output.stdout).trim().replace(" ", "_").replace(":", "-");
                    let transcript_dir = std::path::Path::new(".voice-recordings/transcripts");
                    if let Err(e) = fs::create_dir_all(transcript_dir) {
                        eprintln!("Failed to create transcript directory: {}", e);
                    } else {
                        let filename = format!("{}.md", solar_date);
                        let filepath = transcript_dir.join(filename);
                        if let Err(e) = fs::write(&filepath, text) {
                            eprintln!("Failed to write transcript file: {}", e);
                        } else {
                            println!("\n[Transcript saved to {:?}]", filepath);
                        }
                    }
                }
                Ok(output) => {
                    eprintln!("chronos failed: {}", String::from_utf8_lossy(&output.stderr));
                }
                Err(e) => {
                    eprintln!("Error calling chronos: {}", e);
                }
            }
        } else {
            eprintln!("Failed to parse response format:\n{:#?}", result);
        }
    } else {
        let status = response.status();
        let error_text = response.text().await.unwrap_or_default();
        anyhow::bail!("Error {}: {}", status, error_text);
    }

    Ok(())
}
