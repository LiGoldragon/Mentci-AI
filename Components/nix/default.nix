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
, vtcode_src
, repo_root
, mentci_aid_src
, ai_src
, chronos_src
, mentci_stt_src
, mentci_user_src
, mentci_mcp_src
, aski_lib_src
, mentci_box_src
, mentci_box_lib_src
, mentci_launch_src
}:

let
  mentci_ai = import ./mentci_ai.nix {
    inherit craneLib;
    src = ai_src;
  };

  chronos = import ./chronos.nix {
    inherit craneLib pkgs;
    src = chronos_src;
  };

  mentci_stt = import ./mentci_stt.nix {
    inherit craneLib pkgs;
    src = mentci_stt_src;
    schema_src = ../schema;
    inherit mentci_box_lib_src mentci_user_src;
  };

  mentci_user = import ./mentci_user.nix {
    inherit craneLib pkgs;
    src = mentci_user_src;
  };

  mentci_mcp = import ./mentci_mcp.nix {
    inherit craneLib pkgs;
    src = mentci_mcp_src;
    schema_src = ../schema;
    inherit aski_lib_src;
  };

  mentci_box = import ./mentci_box.nix {
    inherit craneLib pkgs;
    src = mentci_box_src;
    schema_src = ../schema;
    inherit mentci_box_lib_src;
  };

  mentci_box_default = pkgs.callPackage ./mentci-box-default.nix {
    inherit mentci_box mentci_box_src;
  };

  mentci_launch = import ./mentci_launch.nix {
    inherit craneLib pkgs;
    src = mentci_launch_src;
    schema_src = ../schema;
  };

  mentci_vcs = import ./mentci_vcs.nix {
    inherit craneLib pkgs;
  };

  pi = import ./pi.nix {
    inherit pkgs;
    src = pi_mono_src;
  };

  unified_llm = import ./unified_llm.nix {
    inherit pkgs;
    src = pi_mono_src;
  };

  pi_dev = import ./pi-dev.nix {
    inherit pkgs;
    src = pi_mono_src;
  };

  pi_linkup_extension = import ./pi-linkup-extension.nix {
    inherit pkgs;
  };

  pi_with_extensions = import ./pi-with-extensions.nix {
    inherit pkgs;
    pi = pi;
    pi_linkup_extension = pi_linkup_extension;
  };

  pi_rust = import ./pi-rust.nix {
    inherit pkgs;
    src = pi_agent_rust_src;
    rust_toolchain = rust_toolchain;
    pi_mono_src = pi_mono_src;
  };

  vtcode = import ./vtcode.nix {
    inherit pkgs rust_toolchain;
    src = vtcode_src;
  };

  execute = import ./execute.nix {
    inherit craneLib pkgs;
    src = mentci_aid_src;
    schema_src = ../schema;
    inherit mentci_box_lib_src;
  };

  mentci_bootstrap = import ./mentci_bootstrap.nix {
    inherit pkgs mentci_launch;
  };

  attractor = import ./attractor.nix {
    inherit pkgs;
    src = attractor_src;
    pi = pi;
    unified_llm = unified_llm;
  };

  pi_check = import ./pi_check.nix {
    inherit pkgs pi;
  };

  pi_with_extensions_check = import ./pi_with_extensions_check.nix {
    inherit pkgs;
    pi_with_extensions = pi_with_extensions;
  };

  components_index_check = import ./components_index_check.nix {
    inherit pkgs repo_root;
  };

  common_packages = import ./common_packages.nix {
    inherit pkgs system;
    inherit rust_toolchain rust_analyzer;
    inherit codex_cli_nix;
    inherit gemini_cli gemini_tui;
    pi_dev = pi_with_extensions;
    inherit unified_llm vtcode mentci_bootstrap chronos mentci_stt mentci_user mentci_mcp;
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
      pi = pi_with_extensions;
    };

  execute_check = import ./execute_check.nix {
    inherit pkgs execute;
  };
in
{
  inherit mentci_ai mentci_box mentci_box_default mentci_launch mentci_vcs execute mentci_bootstrap chronos mentci_stt mentci_user mentci_mcp execute_check attractor common_packages jail_sources gemini_cli gemini_tui dev_shell pi pi_dev pi_with_extensions pi_linkup_extension pi_check pi_with_extensions_check components_index_check unified_llm pi_rust vtcode;
  mk_shell = import ./mk-shell.nix { inherit pkgs; };
}
