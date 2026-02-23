{ pkgs, codex_cli_nix, system, gemini_cli, gemini_tui, mentci_vcs }:

[
  pkgs.babashka
  pkgs.clojure
  pkgs.clojure-lsp
  pkgs.jet
  pkgs.jujutsu
  pkgs.capnproto
  pkgs.cargo
  pkgs.rustc
  pkgs.rust-analyzer
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
  (pkgs.writeShellScriptBin "mentci-commit" ''
    mentci-vcs commit "$@"
  '')
  (pkgs.writeShellScriptBin "mentci-bootstrap" ''
    ${pkgs.cargo}/bin/cargo run --quiet --manifest-path Components/mentci-aid/Cargo.toml --bin mentci-ai -- job/jails bootstrap "$@"
  '')
]
