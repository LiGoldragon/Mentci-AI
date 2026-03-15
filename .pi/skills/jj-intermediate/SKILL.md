# JJ Intermediate Skills

## Target Bookmark Pattern

**CRITICAL:** Always use `$MENTCI_TARGET_BOOKMARK` for your operations unless explicitly instructed otherwise.

```bash
# The variable is set in the Nix shell environment
echo $MENTCI_TARGET_BOOKMARK  # Typically "dev" or "main"

# Use it in all jj operations:
jj new "$MENTCI_TARGET_BOOKMARK"
jj bookmark move "$MENTCI_TARGET_BOOKMARK" --to '@-'
jj git push --remote origin --bookmark "$MENTCI_TARGET_BOOKMARK"
```

**When the variable is not set:**
- Default to `dev` for development work
- Default to `main` for release/production work

**Example:**
```bash
# Safe, portable command
TARGET="${MENTCI_TARGET_BOOKMARK:-dev}"
jj new "$TARGET"
```

## Empty Commit Workflow

**CRITICAL:** JJ creates an empty working-copy commit after every `jj commit -m "msg"`. This empty commit is NOT the actual work - it's just a marker.

### The Workflow

1. **Create the actual commit** (this has the changes):
   ```bash
   jj commit -m "Your meaningful commit message"
   ```
   - This creates a non-empty commit with your changes
   - The working copy becomes an empty commit (`@`)
   - Your actual work is at `@-` (the parent)

2. **Move bookmark to the actual commit** (not the empty one):
   ```bash
   # Method 1: Using bookmark move with --to
   jj bookmark move "$MENTCI_TARGET_BOOKMARK" --to '@-'
   
   # Method 2: Using bookmark set
   jj bookmark set "$MENTCI_TARGET_BOOKMARK" '@-'
   ```
   - This moves the bookmark to point to the parent commit (the one with changes)
   - The empty working-copy commit is skipped

3. **Push to remote**:
   ```bash
   jj git push --remote origin --bookmark "$MENTCI_TARGET_BOOKMARK"
   ```

### Why This Matters

- **Empty commits should NOT be pushed** - they're just JJ's internal marker
- **The bookmark MUST point to `@-`** - the parent commit with actual changes
- **Pushing the empty commit** creates a useless commit on the remote

### Quick Reference

```bash
# 1. Commit your changes (creates non-empty commit, working copy becomes empty)
jj commit -m "Add new feature"

# 2. Verify the commit
jj log --limit 3

# 3. Move bookmark to the actual commit (skip the empty working copy)
jj bookmark move "$MENTCI_TARGET_BOOKMARK" --to '@-'

# 4. Push to remote
jj git push --remote origin --bookmark "$MENTCI_TARGET_BOOKMARK"
```

### Common Mistakes

**WRONG:**
```bash
jj commit -m "message"
jj git push  # Pushes empty commit!
```

**RIGHT:**
```bash
jj commit -m "message"
jj bookmark move "$MENTCI_TARGET_BOOKMARK" --to '@-'  # Move to actual commit
jj git push --remote origin     # Push the real work
```

### Important Notes

- **Revset syntax:** Use `'@-'` (quoted) when passing as argument to `jj bookmark set`
- **Bookmark move:** Use `--to <revset>` syntax for moving bookmarks
- **Push explicit:** Use `--bookmark <name>` to push specific bookmarks
- **Forward movement:** JJ refuses to move bookmarks backwards/sideways by default; use `--allow-backwards` if needed

### Alternative: Commit and Push in One Step

If you want to avoid the empty commit entirely:
```bash
jj describe -m "Your message"  # Set message without creating commit
jj commit  # Create commit without creating new working copy
jj bookmark move "$MENTCI_TARGET_BOOKMARK" --to '@-'
jj git push --remote origin
```

---

**Status:** JJ intermediate skills documented with target bookmark pattern
