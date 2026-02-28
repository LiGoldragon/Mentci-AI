{ craneLib, pkgs, src }:

import ../mentci-user/default.nix {
  inherit craneLib pkgs src;
}
