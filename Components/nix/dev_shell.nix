{ pkgs, common_packages, jail, repo_root, jj_project_config }:

pkgs.mkShell {
  name = "mentci-ai-dev";
  packages = common_packages;
  env = {
    MENTCI_MODE = "ADMIN";
    MENTCI_RO_INDICATOR = "RW (Admin)";
    RUST_SRC_PATH = "${pkgs.rustPlatform.rustLibSrc}";
    MENTCI_REPO_ROOT = "$(pwd)";
    JJ_CONFIG = "$(pwd)/jj-project-config.toml";
    jailConfig = builtins.toJSON jail.jailConfig;
  };
}
