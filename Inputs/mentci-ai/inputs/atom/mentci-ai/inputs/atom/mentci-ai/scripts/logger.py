import os
import datetime
import json
import getpass
import sys
import argparse

# Simple logging utility for Mentci-AI
# Follows the MachineLog schema structure.

LOGS_DIR = "Logs"

# -- Configuration --
# For now, we default to the GitHub username "LiGoldragon" as the primary identity
# if the system user matches the developer environment.
DEFAULT_GITHUB_USER = "LiGoldragon"

def get_current_user_id():
    """
    Determines the user ID to log as.
    Prioritizes explicit overrides, then falls back to system user.
    """
    system_user = getpass.getuser()
    # In this specific dev environment, we map 'li' to 'LiGoldragon'
    if system_user == "li":
        return DEFAULT_GITHUB_USER
    return system_user

def log_prompt(intent_summary, model="unknown-model", user_id=None):
    """
    Logs a prompt entry to the user's MachineLog file.
    """
    if not os.path.exists(LOGS_DIR):
        os.makedirs(LOGS_DIR)

    if user_id is None:
        user_id = get_current_user_id()

    log_file = os.path.join(LOGS_DIR, f"user_{user_id}.log")
    
    timestamp = datetime.datetime.now().isoformat()
    
    entry = {
        "timestamp": timestamp,
        "userId": user_id,
        "intentSummary": intent_summary,
        "model": model,
        "signature": None # Placeholder for future crypto
    }

    # Append generic JSONL line
    with open(log_file, "a") as f:
        f.write(json.dumps(entry) + "\n")

    print(f"Logged intent: '{intent_summary}' to {log_file}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Log high-level intent to MachineLog.")
    parser.add_argument("intent", help="Short summary of the high-level intent")
    parser.add_argument("--model", default="unknown-model", help="The AI model used (if any)")
    parser.add_argument("--user", default=None, help="Override user ID")

    args = parser.parse_args()

    log_prompt(args.intent, args.model, args.user)
