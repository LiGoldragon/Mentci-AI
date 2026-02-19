import os
import subprocess
import sys
import pathlib
import time

# Tool Stack Transparency:
# Stack: Python 3 / Jujutsu (jj)
# Rationale: Automated verification of the Level 5 Shipping Protocol.

def run_command(cmd, cwd=None):
    print(f"Executing: {' '.join(cmd)}")
    result = subprocess.run(cmd, capture_output=True, text=True, cwd=cwd)
    if result.returncode != 0:
        print(f"Error: {result.stderr}")
        return None
    return result.stdout.strip()

def main():
    repo_root = str(pathlib.Path(__file__).parent.parent.resolve())
    workspace_root = os.path.join(repo_root, "workspace")
    
    # Set environment variables as they would be in the Nix shell
    os.environ["MENTCI_REPO_ROOT"] = repo_root
    os.environ["MENTCI_WORKSPACE"] = workspace_root
    os.environ["MENTCI_COMMIT_TARGET"] = "test-bookmark" # Use a test bookmark to avoid mess

    print(f"Testing Mentci-AI Shipping Protocol...")
    print(f"Repo: {repo_root}")
    print(f"Workspace: {workspace_root}")

    # 1. Create a dummy file in the workspace
    test_file = os.path.join(workspace_root, "TEST_MANIFEST.txt")
    with open(test_file, "w") as f:
        f.write(f"Level 5 Manifestation Test at {time.ctime()}\n")
    
    print(f"Created {test_file}")

    # 2. Run mentci-commit
    # We call the script directly since we might not be in the nix shell yet
    commit_script = os.path.join(repo_root, "scripts/jail_commit.py")
    test_message = f"test: manifestation verification {time.time()}"
    
    output = run_command([sys.executable, commit_script, test_message])
    if output is None:
        print("Commit failed.")
        sys.exit(1)
    
    print(output)

    # 3. Verify the commit and bookmark
    # Check if the test-bookmark points to a commit with our message
    verify_cmd = ["jj", "log", "-r", "test-bookmark", "--no-graph", "-T", "description"]
    description = run_command(verify_cmd, cwd=repo_root)
    
    if description and test_message in description:
        print("\n[SUCCESS] Shipping Protocol Verified.")
        print(f"Bookmark 'test-bookmark' updated with message: '{description}'")
        
        # Cleanup: Remove the test bookmark and dummy file
        # We leave the workspace as is since it's part of the new architecture
        run_command(["jj", "bookmark", "delete", "test-bookmark"], cwd=repo_root)
    else:
        print("\n[FAILURE] Bookmark not updated correctly.")
        sys.exit(1)

if __name__ == "__main__":
    main()
