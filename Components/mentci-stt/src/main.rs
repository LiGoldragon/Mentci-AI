
pub mod schema;
pub use schema::{atom_filesystem_capnp, mentci_capnp};
use schema::mentci_capnp::stt_request;
use std::io::Read;
use structopt::StructOpt;
use std::fs;
use base64::Engine;
use anyhow::{Context, Result};
use reqwest::Client;
use serde_json::json;

#[derive(Debug, StructOpt)]
#[structopt(name = "mentci-stt", about = "Mentci-AI Speech-to-Text via Cap'n Proto Config")]
struct Opt {
    #[structopt(short, long, parse(from_os_str))]
    audio: Option<std::path::PathBuf>,
    
    #[structopt(short, long, parse(from_os_str))]
    capnp: std::path::PathBuf,
}

#[tokio::main]
async fn main() -> Result<()> {
    let opt = Opt::from_args();

    let mut file = fs::File::open(&opt.capnp)
        .with_context(|| format!("Failed to open config: {:?}", opt.capnp))?;
    let mut buffer = Vec::new();
    file.read_to_end(&mut buffer)?;
    
    let mut slice = &buffer[..];
    let message_reader = capnp::serialize_packed::read_message(&mut slice, capnp::message::ReaderOptions::new())?;
    let request = message_reader.get_root::<stt_request::Reader>()?;
    
    let audio_path = if let Some(a) = opt.audio {
        a
    } else {
        std::path::PathBuf::from(request.get_audio_path()?.to_string()?)
    };

    let secret_name = request.get_api_key_secret_name()?.to_string()?;
    let api_key = mentci_user::get_secret(&secret_name)?
        .context(format!("Secret {} not found in mentci-user config or env", secret_name))?;

    let model_name = request.get_model()?.to_string()?;
    let provider_url = request.get_provider_url()?.to_string()?;
    let url = format!("{}/{}:generateContent?key={}", provider_url, model_name, api_key);

    let mut vocabulary = vec![];
    for word in request.get_vocabulary()? {
        vocabulary.push(word?.to_string()?);
    }
    let vocab_str = vocabulary.join(", ");

    let mut prompt_text = request.get_base_prompt()?.to_string()?;
    
    // Add specialized vocabulary with specific instructions
    let stt_instructions = request.get_critical_phonetic_instructions()?.to_string()?;

    let vocabulary_preamble_template = request.get_vocabulary_preamble_template()?.to_string()?;
            let vocabulary_preamble = vocabulary_preamble_template.replace("{vocabulary}", &vocab_str);
        prompt_text = format!("{} {}\n\n{}", prompt_text, stt_instructions, vocabulary_preamble);

    if request.get_include_emotional_emphasis() {
        let emo = request.get_emotional_emphasis_instruction()?.to_string()?;
        prompt_text = format!("{} {}", prompt_text, emo);
    }

    let mime_type = request.get_mime_type()?.to_string()?;

    let audio_data = fs::read(&audio_path)
        .with_context(|| format!("Failed to read audio file: {:?}", audio_path))?;

    let encoded_audio = base64::engine::general_purpose::STANDARD.encode(audio_data);

    let client = Client::new();
    let payload = json!({
        "contents": [
            {
                "parts": [
                    { "text": prompt_text },
                    {
                        "inline_data": {
                            "mime_type": mime_type,
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
        .context("Failed to send request to API")?;

    if response.status().is_success() {
        let result: serde_json::Value = response.json().await?;
        if let Some(text) = result["candidates"][0]["content"]["parts"][0]["text"].as_str() {
            println!("{}", text);
            
            // Call chronos to get timestamp
            let chronos_format = request.get_chronos_format()?.to_string()?;
                        let chronos_precision = request.get_chronos_precision()?.to_string()?;
                        let chronos_output = std::process::Command::new("chronos")
                            .args(&["--format", chronos_format.as_str(), "--precision", chronos_precision.as_str()])
                            .output()
                            .map_err(|e| anyhow::anyhow!("Failed to execute chronos: {}", e));
                
            match chronos_output {
                Ok(output) if output.status.success() => {
                    let solar_date = String::from_utf8_lossy(&output.stdout).trim().replace(" ", "_").replace(":", "-");
                    let transcript_dir_value = request.get_transcript_dir()?.to_string()?;
                                        let transcript_dir_path = std::path::PathBuf::from(transcript_dir_value);
                                        let transcript_dir = transcript_dir_path.as_path();
                    if let Err(e) = fs::create_dir_all(transcript_dir) {
                        eprintln!("Failed to create transcript directory: {}", e);
                    } else {
                        let filename = request.get_transcript_filename_template()?.to_string()?.replace("{solar_date}", &solar_date);
                        let filepath = transcript_dir.join(filename);
                        if let Err(e) = fs::write(&filepath, text) {
                            eprintln!("Failed to write transcript file: {}", e);
                        } else {
                            eprintln!("\n[Transcript saved to {:?}]", filepath);
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
