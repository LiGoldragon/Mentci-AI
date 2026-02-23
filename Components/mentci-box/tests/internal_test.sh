#!/bin/sh
set -e

echo "--- Mentci-Box Internal Test Suite ---"

# 1. Check User
echo "Checking user..."
if [ "$USER" != "mentci-box" ]; then
    echo "ERROR: USER is '$USER', expected 'mentci-box'"
    exit 1
fi
echo "USER is correct."

# 2. Check Tools
echo "Checking tools..."
rustc --version
jj --version
echo "Tools are present."

# 3. Check Writability
echo "Checking worktree writability..."
touch .mentci-box-test-write
rm .mentci-box-test-write
echo "Worktree is writable."

# 4. Check Environment Cleanliness
echo "Checking /tmp cleanliness..."
# /tmp should contain the home directory we created, and maybe others.
# But it should be isolated from the host's /tmp.
if [ -f "/tmp/host-only-file" ]; then
    echo "ERROR: Found host file in isolated /tmp"
    exit 1
fi
echo "/tmp is isolated."

# 5. Check Remotes
if [ -n "$EXPECTED_REMOTE" ]; then
    echo "Checking git remotes (expecting $EXPECTED_REMOTE)..."
    if ! git remote -v | grep -q "$EXPECTED_REMOTE"; then
        echo "ERROR: Expected remote '$EXPECTED_REMOTE' not found"
        git remote -v
        exit 1
    fi
    echo "Remote is correct."
else
    echo "Checking git remotes..."
    git remote -v
fi

echo "--- Internal Test Suite Passed ---"
