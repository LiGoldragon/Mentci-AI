{ pkgs, gemini_cli }:

pkgs.symlinkJoin {
  name = "gemini-tui";
  paths = [
    (pkgs.writeShellScriptBin "gemini-tui" ''
      # Override environment variables to ensure interactive mode and color support.
      # The dev shell forces TERM=dumb and CI=true; we must undo these for a real TUI.
      export TERM=xterm-256color
      export COLORTERM=truecolor
      export FORCE_COLOR=3
      export CLICOLOR_FORCE=1
      unset CI

      # Execute the original gemini binary with all arguments
      exec ${gemini_cli}/bin/gemini "$@"
    '')
  ];
}
