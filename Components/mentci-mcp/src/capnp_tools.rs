use anyhow::{Context, Result};
use std::process::Command;
use std::io::Write;
use sha2::{Sha256, Digest};
use std::path::Path;

pub fn capnp_sync_protocol(schema_path: &str, root_struct: &str, text_source_path: &str, output_dir: &str, base_name: &str) -> Result<String> {
    // 1. Read EDN
    let edn_content = std::fs::read_to_string(text_source_path).context("Failed to read text source")?;
    
    // 2. Hash it
    let mut hasher = Sha256::new();
    hasher.update(edn_content.as_bytes());
    let hash = format!("{:x}", hasher.finalize())[..16].to_string();

    // 3. Convert EDN to Capnp Text
    let capnp_txt = aski_lib::projector::edn_to_capnp_txt(&edn_content).context("Failed to convert EDN to capnp format")?;

    // 4. Encode to binary
    let bin = run_capnp_encode(schema_path, root_struct, &capnp_txt, true)?;

    // 5. Write to output_dir
    let output_dir_path = Path::new(output_dir);
    if !output_dir_path.exists() {
        std::fs::create_dir_all(output_dir_path)?;
    }
    
    let bin_filename = format!("{}_{}.bin", base_name, hash);
    let bin_path = output_dir_path.join(&bin_filename);
    std::fs::write(&bin_path, bin).context("Failed to write binary file")?;

    // 6. Update symlink
    let symlink_path = output_dir_path.join(format!("{}.bin", base_name));
    let _ = std::fs::remove_file(&symlink_path); // Ignore error if it doesn't exist
    std::os::unix::fs::symlink(&bin_filename, &symlink_path).context("Failed to create symlink")?;

    Ok(format!("Synced successfully to {}", bin_path.display()))
}

pub fn capnp_encode_message(schema_path: &str, root_struct: &str, text_payload: &str, packed: bool) -> Result<String> {
    let capnp_txt = aski_lib::projector::edn_to_capnp_txt(text_payload)?;
    let bin = run_capnp_encode(schema_path, root_struct, &capnp_txt, packed)?;
    
    use base64::Engine;
    Ok(base64::engine::general_purpose::STANDARD.encode(&bin))
}

fn run_capnp_encode(schema_path: &str, root_struct: &str, text_content: &str, packed: bool) -> Result<Vec<u8>> {
    let mut cmd = Command::new("capnp");
    cmd.arg("encode");
    if packed {
        cmd.arg("--packed");
    }
    cmd.arg(schema_path).arg(root_struct);

    let mut child = cmd.stdin(std::process::Stdio::piped())
        .stdout(std::process::Stdio::piped())
        .stderr(std::process::Stdio::piped())
        .spawn().context("Failed to spawn capnp command")?;

    if let Some(mut stdin) = child.stdin.take() {
        stdin.write_all(text_content.as_bytes())?;
    }

    let output = child.wait_with_output()?;
    if !output.status.success() {
        anyhow::bail!("capnp encode failed: {}", String::from_utf8_lossy(&output.stderr));
    }

    Ok(output.stdout)
}
