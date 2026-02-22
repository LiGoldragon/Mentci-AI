# Release Protocol: Zodiac-Ordinal Versioning

**Status:** Draft / Specification
**Context:** Major version commit messages and tags

## 1. Purpose
Major releases use the Zodiac-Ordinal notation to encode time and intent. Commit messages for major releases must summarize the broad change set since the last tag.

Year-version policy:
- Use `v0.<sign>.<degree>.<minute>.<second>` until the vernal equinox.
- After the equinox, increment the year-version to `v1.*`.

## 2. Format
### 2.1 Commit Message
Use a markdown-formatted commit message with a single-line title and a bullet overview:

```
release: v<year-version>.<sign>.<degree>.<minute>.<second> - <short summary>
```

- `<short summary>` is a high-level phrase, not a feature list.
- Example:
  `release: v0.12.1.28.44 - stabilize level-5 workflow standards`

### 2.2 Annotated Tag
Create an annotated tag with the same version:

```
git tag -a v<cycle>.<sign>.<degree>.<minute>.<second> -m "release: v<cycle>.<sign>.<degree>.<minute>.<second>"
```

## 3. Required Overview Block
Add a short overview of major changes since the last tag in the commit **description** (body), using 3–6 bullets. Include the unicode zodiac date line above the bullets.

```
release: v0.12.1.28.44 - stabilize level-5 workflow standards
♓︎ 1° 28' 44" | 5919 AM

- Standardized JJ workflows and audit trail policy
- Introduced Aski-Astral and Aski-FS specs
- Established DSL guidelines for delimiter-driven typing
```

## 3.1 Required Companion Change
Every tagged release commit must include an update to:

- `docs/guides/RESTART_CONTEXT.md`

The `RESTART_CONTEXT.md` update is part of the release changeset and must be committed in the same release commit that is tagged.

## 3.2 Admin-Mode Main Push (Pending Security Protocol)
When operating in Admin Developer Mode, a tagged release must be pushed to `main` after the release commit and tag are created.

Required sequence:
1. Create the release commit.
2. Create the annotated release tag.
3. Push `main` and the release tag together.

Security note:
- The security protocol for authorizing `main` pushes in Admin Developer Mode is not finalized yet.
- Until finalized, treat this as a policy requirement with implementation details pending.

## 4. Source of Truth
Use `jj log` and `jj diff` to determine the change list since the last tag.

## 5. Related References
- Chronography: `docs/architecture/CHRONOGRAPHY.md`
- Version Control: `docs/architecture/VERSION_CONTROL.md`
