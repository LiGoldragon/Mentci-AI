use std::process::Command;
use std::fs;
use regex::Regex;

fn main() {
    let targets = ["Components/nix/pi.nix", "Components/nix/pi-dev.nix"];
    
    // 1. Break hashes to force failure
    for path in &targets {
        let content = fs::read_to_string(path).expect("Read nix file");
        let re = Regex::new(r#"npmDepsHash = "sha256-[^"]+";"#).unwrap();
        let broken = re.replace(&content, "npmDepsHash = \"sha256-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\";");
        fs::write(path, broken.as_ref()).expect("Write broken hash");
    }

    println!("Triggering nix build to fetch new hashes...");
    
    // 2. Run nix build and capture stderr
    let output = Command::new("nix")
        .args(["build", ".#piDev"])
        .output()
        .expect("Failed to execute nix build");

    let stderr = String::from_utf8_lossy(&output.stderr);
    
    // 3. Extract correct hash
    let re_hash = Regex::new(r"got:\s+(sha256-[a-zA-Z0-9+/=]+)").unwrap();
    if let Some(caps) = re_hash.captures(&stderr) {
        let new_hash = &caps[1];
        println!("Detected new hash: {}", new_hash);
        
        for path in &targets {
            let content = fs::read_to_string(path).expect("Read nix file");
            let re = Regex::new(r#"npmDepsHash = "sha256-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";"#).unwrap();
            let patched = re.replace(&content, &format!("npmDepsHash = \"{}\";", new_hash));
            fs::write(path, patched.as_ref()).expect("Write patched hash");
        }
        println!("Successfully patched all nix files.");
    } else {
        eprintln!("Failed to detect new hash from nix output.");
        eprintln!("Output was: {}", stderr);
    }
}
#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
        assert!(true);
    }
}
