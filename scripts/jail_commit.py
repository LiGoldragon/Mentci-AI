import os
import subprocess
import sys
import pathlib

# Tool Stack Transparency:
# Stack: Python 3 / Jujutsu (jj)
# Rationale: Direct interaction with jj workspace to commit changes back to the main repo.

def main():
    target_bookmark = os.environ.get("MENTCI_COMMIT_TARGET", "dev")
    repo_root = os.environ.get("MENTCI_REPO_ROOT")
    workspace_root = os.environ.get("MENTCI_WORKSPACE")
    
    if not repo_root or not workspace_root:
        print("Error: MENTCI_REPO_ROOT or MENTCI_WORKSPACE not set.")
        sys.exit(1)

    if len(sys.argv) < 2:
        print("Usage: mentci-commit <message>")
        sys.exit(1)

    message = sys.argv[1]
    
    try:
        # 1. Update the description of the change in the workspace
        subprocess.run(["jj", "describe", "-m", message, "-R", workspace_root], check=True)
        
        # 2. Advance the target bookmark in the main repo to the current workspace change
        # jj works across workspaces natively if they share the same repo.
        subprocess.run(["jj", "bookmark", "set", target_bookmark, "-r", "@", "-R", workspace_root], check=True)
        
        print(f"Successfully committed and advanced bookmark '{target_bookmark}' from workspace.")
    except subprocess.CalledProcessError as e:
        print(f"Error during jj operation: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()

