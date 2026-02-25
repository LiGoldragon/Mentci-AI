{ pkgs, codex_cli_nix, system, rust_toolchain, rust_analyzer, gemini_cli, gemini_tui, mentci_vcs, pi_dev, unified_llm, pi_rust, execute }:

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
  codex_cli_nix.packages.${system}.default
  gemini_cli
  gemini_tui
  mentci_vcs
  execute
  pi_dev
  pkgs.nodejs
  unified_llm
  pi_rust
  (pkgs.writeShellScriptBin "mentci-commit" ''
    mentci-vcs commit "$@"
  '')
  (pkgs.writeShellScriptBin "mentci-bootstrap" ''
    ${pkgs.cargo}/bin/cargo run --quiet --manifest-path Components/mentci-aid/Cargo.toml --bin mentci-ai -- job/jails bootstrap "$@"
  '')
]
