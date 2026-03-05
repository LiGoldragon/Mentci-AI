{ pkgs, common_packages, jail, repo_root, pi }:

pkgs.mkShell {
  name = "mentci-ai-dev";
  packages = common_packages;
  env = {
    MENTCI_MODE = "ADMIN";
    MENTCI_RO_INDICATOR = "RW (Admin)";
    RUST_SRC_PATH = "${pkgs.rustPlatform.rustLibSrc}";
    jailConfig = builtins.toJSON jail.jailConfig;

    # Non-interactive overrides to prevent agents from getting stuck
    CI = "true";
    DEBIAN_FRONTEND = "noninteractive";
    GIT_TERMINAL_PROMPT = "0";
    GH_PROMPT_DISABLED = "1";
    TERM = "dumb";
    PAGER = "cat";

  };

  shellHook = ''
    export MENTCI_REPO_ROOT="$(pwd)"
    export JJ_CONFIG="$(pwd)/.mentci/jj-project-config.toml"

    # Pi extension update check (Version-based)
    mkdir -p .pi
    if command -v pi >/dev/null 2>&1; then
      _current_pi_version="$(pi --version 2>/dev/null || true)"
      _last_pi_version="$(cat .pi/last_pi_version 2>/dev/null || true)"
      if [ -n "$_current_pi_version" ] && [ "$_current_pi_version" != "$_last_pi_version" ]; then
        echo "[pi] Version change detected ($_last_pi_version -> $_current_pi_version). Updating extensions..."
        if pi update >/dev/null 2>&1; then
          printf '%s\n' "$_current_pi_version" > .pi/last_pi_version
        else
          echo "[pi] warning: update skipped because 'pi update' failed" >&2
        fi
      fi
      unset _current_pi_version _last_pi_version
    fi

    # Canonical Cap'n Proto environment spec pointer (hash-agnostic symlink)
    export MENTCI_USER_SETUP_BIN="${repo_root}/Components/mentci-user/data/setup.bin"

    # Initialize user-specific extension secrets (Logic-Data Separation)
    # mentci-user resolves default setup source from MENTCI_USER_SETUP_BIN / canonical paths.
    if command -v mentci-user >/dev/null 2>&1; then
      if _mentci_user_exports="$(mentci-user export-env 2>/dev/null)"; then
        eval "$_mentci_user_exports"
      else
        echo "[mentci-user] warning: export-env failed; secrets were not loaded into this shell" >&2
      fi
      unset _mentci_user_exports
    fi

    # Ensure stable pi-source symlink for Nix tokenization fix
    # This prevents absolute Nix store paths (hashes) from leaking into the LLM system prompt
    mkdir -p ~/.pi
    export PI_SOURCE_STABLE_LINK="$HOME/.pi/pi-source"
    if [ ! -L "$PI_SOURCE_STABLE_LINK" ] || [ "$(readlink -f "$PI_SOURCE_STABLE_LINK")" != "${pi}/lib/node_modules/pi" ]; then
      ln -sfn "${pi}/lib/node_modules/pi" "$PI_SOURCE_STABLE_LINK"
    fi
    export PI_PACKAGE_DIR="$PI_SOURCE_STABLE_LINK"
  '';
}
