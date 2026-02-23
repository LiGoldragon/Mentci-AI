{ pkgs, jail_nix }:

let
  jail = jail_nix.lib.init pkgs;
  unwrapped = pkgs.writeShellScriptBin "mentci-jail-run-unwrapped" ''
    set -euo pipefail
    if [ "$#" -eq 0 ]; then
      echo "Usage: mentci-jail-run <command> [args...]" >&2
      exit 2
    fi
    exec "$@"
  '';
in
jail "mentci-jail-run" "${unwrapped}/bin/mentci-jail-run-unwrapped" (c:
  let
    toolPkgs = [
      pkgs.babashka
      pkgs.cargo
      pkgs.rustc
      pkgs.stdenv.cc
      pkgs.pkg-config
      pkgs.capnproto
      pkgs.jujutsu
      pkgs.git
      pkgs.rsync
      pkgs.tree
      pkgs.clojure
      pkgs.nix-prefetch-git
    ];
  in
  [
    c."mount-cwd"
    c."readonly-runtime-args"
    c.network
    (c."set-hostname" "mentci-jail")
    (c."add-pkg-deps" toolPkgs)
    (c."try-fwd-env" "HOME")
    (c."try-fwd-env" "USER")
    (c."try-fwd-env" "LANG")
    (c."try-fwd-env" "TERM")
    (c."try-fwd-env" "SSH_AUTH_SOCK")
    (c."try-fwd-env" "GPG_TTY")
    (c."try-fwd-env" "MENTCI_MODE")
    (c."try-fwd-env" "MENTCI_RO_INDICATOR")
    (c."try-fwd-env" "MENTCI_REPO_ROOT")
    (c."try-fwd-env" "JJ_CONFIG")
    (c."try-fwd-env" "NIX_ATTRS_JSON_FILE")
    (c."try-fwd-env" "jailConfig")
  ] ++ (map (pkg: c.readonly (toString pkg)) toolPkgs))
