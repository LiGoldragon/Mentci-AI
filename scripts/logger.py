import os
import datetime
import getpass
import sys
import argparse
import pathlib

# Tool Stack Transparency:
# Library: edn_format (Python)
# Origin: github:swaroopch/edn_format (vendored in tools/)
# Rationale: Provides EDN support for Python scripts, adhering to the Python mandate.

# Calculate absolute path to 'tools' directory
# Project root is parent of 'scripts'
script_path = pathlib.Path(__file__).resolve()
project_root = script_path.parent.parent
tools_path = project_root / "tools"

if str(tools_path) not in sys.path:
    sys.path.insert(0, str(tools_path))

try:
    import edn_format
    from edn_format import Keyword
except ImportError as e:
    # Diagnostic info
    print(f"Error: edn_format not found in {tools_path}.", file=sys.stderr)
    print(f"DEBUG: sys.path: {sys.path}", file=sys.stderr)
    print(f"DEBUG: tools content: {list(tools_path.glob('*')) if tools_path.exists() else 'MISSING'}", file=sys.stderr)
    sys.exit(1)

# Simple logging utility for Mentci-AI
# Follows the MachineLog schema structure.

LOGS_DIR = project_root / "Logs"

# -- Configuration --
DEFAULT_GITHUB_USER = "LiGoldragon"

def get_current_user_id():
    system_user = getpass.getuser()
    if system_user == "li":
        return DEFAULT_GITHUB_USER
    return system_user

def log_prompt(intent_summary, model="unknown-model", user_id=None):
    """
    Logs a prompt entry to the user's MachineLog file in EDN format.
    """
    if not LOGS_DIR.exists():
        LOGS_DIR.mkdir(parents=True, exist_ok=True)

    if user_id is None:
        user_id = get_current_user_id()

    log_file = LOGS_DIR / f"user_{user_id}.edn"
    
    timestamp = datetime.datetime.now().isoformat()
    
    # Construct EDN map structure using Keywords for keys
    entry = {
        Keyword("timestamp"): timestamp,
        Keyword("userId"): user_id,
        Keyword("intentSummary"): intent_summary,
        Keyword("model"): model,
        Keyword("signature"): None 
    }

    # Append EDN entry
    with open(log_file, "a") as f:
        f.write(edn_format.dumps(entry) + "\n")

    print(f"Logged intent: '{intent_summary}' to {log_file}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Log high-level intent to MachineLog.")
    parser.add_argument("intent", help="Short summary of the high-level intent")
    parser.add_argument("--model", default="deepseek-v4", help="The AI model used (Defaulting to deepseek-v4 per project switch)")
    parser.add_argument("--user", default=None, help="Override user ID")

    args = parser.parse_args()

    log_prompt(args.intent, args.model, args.user)