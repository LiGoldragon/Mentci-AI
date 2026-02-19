import os
import json
import sys
import shutil
import pathlib

# Tool Stack Transparency:
# Stack: Python 3
# Rationale: Core environment initialization logic.

def link_input(name, category, source_path, inputs_root):
    """
    Links a single input into the jail directory structure.
    """
    target_dir = inputs_root / category
    target_path = target_dir / name
    
    target_dir.mkdir(parents=True, exist_ok=True)
    
    if target_path.is_symlink() or target_path.exists():
        if target_path.is_dir() and not target_path.is_symlink():
            shutil.rmtree(target_path)
        else:
            target_path.unlink()
            
    try:
        os.symlink(source_path, target_path)
    except OSError as e:
        print(f"Error linking {name}: {e}", file=sys.stderr)

def main():
    print("Initializing Mentci-AI Level 5 Jail Environment (Python)...")

    # 1. Load Structured Attributes from Nix
    # When __structuredAttrs = true, Nix creates .attrs.json
    attrs_path = pathlib.Path(".attrs.json")
    if not attrs_path.exists():
        # Try checking for JAIL_CONFIG_JSON as fallback or environment variables
        config_env = os.environ.get("jailConfig")
        if config_env:
            config = json.loads(config_env)
        else:
            print("Error: .attrs.json not found and jailConfig not in env.", file=sys.stderr)
            return
    else:
        with open(attrs_path, 'r') as f:
            full_attrs = json.load(f)
            config = full_attrs.get("jailConfig", {})

    # 2. Determine Purity Mode
    is_impure = os.path.isdir("/usr/local") or "NIX_SHELL_PRESERVE_PROMPT" in os.environ
    mode = "IMPURE" if is_impure else "PURE"
    ro_indicator = "RW (Impure)" if is_impure else "RO (Pure)"
    
    os.environ["MENTCI_MODE"] = mode
    os.environ["MENTCI_RO_INDICATOR"] = ro_indicator
    
    print(f"Running in {mode} mode ({ro_indicator})")

    # 3. Jail Launcher Logic
    inputs_path_str = config.get("inputsPath", "inputs")
    inputs_root = pathlib.Path(inputs_path_str).resolve()
    inputs_root.mkdir(parents=True, exist_ok=True)
    
    print("Launching Nix Jail...")
    print("Organizing inputs from data manifest...")

    input_manifest = config.get("inputManifest", {})
    
    for category, items in input_manifest.items():
        for name, path in items.items():
            link_input(name, category, path, inputs_root)

    print("Jail Launcher Complete.")
    print(f"Inputs available in '{inputs_root}':")
    
    # Simple recursive listing
    for p in inputs_root.rglob('*'):
        level = len(p.relative_to(inputs_root).parts)
        indent = ' ' * 4 * (level - 1)
        if p.is_dir():
            print(f"{indent}{p.name}/")
        else:
            print(f"{indent}{p.name}")

    # 4. Display Context
    context_file = pathlib.Path("Level5-Ai-Coding.md")
    if context_file.exists():
        print("\n--- Level 5 Programming Context ---\n")
        print(context_file.read_text())
        print("\n-----------------------------------\n")

    print("Welcome to the Mentci-AI Jail.")

if __name__ == "__main__":
    main()