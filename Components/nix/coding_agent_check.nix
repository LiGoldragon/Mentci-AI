{ pkgs, coding_agent }:

pkgs.runCommand "pi-coding-agent-check" { } ''
  test -x ${coding_agent}/bin/pi
  ${coding_agent}/bin/pi --help > "$TMPDIR/pi-help.txt"
  grep -qi "usage" "$TMPDIR/pi-help.txt"
  touch "$out"
''
