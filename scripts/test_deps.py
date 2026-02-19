import shutil
import subprocess
import sys

# List of critical dependencies for Mentci-AI
DEPS = [
    "nix", "cargo", "rustc", "python3", "capnp", "git", "jj", "jet", "gdb"
]

def check_dep(name):
    path = shutil.which(name)
    if path:
        print(f"[OK] {name.ljust(10)} -> {path}")
        return True
    else:
        print(f"[!!] {name.ljust(10)} NOT FOUND")
        return False

def main():
    print("Mentci-AI Dependency Audit")
    print("-" * 30)
    all_found = True
    for dep in DEPS:
        if not check_dep(dep):
            all_found = False
    
    print("-" * 30)
    if all_found:
        print("All administrative dependencies found in environment.")
    else:
        print("Warning: Missing dependencies detected for the current shell.")
        sys.exit(1)

if __name__ == "__main__":
    main()
