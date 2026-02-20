# Release Protocol: Zodiac-Ordinal Versioning

**Status:** Draft / Specification
**Context:** Major version commit messages and tags

## 1. Purpose
Major releases use the Zodiac-Ordinal notation to encode time and intent. Commit messages for major releases must summarize the broad change set since the last tag.

## 2. Format
### 2.1 Commit Message
Use a single-line commit message:

```
release: v<cycle>.<sign>.<degree>.<minute>.<second> - <short summary>
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
Add a short overview of major changes since the last tag in the commit **description** (body), using 3–6 bullets:

```
release: v0.12.1.28.44 - stabilize level-5 workflow standards

- Standardized JJ workflows and audit trail policy
- Introduced Aski-Astral and Aski-FS specs
- Established DSL guidelines for delimiter-driven typing
```

## 4. Source of Truth
Use `jj log` and `jj diff` to determine the change list since the last tag.

## 5. Related References
- Chronography: `docs/architecture/CHRONOGRAPHY.md`
- Version Control: `docs/architecture/VERSION_CONTROL.md`
