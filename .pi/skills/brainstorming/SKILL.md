---
name: brainstorming
description: "You MUST use this before any creative work - creating features, building components, adding functionality, or modifying behavior. Explores user intent, requirements and design before implementation."
---

> **Related skills:** Consider `/skill:using-git-worktrees` to set up an isolated workspace, then `/skill:writing-plans` for implementation planning.
>
> **JJ skills:**
> - Basic: @.pi/skills/jj-basic/SKILL.md
> - Intermediate: @.pi/skills/jj-intermediate/SKILL.md
> - Expert: @.pi/skills/jj-expert/SKILL.md

## Repo-Local Nix Purity Rule
- Treat every repository as a self-contained world during Nix evaluation.
- Never reference files from a parent repo, sibling checkout, ad-hoc absolute path, or undeclared local path escape inside Nix code.
- If reusable Nix code is needed, it must live inside the active repository or arrive through a declared flake input; if we create that code, it belongs in a repository and our repository workflow remains Git-backed JJ.
- Deep modules must not `../`-escape repo boundaries to find package code. Root-wire shared derivations from the active repo root and pass them down through module arguments / `specialArgs`.


# Brainstorming Ideas Into Designs

## Overview

Help turn ideas into fully formed designs and specs through natural collaborative dialogue.

Start by understanding the current project context, then ask questions one at a time to refine the idea. Once you understand what you're building, present the design in small sections (200-300 words), checking after each section whether it looks right so far.

## Boundaries
- Read code and docs: yes
- Write to docs/plans/: yes
- Edit or create any other files: no

## The Process

**Before anything else — check repository state via `jj-agent`:**
- Delegate a bounded current-state report to the `jj-agent` agent. Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.
- Ask it to establish the runtime bookmark, working-copy cleanliness, and nearby lineage before brainstorming.
- If the repo has uncommitted or unmerged work, ask the user whether to finish prior work, isolate it, or explicitly continue here.
- If the topic is new, suggest creating an isolated JJ clone or other user-approved workspace strategy before brainstorming.

**Understanding the idea:**
- Check out the current project state first (files, docs, recent commits)
- Check if the codebase or ecosystem already solves this before designing from scratch
- Ask questions one at a time to refine the idea
- Prefer multiple choice questions when possible, but open-ended is fine too
- Only one question per message - if a topic needs more exploration, break it into multiple questions
- Focus on understanding: purpose, constraints, success criteria

**Exploring approaches:**
- Propose 2-3 different approaches with trade-offs
- Present options conversationally with your recommendation and reasoning
- Lead with your recommended option and explain why

**Presenting the design:**
- Once you believe you understand what you're building, present the design
- Break it into sections of 200-300 words
- Ask after each section whether it looks right so far
- Cover: architecture, components, data flow, error handling, testing
- Be ready to go back and clarify if something doesn't make sense

## After the Design

**Documentation:**
- Write the validated design to `docs/plans/YYYY-MM-DD-<topic>-design.md`
- If the design artifact should be committed now, ask `jj-agent` to finalize and push that planning intent. Fall back to `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.
- Mark the brainstorm phase complete: call `plan_tracker` with `{action: "update", status: "complete"}` for the current phase

**Implementation (if continuing):**
- Ask: "Ready to set up for implementation?"
- Set up isolated workspace — `/skill:using-git-worktrees` for larger work, or another user-approved JJ/bookmark-isolation strategy for smaller changes
- Use `/skill:writing-plans` to create detailed implementation plan

## Key Principles

- **One question at a time** - Don't overwhelm with multiple questions
- **Multiple choice preferred** - Easier to answer than open-ended when possible
- **YAGNI ruthlessly** - Remove unnecessary features from all designs
- **Design for testability** - Favor approaches with clear boundaries that are easy to verify with TDD
- **Explore alternatives** - Always propose 2-3 approaches before settling
- **Incremental validation** - Present design in sections, validate each
- **Be flexible** - Go back and clarify when something doesn't make sense
