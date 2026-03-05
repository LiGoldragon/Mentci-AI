{ pkgs, mentci_box, mentci_box_src, capnproto }:

let
  defaultRequest = "${mentci_box_src}/default_request.bin";
in
pkgs.writeShellScriptBin "mentci-box-default" ''
  exec ${mentci_box}/bin/mentci-box ${defaultRequest} "$@"
''
