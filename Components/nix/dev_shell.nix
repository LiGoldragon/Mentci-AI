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

    # Default Antigravity version for the pi agent
    PI_AI_ANTIGRAVITY_VERSION = "1.23.0";
  };

  shellHook = ''
    export MENTCI_REPO_ROOT="$(pwd)"
    export JJ_CONFIG="$(pwd)/.mentci/jj-project-config.toml"

    # Define the canonical path to the prebuilt Cap'n Proto environment spec
    export MENTCI_USER_SETUP_BIN="${repo_root}/Components/mentci-user/data/setup_44887cc6d40e12cd.bin"

    # Initialize user-specific extension secrets (Logic-Data Separation)
    # The shellHook knows NO data. It relies purely on the mentci-user Rust binary
    # reading the Cap'n Proto spec to construct the environment exports.
    if command -v mentci-user >/dev/null 2>&1; then
      eval "$(mentci-user export-env "$MENTCI_USER_SETUP_BIN" 2>/dev/null)"
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
