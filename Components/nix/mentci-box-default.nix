{ pkgs, mentci_box, capnproto }:

let
  defaultRequest = ../mentci-box/default_request.bin;
in
pkgs.writeShellScriptBin "mentci-box-default" ''
  exec ${mentci_box}/bin/mentci-box ${defaultRequest} "$@"
''
