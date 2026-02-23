{ pkgs, mentci_box, capnp }:

let
  defaultRequest = ../mentci-box/default_request.bin;
in
pkgs.writeShellScriptBin "mentci-box-default" ''
  exec ${mentci_box}/bin/mentci-box ${defaultRequest} "$@"
''
