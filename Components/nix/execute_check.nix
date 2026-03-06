{ pkgs, execute }:

pkgs.runCommand "mentci-execute-check" { } ''
  ${execute}/bin/execute > "$TMPDIR/execute-help.txt"
  grep -q 'commands:' "$TMPDIR/execute-help.txt"
  grep -q 'root-guard' "$TMPDIR/execute-help.txt"

  ${execute}/bin/execute version > "$TMPDIR/execute-version.txt"
  test -s "$TMPDIR/execute-version.txt"

  touch "$out"
''
