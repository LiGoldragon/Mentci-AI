{ pkgs, pi }:

pkgs.runCommand "pi-check" { } ''
  test -x ${pi}/bin/pi
  ${pi}/bin/pi --help > "$TMPDIR/pi-help.txt"
  grep -qi "usage" "$TMPDIR/pi-help.txt"
  touch "$out"
''
