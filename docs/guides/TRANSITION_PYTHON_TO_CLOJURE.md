# Transition Plan: Python to Clojure (Babashka)

## 1. Rationale
Mentci-AI currently uses Python for glue code and orchestration. However, the system's "Semantic Truth" is recorded in **EDN (Extensible Data Notation)**. Clojure is the native environment for EDN, offering superior data manipulation capabilities and alignment with the **Criome/Sema** ecosystem.

To maintain fast startup times for CLI operations (launchers, loggers), we will use **Babashka** (a fast-starting Clojure scripting runtime) for the immediate transition, with the option to compile to GraalVM native images later.

## 2. Phase 1: Infrastructure (Nix)
Transition the `flake.nix` from a Python-centric environment to a Clojure-centric one.

### 2.1. Tooling Integration
- **Runtime:** Add `pkgs.babashka` and `pkgs.clojure` to `commonPackages`.
- **Derivation:** Implement `mkDerivation` using `babashka` to wrap the orchestration logic.

### 2.2. Pure Clojure Shell
Create a `mkShell` that eliminates Python dependencies entirely, providing only the JVM, Babashka, and necessary Clojure CLI tools.

## 3. Phase 2: Logic Migration
Migrate existing scripts one-by-one, ensuring atomic commits for each.

| Python Script | Clojure Equivalent | Responsibility |
| :--- | :--- | :--- |
| `logger.py` | `logger.clj` | Deprecated: logging is no longer required; use `jj log` for audit. |
| `jail_launcher.py` | `launcher.clj` | Ingesting `.attrs.json` and symlinking `inputs/`. |
| `jail_commit.py` | `commit.clj` | Wrapping `jj` for workspace-to-host shipping. |
| `test_deps.py` | `test_deps.clj` | Verifying Clojure/Nix environment integrity. |

## 4. Phase 3: Pure Nix Derivation (`mentci-clj`)
Define a Nix derivation that packages the Clojure logic as a first-class component.

```nix
mentci-clj = pkgs.stdenv.mkDerivation {
  pname = "mentci-clj-orchestrator";
  version = "0.1.0";
  src = ./scripts;
  nativeBuildInputs = [ pkgs.babashka ];
  installPhase = ''
    mkdir -p $out/bin
    cp *.clj $out/bin/
    # Wrap scripts with babashka shebang
  '';
};
```

## 5. Phase 4: Cleanup
- Remove `pkgs.python3` and `python3Packages` from `flake.nix` and `jail.nix`.
- Delete all `.py` files once parity is verified.
- Update `ARCHITECTURAL_GUIDELINES.md` to reflect the **Clojure Mandate**.

## 6. Verification Matrix
- [ ] Remove logger tooling references once no longer in use.
- [ ] `bb launcher.clj` correctly processes structured Nix attributes.
- [ ] `nix build .#mentci-clj` succeeds in a pure environment.
- [ ] `nix develop` provides a python-free shell.

---
*Target Completion: ♈︎ 1.1.1 (5920 Anno Mundi)*
