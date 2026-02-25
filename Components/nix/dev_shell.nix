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

    # Ensure stable pi-source symlink for Nix tokenization fix
    # This prevents absolute Nix store paths (hashes) from leaking into the LLM system prompt
    mkdir -p ~/.pi
    export PI_SOURCE_STABLE_LINK="$HOME/.pi/pi-source"
    if [ ! -L "$PI_SOURCE_STABLE_LINK" ] || [ "$(readlink -f "$PI_SOURCE_STABLE_LINK")" != "${pi}/lib/node_modules/pi" ]; then
      ln -sfn "${pi}/lib/node_modules/pi" "$PI_SOURCE_STABLE_LINK"
    fi
    export PI_PACKAGE_DIR="$PI_SOURCE_STABLE_LINK"

    # Load user secrets if mentci-user is available
    if command -v mentci-stt >/dev/null 2>&1; then
      # This is a temporary shim until the full MCP apparatus handles this
      # We extract keys for standard tools that expect env vars
      export LINKUP_API_KEY=$(gopass show Mentci-AI/linkup.so/Goldragon-Key-v1 2>/dev/null | head -n 1)
    fi
  '';
}
