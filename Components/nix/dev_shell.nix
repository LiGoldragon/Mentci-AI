{ pkgs, common_packages, jail, repo_root, pi, mentci_user_src }:

let
  prometheusAgentConfigPath = repo_root + "/config/pi/prometheus-agent-settings.json";
  prometheusAgentConfig = builtins.fromJSON (builtins.readFile prometheusAgentConfigPath);
  prometheusAgentModelsJson = pkgs.writeText "prometheus-pi-models.json" (builtins.toJSON prometheusAgentConfig.models);
  prometheusAgentSettingsJson = pkgs.writeText "prometheus-pi-settings.json" (builtins.toJSON prometheusAgentConfig.settings);
in
pkgs.mkShell {
  name = "mentci-ai-dev";
  packages = common_packages;
  env = {
    MENTCI_MODE = "ADMIN";
    MENTCI_RO_INDICATOR = "RW (Admin)";
    RUST_SRC_PATH = "${pkgs.rustPlatform.rustLibSrc}";
    jailConfig = builtins.toJSON jail.jailConfig;
    PROMETHEUS_AGENT_MODELS_JSON = prometheusAgentModelsJson;
    PROMETHEUS_AGENT_SETTINGS_JSON = prometheusAgentSettingsJson;

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

    # Short-term dynamic target bookmark contract (session-local, not VC-backed)
    # Priority: explicit env > directory-name inference > safe default.
    if [ -z "''${MENTCI_TARGET_BOOKMARK:-}" ]; then
      _repo_base="$(basename "$(pwd)" | tr '[:upper:]' '[:lower:]')"
      if [[ "$_repo_base" == *"--"* ]]; then
        export MENTCI_TARGET_BOOKMARK="''${_repo_base##*--}"
      elif [ "$_repo_base" = "mentci-ai" ]; then
        export MENTCI_TARGET_BOOKMARK="main"
      else
        export MENTCI_TARGET_BOOKMARK="dev"
      fi
      unset _repo_base
    fi

    # Keep pi mutable state inside the repo
    export PI_CODING_AGENT_DIR="$(pwd)/.pi/agent"
    mkdir -p "$PI_CODING_AGENT_DIR"/{prompts,skills,themes,tools,sessions,agents,commands,extensions}

    ln -sfn "$PROMETHEUS_AGENT_MODELS_JSON" "$PI_CODING_AGENT_DIR/models.json"
    ln -sfn "$PROMETHEUS_AGENT_SETTINGS_JSON" "$PI_CODING_AGENT_DIR/settings.json"

    # Share OAuth auth across repos so login is not required per-repo
    # (sessions/tools stay repo-local; auth remains user-global)
    export PI_SHARED_AUTH_FILE="$HOME/.pi/agent/auth.json"
    mkdir -p "$(dirname "$PI_SHARED_AUTH_FILE")"
    if [ -e "$PI_CODING_AGENT_DIR/auth.json" ] && [ ! -L "$PI_CODING_AGENT_DIR/auth.json" ]; then
      mv "$PI_CODING_AGENT_DIR/auth.json" "$PI_CODING_AGENT_DIR/auth.json.bak.$(date +%s)"
    fi
    ln -sfn "$PI_SHARED_AUTH_FILE" "$PI_CODING_AGENT_DIR/auth.json"

    # Canonical mentci-user setup pointer from the checked-out component tree.
    # This keeps the dev shell repo-local and avoids relying on fetcher-specific
    # path coercion for flake=false source inputs.
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
    # Keep the link project-local (avoid mutating ~/.pi)
    export PI_SOURCE_STABLE_LINK="$(pwd)/.pi/pi-source"
    if [ ! -L "$PI_SOURCE_STABLE_LINK" ] || [ "$(readlink -f "$PI_SOURCE_STABLE_LINK")" != "${pi}/lib/node_modules/pi" ]; then
      ln -sfn "${pi}/lib/node_modules/pi" "$PI_SOURCE_STABLE_LINK"
    fi
    export PI_PACKAGE_DIR="$PI_SOURCE_STABLE_LINK"
  '';
}
