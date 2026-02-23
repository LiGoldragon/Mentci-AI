{ pkgs
, craneLib
, codex_cli_nix
, system
, inputs
, attractor_src
, attractor_docs_src
, scripts_dir
, repo_root
}:

let
  mentci_ai = import ./mentci_ai.nix {
    inherit craneLib pkgs;
  };

  execute = pkgs.runCommand "mentci-execute" { } ''
    mkdir -p "$out/bin"
    ln -s "${mentci_ai}/bin/execute" "$out/bin/execute"
  '';

  attractor = import ./attractor.nix {
    inherit pkgs;
    src = attractor_src;
  };

  mentci_clj = import ./mentci_clj.nix {
    inherit pkgs;
    inherit scripts_dir;
  };

  common_packages = import ./common_packages.nix {
    inherit pkgs system;
    inherit codex_cli_nix;
    inherit scripts_dir;
    inherit gemini_cli;
    inherit mentci_clj;
  };

  jail_sources = import ./jail_sources.nix {
    inherit inputs;
    attractor_src = attractor_src;
    attractor_docs_src = attractor_docs_src;
  };

  gemini_cli = pkgs.callPackage ./gemini-cli.nix { };

  dev_shell = { jail }:
    import ./dev_shell.nix {
      inherit pkgs jail;
      inherit common_packages;
      inherit repo_root;
    };

  execute_check = import ./execute_check.nix {
    inherit pkgs mentci_ai;
  };
in
{
  inherit mentci_ai execute execute_check attractor mentci_clj common_packages jail_sources gemini_cli dev_shell;
  mk_shell = import ./mk-shell.nix { inherit pkgs; };
}
