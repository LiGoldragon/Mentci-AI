import pathlib

# Cleanup script to remove non-EDN logs
logs_dir = pathlib.Path("Logs")
if logs_dir.exists():
    for f in logs_dir.glob("*.log"):
        print(f"Removing legacy log: {f}")
        f.unlink()
    print("Cleanup complete.")
else:
    print("Logs directory not found.")