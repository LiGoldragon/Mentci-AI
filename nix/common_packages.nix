{ pkgs, codex_cli_nix, system, scripts_dir }:

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
  pkgs.rsync
  codex_cli_nix.packages.${system}.default
  (pkgs.writeShellScriptBin "mentci-commit" ''
    ${pkgs.babashka}/bin/bb ${scripts_dir}/commit.clj --runtime "$(pwd)/workspace/.mentci/runtime.json" "$@"
  '')
  (pkgs.writeShellScriptBin "mentci-jj" ''
    ${pkgs.babashka}/bin/bb ${scripts_dir}/jj_workflow.clj --runtime "$(pwd)/workspace/.mentci/runtime.json" "$@"
  '')
  (pkgs.writeShellScriptBin "mentci-bootstrap" ''
    ${pkgs.cargo}/bin/cargo run --quiet --bin mentci-ai -- job/jails bootstrap "$@"
  '')
]
