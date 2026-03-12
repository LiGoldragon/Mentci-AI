# Prometheus Llama.cpp Cutover Implementation Plan

I'm using the writing-plans skill to create the implementation plan.

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Remove every Ollama reference from the Prometheus CriomOS stack, document the mistake, and rely exclusively on llama.cpp for the OS and home/dev layers.

**Architecture:** Rework the metal and home modules so they only build with the local llama.cpp derivation, keep the configuration declarative (no Ollama overlay, no fallback tunnel), document the historical misstep, and re-deploy the updated configurations.

**Tech Stack:** CriomOS Nix stacks (`nix/mkCriomOS/metal/default.nix`, `nix/homeModule/min/default.nix`), llama.cpp local build, NixOS/home-manager switches, Markdown documentation in `docs/research`.

---

### Task 1: Remove Ollama from the CriomOS metal stack

**TDD scenario:** Modifying tested configuration — run the existing metal evaluation before and after the change to see the Ollama references disappear.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/metal/default.nix` (remove `ollamaPackage`, drop `services.ollama`, keep llama.cpp path).
- Test: `nix build -f /home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/metal/default.nix metalSystem`

**Step 1: Run the existing build**
Run: `nix build -f /home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/metal/default.nix metalSystem`
Expected: PASS and the build log currently references the `ollama` derivation so you can pin the pre-change state.

**Step 2: Remove Ollama wiring**
- Delete the `ollamaPackage` attribute and any `ollama` service or job definitions.
- Ensure `environment.systemPackages`, `programs` and any `home` wrappers only refer to `llama-cpp` derived from the local llama.cpp checkout.
- Confirm `llama-cpp` is defined as a `callPackage` pointing at the repo’s `llama.cpp` directory and no other fallback paths remain.

**Step 3: Re-run the nix build**
Run: same `nix build` command as Step 1.
Expected: PASS without any `ollama` references; `llama-cpp` should now build as the only local dependency.

**Step 4: Validate post-change attributes**
Run: `nix eval -f /home/li/git/Mentci-AI--dev/Components/CriomOS/nix/mkCriomOS/metal/default.nix metalSystem.services` and inspect for absence of `ollama` keys and presence of `llama-cpp`/`llama-wrapper`.
Expected: `services.ollama` is gone and the `llama-cpp` derivation resolves to the intended local path.

**Step 5: Finalize via `jj-agent`**
Ask `jj-agent` to:
- establish bounded JJ state relative to `$MENTCI_TARGET_BOOKMARK`,
- finalize the current intent into a described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if `jj-agent` misbehaves.

---

### Task 2: Remove the Ollama fallback/tunnel from the home module

**TDD scenario:** Modifying tested configuration — verify the home-manager build both before and after removing the fallback.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/nix/homeModule/min/default.nix` (remove the Ollama tunnel/fallback and ensure only llama.cpp local path remains).
- Test: `home-manager switch --flake /home/li/git/Mentci-AI--dev#min`

**Step 1: Build the existing home module**
Run: `home-manager switch --flake /home/li/git/Mentci-AI--dev#min`
Expected: PASS showing the current fallback configuration and `ollama` reference (helps confirm pre-change behavior).

**Step 2: Strip the fallback/tunnel**
- Remove the `home.file` entries, `home.sessionVariables`, or `home.packages` pieces that attempt to discover or fall back to an Ollama socket.
- Ensure `home.file` links and scripts reference `llama.cpp` binaries only and the environment variables point to the new `llama-cpp` output.
- Document the only local path used for `llama.cpp` so future readers know this is the canonical binary.

**Step 3: Re-run home-manager**
Run: same `home-manager switch` command.
Expected: PASS and the switch log mentions only the new `llama-cpp` targets, no `ollama` packages.

**Step 4: Introspect session variables**
Run: `home-manager -f /home/li/git/Mentci-AI--dev switch --show-trace --flake /home/li/git/Mentci-AI--dev#min` (or `nix build` equivalent) and verify the generated `profile/bin/llama` link points to the local llama.cpp store path.
Expected: The symlink resolves to `.../llama.cpp` and there are no `OLLAMA_HOME` or similar variables.

**Step 5: Finalize via `jj-agent`** (same instructions as Task 1).

---

### Task 3: Document the historical Ollama mistake in docs/research

