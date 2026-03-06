{ pkgs, mentci_launch }:

pkgs.writeShellScriptBin "mentci-bootstrap" ''
  cmd="''${1:-launcher}"
  shift || true

  case "$cmd" in
    launcher)
      exec ${mentci_launch}/bin/mentci-launch "$@"
      ;;
    version)
      echo "mentci-bootstrap 0.1.0"
      ;;
    *)
      echo "usage: mentci-bootstrap [launcher|version] [args...]" >&2
      exit 2
      ;;
  esac
''
