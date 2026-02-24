{ pkgs
, craneLib
, rust_toolchain
, rust_analyzer
, codex_cli_nix
, system
, inputs
, attractor_src
, attractor_docs_src
, pi_mono_src
, pi_agent_rust_src
, repo_root
}:

let
  mentci_ai = import ./mentci_ai.nix {
    inherit craneLib pkgs;
  };

  mentci_box = import ./mentci_box.nix {
    inherit craneLib pkgs repo_root;
  };

  mentci_box_default = pkgs.callPackage ./mentci-box-default.nix {
    inherit mentci_box;
  };

  mentci_vcs = import ./mentci_vcs.nix {
    inherit craneLib pkgs;
  };

  coding_agent = import ./coding_agent.nix {
    inherit pkgs;
    src = pi_mono_src;
  };

  unified_llm = import ./unified_llm.nix {
    inherit pkgs;
    src = pi_mono_src;
  };

  pi_agent_rust = import ./pi_agent_rust.nix {
    inherit pkgs;
    src = pi_agent_rust_src;
    rust_toolchain = rust_toolchain;
    pi_mono_src = pi_mono_src;
  };

  execute = pkgs.runCommand "mentci-execute" { } ''
    mkdir -p "$out/bin"
    ln -s "${mentci_ai}/bin/execute" "$out/bin/execute"
  '';

  attractor = import ./attractor.nix {
    inherit pkgs;
    src = attractor_src;
    coding_agent = coding_agent;
    unified_llm = unified_llm;
  };

  coding_agent_check = import ./coding_agent_check.nix {
    inherit pkgs coding_agent;
  };

  components_index_check = import ./components_index_check.nix {
    inherit pkgs repo_root;
  };

  common_packages = import ./common_packages.nix {
    inherit pkgs system;
    inherit rust_toolchain rust_analyzer;
    inherit codex_cli_nix;
    inherit gemini_cli gemini_tui;
    inherit mentci_vcs coding_agent unified_llm pi_agent_rust execute;
  };

  jail_sources = import ./jail_sources.nix {
    inherit inputs;
    attractor_src = attractor_src;
    attractor_docs_src = attractor_docs_src;
  };

  gemini_cli = pkgs.callPackage ./gemini-cli.nix { };

  gemini_tui = pkgs.callPackage ./gemini-tui.nix {
    inherit gemini_cli;
  };

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
  inherit mentci_ai mentci_box mentci_box_default mentci_vcs execute execute_check attractor common_packages jail_sources gemini_cli gemini_tui dev_shell coding_agent coding_agent_check components_index_check unified_llm pi_agent_rust;
  mk_shell = import ./mk-shell.nix { inherit pkgs; };
}
