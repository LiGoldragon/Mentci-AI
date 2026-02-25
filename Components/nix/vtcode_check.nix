{ pkgs, vtcode, mentci_user, repo_root }:

let
  # Determine the setup bin path relative to the repo root
  setupBin = "${repo_root}/Components/mentci-user/data/setup_4311f0704ec238e2.bin";
in
pkgs.runCommand "vtcode-check" {
  nativeBuildInputs = [ vtcode mentci_user pkgs.gnugrep ];
  # Note: Nix tests are sandbox-isolated, but mentci-user needs local secrets
  # This test will primarily verify binary sanity and help-output behavior
  # to ensure the build is correct and provider logic is linked.
} ''
  echo "Checking vtcode binary existence..."
  test -x ${vtcode}/bin/vtcode
  
  echo "Verifying help output..."
  vtcode --help > help.txt
  grep -qi "coding assistant" help.txt
  
  echo "Checking version..."
  vtcode --version
  
  echo "Verifying configuration loading logic (dry-run)..."
  # Use the cheapest/lightest model if we ever triggered actual API calls,
  # but here we check CLI arg parsing.
  vtcode --model "gemini-2.0-flash-lite" --print "test" --help > /dev/null
  
  touch $out
''
