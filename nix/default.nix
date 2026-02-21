{ pkgs
, craneLib
, codex_cli_nix
, system
, inputs
, attractor_src
, attractor_docs_src
, src
, scripts_dir
, repo_root
, jj_project_config
}:

let
  mentci_ai = import ./mentci_ai.nix {
    inherit craneLib pkgs src;
  };

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
  };

  jail_inputs = import ./jail_inputs.nix {
    inherit inputs;
    attractor_src = attractor_src;
    attractor_docs_src = attractor_docs_src;
  };

  gemini_cli = pkgs.callPackage ./gemini-cli.nix { };

  dev_shell = { jail }:
    import ./dev_shell.nix {
      inherit pkgs jail;
      inherit common_packages;
      inherit repo_root jj_project_config;
    };
in
{
  inherit mentci_ai attractor mentci_clj common_packages jail_inputs gemini_cli dev_shell;
  mk_shell = import ./mk-shell.nix { inherit pkgs; };
}