**TDD scenario:** Trivial change — use judgement, but keep documentation consistent with markdown linting.

**Files:**
- Create/Modify: `/home/li/git/Mentci-AI--dev/docs/research/prometheus-llama-history.md` (add a section explaining that Ollama was an earlier, incorrect choice replaced by llama.cpp).
- Test: `mdformat --check /home/li/git/Mentci-AI--dev/docs/research/prometheus-llama-history.md`

**Step 1: Draft the narrative**
Write the history paragraph/slides that explain:
- why Ollama was originally chosen,
- why it is now considered erroneous (performance, license, maintainability),
- the rationale for switching to llama.cpp and how to set it up going forward.

**Step 2: Run markdown lint/formatter**
Run: `mdformat --check /home/li/git/Mentci-AI--dev/docs/research/prometheus-llama-history.md`
Expected: PASS. If the command is missing, note that installing `mdformat` is a blocker and mention it explicitly.

**Step 3: Re-read the doc for accuracy**
Manually review and ensure this doc references the precise commit/branch names and points readers to `llama.cpp` activation instructions in the CriomOS config.

**Step 4: Save/update the doc in git**
No command beyond editing; ensure the doc is staged with the plan’s subsequent `jj-agent` finalization.

**Step 5: Finalize via `jj-agent`.**

---

### Task 4: Activate the rebuilt OS configuration on Prometheus

**TDD scenario:** Modifying deployment state — verify the new OS config redeploys cleanly.

**Files:**
- Modify: `/home/li/git/Mentci-AI--dev/flake.nix` or the deployment script if needed to point Prometheus at the updated metal configuration (if not already wired by Task 1).
- Test: `sudo nixos-rebuild switch --flake /home/li/git/Mentci-AI--dev#prometheus-metal`

**Step 1: Dry-run the rebuild**
Run: `sudo nixos-rebuild dry-run --flake /home/li/git/Mentci-AI--dev#prometheus-metal`
Expected: PASS with evaluation reflecting only `llama-cpp`, no `ollama` units.

**Step 2: Apply the rebuild**
Run: `sudo nixos-rebuild switch --flake /home/li/git/Mentci-AI--dev#prometheus-metal`
Expected: System applies the new OS configuration and reports no failed units.

**Step 3: Check the derived symlinks**
Run: `readlink /run/current-system/sw/bin/llama-cpp` and confirm it points under `/nix/store/...-llama.cpp`.

**Step 4: Finalize via `jj-agent`.**

---

### Task 5: Activate the new home/dev environment and verify services/endpoints

**TDD scenario:** Modifying running services — validate via service introspection and endpoint probing.

**Files:**
- Modify: any runtime activation scripts or `.env` overlays used by home/dev machines (if they exist) so the upgrade to llama.cpp is mirrored; reference `/home/li/git/Mentci-AI--dev/nix/homeModule/min/default.nix` results.
- Test: `systemctl status prometheus.service && curl -f http://localhost:9090/-/ready`

**Step 1: Switch the home/dev environment**
Run: `home-manager switch --flake /home/li/git/Mentci-AI--dev#dev` (replace `#dev` with the actual home configuration, e.g., `#prometheus-dev`).
Expected: PASS and the home profile now contains the new llama.cpp binary.

**Step 2: Check service health**
Run: `systemctl status prometheus.service` and `systemctl status criomOS-llama.service` (or equivalents).
Expected: Both services show `active (running)` and reference `llama-cpp` in their unit files.

**Step 3: Probe endpoints**
Run: `curl -f http://127.0.0.1:9090/-/ready` and `curl -f http://127.0.0.1:9090/api/v1/query?query=up`.
Expected: Both `curl` commands return `200 OK` with expected payloads. If HTTP access is blocked (e.g., due to restricted environment), note that as a validation blocker and capture the error message.

**Step 4: Log the new service configuration**
Run: `systemctl status prometheus.service -l | head -n 20` and save the snippet as evidence that the unit now references `llama-cpp` as its executable.

**Step 5: Finalize via `jj-agent`.**

---

#### Blockers
- If `mdformat` is not available in the environment, note that Markdown validation must be deferred until the formatter can be installed; include the failure output in the plan’s validation logs.
- If the Prometheus machine lacks HTTP connectivity from the shell, capturing the `curl` failure (e.g., `curl: (7) Failed to connect`) is mandatory; mention it in the verification report.
