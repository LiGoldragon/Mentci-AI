{ pkgs, codex_cli_nix, system, rust_toolchain, rust_analyzer, gemini_cli, gemini_tui, pi_dev, unified_llm, vtcode, mentci_bootstrap, chronos, execute, mentci_stt, mentci_user, mentci_mcp, jcodemunch_mcp, agentic_jujutsu, litellm_proxy, fava_trails, fava_trails_mcp_server }:

[
  pkgs.babashka
  pkgs.clojure
  pkgs.clojure-lsp
  pkgs.jet
  pkgs.jujutsu
  pkgs.capnproto
  rust_toolchain
  rust_analyzer
  pkgs.git
  pkgs.gdb
  pkgs.strace
  pkgs.valgrind
  pkgs.bubblewrap
  pkgs.rsync

  # AI Assistants & Agents
  codex_cli_nix.packages.${system}.default
  gemini_cli
  gemini_tui
  pi_dev
  unified_llm
  vtcode
  fava_trails
  fava_trails_mcp_server

  # Mentci Internal Utilities
  mentci_bootstrap
  chronos
  execute
  mentci_stt
  mentci_user
  mentci_mcp
  jcodemunch_mcp
  agentic_jujutsu
  litellm_proxy

  # Runtime dependencies
  pkgs.nodejs
  pkgs.sqlite
  pkgs.nixd

  # Wrapper Scripts
  (pkgs.writeShellScriptBin "mentci-commit" ''
    mentci-vcs commit "$@"
  '')
  (pkgs.writeShellScriptBin "mentci-bootstrap" ''
    ${pkgs.cargo}/bin/cargo run --quiet --manifest-path Components/mentci-aid/Cargo.toml --bin mentci-ai -- job/jails bootstrap "$@"
  '')
]
