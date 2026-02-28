{ craneLib, pkgs }:

import ../mentci-user/default.nix {
  inherit craneLib pkgs;
}
