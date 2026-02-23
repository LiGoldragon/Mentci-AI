{ pkgs, mentci_ai }:

pkgs.runCommand "mentci-execute-check" { } ''
  mkdir -p scripts/sample
  cat > scripts/sample/main.clj <<'EOF'
  (println "sample")
  EOF

  export MENTCI_SCRIPTS_DIR="$PWD/scripts"
  ${mentci_ai}/bin/execute list > "$TMPDIR/execute-list.txt"
  grep -q '^sample$' "$TMPDIR/execute-list.txt"

  touch "$out"
''
