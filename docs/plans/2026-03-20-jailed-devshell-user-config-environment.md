# Jailed Devshell User-Config Environment Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Make the jailed devshell/runtime path materialize its environment from a fully specified `Components/mentci-user` configuration instead of relying on ad-hoc shell-hook exports and host-local override heuristics.

**Architecture:** Keep `Components/mentci-user` as the runtime authority for environment realization, but upgrade it from a thin “required env vars + local overrides” tool into a structured user-profile interpreter. The profile remains logic-data separated: the component-local config declares what variables, resolvers, paths, and shell additions are needed, while secrets are still fetched at runtime from allowed resolvers (`gopass`, `env`, `literal`) rather than committed into the repo. The jailed entrypoint should use `mentci-user exec ... -- <command>` so the environment is instantiated inside the jail boundary, not by `eval` in the outer shell.

**Tech Stack:** Nix flakes, `nix develop`, jail wrapper (`Components/nix/jail.nix` / `mentci_jail_run.nix`), Rust (`Components/mentci-user`), Cap’n Proto, EDN sidecars, JJ.

---

## Current-state assumptions to preserve

- `Components/mentci-user/data/setup.edn` currently declares `userConfigPath` and `requiredEnvVars` only.
- `Components/mentci-user/src/main.rs` currently supports `export-env` and `exec` modes and resolves secrets from `gopass`, `env`, or `literal`.
- `Components/nix/dev_shell.nix` currently does `eval "$(mentci-user export-env ...)"` in `shellHook`.
- `Components/nix/mentci_jail_run.nix` currently forwards selected environment variables into the jail, but does not construct a jailed environment from a fully specified `mentci-user` profile.

## Target contract

The target state should support this shape:

1. `Components/mentci-user` owns a complete user-environment spec sidecar.
2. The spec can declare:
   - exported environment variables,
   - how each one resolves,
   - optional PATH additions / extra shell variables,
   - optional tool-specific env bundles.
3. `mentci-user exec` can hydrate that profile and launch a command with it.
4. The jailed runtime path uses that command path directly so the environment is constructed **inside** the jail.
5. No raw secrets are committed; only resolution metadata is stored.

---

### Task 1: Define the fully specified user-config contract

**TDD scenario:** New feature — full TDD cycle.

**Files:**
- Modify: `Components/mentci-user/schema/mentci_user.capnp`
- Modify: `Components/mentci-user/data/setup.edn`
- Create: `Components/mentci-user/data/user-config.edn`
- Create: `Components/mentci-user/tests/user_config_contract.rs`

**Step 1: Write the failing contract test**

Create a test that encodes the minimum supported profile contract:

```rust
#[test]
fn parses_full_user_profile_contract() {
    let profile = mentci_user::load_user_profile("Components/mentci-user/data/user-config.edn")
        .expect("profile should parse");

    assert!(profile.env.len() >= 3);
    assert!(profile.tools.iter().any(|t| t.name == "pi"));
    assert!(profile.path_additions.iter().any(|p| !p.is_empty()));
}
```

**Step 2: Run the test to verify it fails**

Run:
```bash
cd Components/mentci-user && cargo test --test user_config_contract parses_full_user_profile_contract -- --exact
```
Expected: FAIL because `load_user_profile` and/or the profile file do not exist yet.

**Step 3: Extend the schema and sidecar design**

Add the minimum new contract to the schema/EDN authority. Keep it YAGNI. A sufficient shape is:

```capnp
struct UserSetupConfig {
  textHash @0 :Text;
  userConfigPath @1 :Text;
  requiredEnvVars @2 :List(EnvVarReq);
  profilePath @3 :Text;
}
```

And a profile sidecar like:

```edn
{:env [{:name "GEMINI_API_KEY" :method "gopass" :path "Mentci-AI/google/Goldragon-Key-v1"}]
 :pathAdditions [".local/bin"]
 :shellVars [{:name "EDITOR" :value "hx"}]
 :tools [{:name "pi" :env [{:name "OPENAI_API_KEY" :from "OPENAI_API_KEY"}]}]}
```

**Step 4: Re-run the contract test**

Run:
```bash
cd Components/mentci-user && cargo test --test user_config_contract parses_full_user_profile_contract -- --exact
```
Expected: PASS.

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent onto the correct non-empty revision,
- capture it with an explicit commit message rather than pre-emptively describing the working-copy node,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.

---

### Task 2: Implement full-profile loading and environment realization in `mentci-user`

**TDD scenario:** New feature — full TDD cycle.

