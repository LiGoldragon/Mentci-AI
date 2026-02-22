import os
import datetime
import json
import getpass

# Simple logging utility for Mentci-AI
# Follows the MachineLog schema structure.

LOGS_DIR = "logs"

def log_prompt(intent_summary, model="unknown-model", user_id=None):
    """
    Logs a prompt entry to the user's MachineLog file.
    """
    if not os.path.exists(LOGS_DIR):
        os.makedirs(LOGS_DIR)

    if user_id is None:
        user_id = getpass.getuser()

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
    # Test execution
    log_prompt("Initial test log entry", "gemini-cli-test")
