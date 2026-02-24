{ pkgs, mentci_ai }:

pkgs.runCommand "mentci-execute-check" { } ''
  ${mentci_ai}/bin/execute > "$TMPDIR/execute-help.txt"
  grep -q 'commands:' "$TMPDIR/execute-help.txt"
  grep -q 'root-guard' "$TMPDIR/execute-help.txt"

  ${mentci_ai}/bin/execute version > "$TMPDIR/execute-version.txt"
  test -s "$TMPDIR/execute-version.txt"

  touch "$out"
''