**Files:**
- Modify: `Components/mentci-user/src/lib.rs`
- Modify: `Components/mentci-user/src/main.rs`
- Create: `Components/mentci-user/tests/env_realization.rs`

**Step 1: Write failing runtime tests**

Add tests for merging setup + profile + local overrides:

```rust
#[test]
fn realizes_env_from_profile_and_overrides() {
    let realized = mentci_user::realize_env(&setup, &profile, &local_overrides)
        .expect("env should resolve");

    assert_eq!(realized.get("GEMINI_API_KEY"), Some(&"dummy".to_string()));
    assert_eq!(realized.get("EDITOR"), Some(&"hx".to_string()));
}

#[test]
fn exec_mode_passes_realized_env_to_child() {
    let output = mentci_user::run_with_env_for_test(&setup_path, &["bash", "-lc", "printf %s \"$EDITOR\""])
        .expect("command should run");
    assert_eq!(output, "hx");
}
```

**Step 2: Run the failing tests**

Run:
```bash
cd Components/mentci-user && cargo test --test env_realization -- --nocapture
```
Expected: FAIL because profile-aware realization does not exist.

**Step 3: Implement the minimal runtime support**

Add typed structs and helpers in `src/lib.rs`:

```rust
pub struct UserProfile {
    pub env: Vec<UserSecretOverride>,
    pub path_additions: Vec<String>,
    pub shell_vars: Vec<NamedValue>,
    pub tools: Vec<ToolProfile>,
}

pub fn realize_env(...) -> Result<BTreeMap<String, String>> { ... }
pub fn load_user_profile(path: &str) -> Result<UserProfile> { ... }
```

Update `src/main.rs` so both `export-env` and `exec` use the same realization path, and so `exec` is the preferred jailed execution surface.

**Step 4: Re-run the tests**

Run:
```bash
cd Components/mentci-user && cargo test --test env_realization -- --nocapture
cd Components/mentci-user && cargo test
```
Expected: all pass.

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to perform the bounded finalize/push/verify flow for this task.

---

### Task 3: Make the Nix packaging expose the full user-config authority explicitly

**TDD scenario:** Modifying tested code — verify the build/runtime wiring before and after.

**Files:**
- Modify: `Components/nix/mentci_user.nix`
- Modify: `Components/nix/default.nix`
- Modify: `flake.nix`
- Create: `Components/nix/mentci_user_profile.nix`

**Step 1: Write a failing packaging test/inspection step**

Capture the desired invariant: the built shell/runtime should not depend on `repo_root/Components/mentci-user/...` stringly paths for the authoritative profile.

Run:
```bash
nix eval .#devShells.x86_64-linux.default.inputDerivation --json >/tmp/devshell.json
rg -n "Components/mentci-user/data/setup.bin|user-config.edn" /tmp/devshell.json
```
Expected: current output still points at the repo-root path or does not expose the user profile explicitly.

**Step 2: Add explicit Nix-side derivations for the setup/profile artifacts**

Introduce a small derivation surface such as:

```nix
{
  setupBin = src + "/data/setup.bin";
  userProfile = src + "/data/user-config.edn";
}
```

Export those from `Components/nix/default.nix` so `dev_shell.nix` and jail tooling consume the same authority.

**Step 3: Re-run the packaging inspection**

Run:
```bash
nix eval .#devShells.x86_64-linux.default.inputDerivation --json >/tmp/devshell.json
rg -n "user-config.edn|setup.bin" /tmp/devshell.json
```
Expected: the shell derivation now carries explicit `mentci-user` profile/setup references through the Nix graph.

**Step 4: Finalize via `jj-agent`**

Ask the `jj-agent` agent to perform the bounded finalize/push/verify flow for this task.

---

### Task 4: Replace outer-shell `eval` with jailed `mentci-user exec` wiring

**TDD scenario:** Modifying tested code — use the current shell/jail behavior as the regression and verify the new launch path.

**Files:**
- Modify: `Components/nix/dev_shell.nix`
- Modify: `Components/nix/mentci_jail_run.nix`
- Modify: `Components/nix/jail.nix`
- Create: `Components/mentci-execute/tests/jail_user_env.rs`

**Step 1: Write the failing jail/runtime test**

Create a test or smoke harness that proves a jailed command sees the realized environment:

```rust
#[test]
fn jailed_command_receives_mentci_user_environment() {
    let out = run_jail_command(["bash", "-lc", "printf '%s' \"$GEMINI_API_KEY\""]);
    assert_eq!(out.trim(), "dummy");
}
```

