{ pkgs, gemini_cli }:

pkgs.writeShellScriptBin "gemini-tui" ''
  # Override environment variables to ensure interactive mode and color support
  export TERM=''${TERM:-xterm-256color}
  [ "$TERM" = "dumb" ] && export TERM=xterm-256color
  export CI=false
  export FORCE_COLOR=1

  # Execute the original gemini binary with all arguments
  exec ${gemini_cli}/bin/gemini "$@"
''
