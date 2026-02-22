{ pkgs, codex_cli_nix, system, scripts_dir, gemini_cli, mentci_clj }:

let
  mentci_jj = import ./mentci_jj.nix {
    inherit pkgs scripts_dir;
  };
in
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
  gemini_cli
  mentci_clj
  (pkgs.writeShellScriptBin "mentci-commit" ''
    ${pkgs.babashka}/bin/bb ${scripts_dir}/commit/main.clj --runtime "$(pwd)/workspace/.mentci/runtime.json" "$@"
  '')
  mentci_jj
  (pkgs.writeShellScriptBin "mentci-bootstrap" ''
    ${pkgs.cargo}/bin/cargo run --quiet --manifest-path Components/Cargo.toml --bin mentci-ai -- job/jails bootstrap "$@"
  '')
]