If a Rust integration test is too heavy, add a shell-based check in the Nix verification lane, but prefer a repeatable automated test.

**Step 2: Reproduce the old behavior**

Run:
```bash
nix develop . --command bash -lc 'printf "setup:%s\n" "${MENTCI_USER_SETUP_BIN:-missing}"'
./result/bin/mentci-jail-run bash -lc 'printf "key:%s\n" "${GEMINI_API_KEY:-missing}"' || true
```
Expected: host shell may have exports, but jailed execution is not guaranteed to be realized from the full profile.

**Step 3: Switch the launch contract**

In `dev_shell.nix`, avoid relying on:

```bash
eval "$(mentci-user export-env ...)"
```

Prefer a launch path like:

```bash
export MENTCI_USER_PROFILE_PATH="${userProfile}"
export MENTCI_USER_SETUP_BIN="${setupBin}"
```

And in the jail runner, wrap the child command with:

```bash
exec mentci-user exec "$MENTCI_USER_SETUP_BIN" -- "$@"
```

If profile path is separate, pass/forward it explicitly as `MENTCI_USER_PROFILE_PATH` and have `mentci-user` consume it.

**Step 4: Re-run the jailed smoke tests**

Run:
```bash
nix develop . --command bash -lc 'mentci-user exec "$MENTCI_USER_SETUP_BIN" -- bash -lc "printf %s \"$EDITOR\""'
./result/bin/mentci-jail-run bash -lc 'printf "%s|%s" "${EDITOR:-missing}" "${GEMINI_API_KEY:+present}"'
```
Expected: both commands succeed; env values are realized inside the child command, not just in the outer shell.

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to perform the bounded finalize/push/verify flow for this task.

---

### Task 5: Add end-to-end verification for `nix develop` + jail + user profile

**TDD scenario:** Verification and regression coverage.

**Files:**
- Modify: `Components/nix/pi_with_extensions_check.nix` (if needed)
- Create: `Components/nix/devshell_user_env_check.nix`
- Create: `Research/medium/mentci-user/2026-03-20-jailed-devshell-user-config-environment.md`

**Step 1: Add a dedicated smoke check**

Create a check derivation that verifies:
- `mentci-user` can parse the setup/profile,
- `nix develop` enters successfully,
- a jailed command sees the realized env,
- no raw secret values are embedded in store paths or committed files.

A sufficient smoke command is:

```bash
bash -lc '
  test -n "$MENTCI_USER_SETUP_BIN" &&
  mentci-user exec "$MENTCI_USER_SETUP_BIN" -- bash -lc "test -n \"$EDITOR\"" &&
  mentci-user exec "$MENTCI_USER_SETUP_BIN" -- bash -lc "test -n \"$GEMINI_API_KEY\""
'
```

**Step 2: Run the full verification packet**

Run:
```bash
cd Components/mentci-user && cargo test
cd /home/li/git/Mentci-AI--dev && nix flake check --keep-going
cd /home/li/git/Mentci-AI--dev && nix develop . --command bash -lc 'mentci-user exec "$MENTCI_USER_SETUP_BIN" -- env | rg "^(EDITOR|GEMINI_API_KEY|OPENAI_API_KEY)="'
```
Expected: all checks pass; the final command prints only variable names/lines and should not be copied verbatim into logs if values are sensitive.

**Step 3: Write the research artifact**

Create a short report in:
- `Research/medium/mentci-user/2026-03-20-jailed-devshell-user-config-environment.md`

Record:
- chosen contract,
- why `exec` was preferred over shell `eval`,
- jail boundary implications,
- remaining follow-up items.

**Step 4: Request code review**

Dispatch a reviewer for the combined diff range before claiming completion.

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to perform the bounded finalize/push/verify flow for the final task.

---

## Recommended execution order

1. Task 1 — schema/contract
2. Task 2 — Rust runtime
3. Task 3 — Nix packaging authority
4. Task 4 — jail/devshell wiring
5. Task 5 — end-to-end verification + research + review

## Notes for the implementer

- Keep raw secrets out of the repository and out of Nix store text whenever possible; only resolution metadata belongs in the fully specified config.
- Prefer component-local authority under `Components/mentci-user/data/` over repo-root ad-hoc files.
- Do not broaden the scope into full account/profile management unless the first pass proves env vars + PATH + tool bundles are insufficient.
- If the actual requirement is “mount a richer `$HOME` into the jail” rather than “realize a fully specified environment,” stop and re-scope before implementation; that is a different boundary and security problem.
