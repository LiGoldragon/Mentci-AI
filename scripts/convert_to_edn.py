import subprocess
import os

# Tool Stack Transparency:
# Tool: jet (Clojure/GraalVM native binary from nixpkgs)
# Rationale: Direct conversion of JSONL to EDN.

def convert():
    json_path = "Logs/user_LiGoldragon.log"
    edn_path = "Logs/user_LiGoldragon.edn"
    
    if not os.path.exists(json_path):
        print(f"Error: {json_path} not found.")
        return

    with open(json_path, 'r') as f_in:
        # We use 'nix run' to ensure jet is available even if not built yet
        cmd = ["nix", "run", "nixpkgs#jet", "--", "--from", "json", "--to", "edn"]
        result = subprocess.run(cmd, stdin=f_in, capture_output=True, text=True)
        
        if result.returncode == 0:
            with open(edn_path, 'w') as f_out:
                f_out.write(result.stdout)
            print(f"Successfully converted {json_path} to {edn_path}")
        else:
            print(f"Error during conversion: {result.stderr}")

if __name__ == "__main__":
    convert()
