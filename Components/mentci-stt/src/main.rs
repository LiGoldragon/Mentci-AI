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
    file: std::path::PathBuf,
}

#[tokio::main]
async fn main() -> Result<()> {
    let opt = Opt::from_args();

    let api_key = env::var("GEMINI_API_KEY")
        .context("GEMINI_API_KEY environment variable is not set")?;

    let audio_data = fs::read(&opt.file)
        .with_context(|| format!("Failed to read audio file: {:?}", opt.file))?;

    let encoded_audio = base64::engine::general_purpose::STANDARD.encode(audio_data);

    let client = Client::new();
    let url = format!(
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={}",
        api_key
    );

    let prompt_text = "You are an expert transcriber and linguist working on the Mentci-AI project (created by Li Goldragon). \
Please transcribe the following audio recording with extremely high fidelity. \
The recording may contain specialized vocabulary related to the project, including: \
- Mentci, mentci-aid, Mentci-Box \
- Aski (a highly concise ASCII-based representation language) \
- Lojix (an EDN-based Aski dialect) \
- Criome (the broader ecosystem/operating system) \
- SEMA (the binary object format, Cap'n Proto) \
- Rust, Clojure, Nix, Jujutsu (jj), EDN \
\
Furthermore, it is critical that you pick up on emotional emphasis. If the speaker places heavy emphasis, passion, urgency, or hesitation on certain words or phrases, indicate that in the transcription using markdown (e.g., *italics* for light emphasis, **bold** for strong emphasis, [pauses] or [sighs] where appropriate). \
\
Return ONLY the high-fidelity transcription.";

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
