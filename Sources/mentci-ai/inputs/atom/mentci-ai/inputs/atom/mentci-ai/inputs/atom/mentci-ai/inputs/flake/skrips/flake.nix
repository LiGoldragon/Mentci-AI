{
  description = "Skrips";

  outputs = { self }: {
    SobUyrld = {
      modz = [ "pkgs" "pkdjz" ];

      lamdy = { runCommand, mksh, sd }:
        let
          inherit (builtins) readFile readDir mapAttrs;

          mkSkrip = name: srcPath:
            let
              mksheBang = "#!" + mksh + "/bin/mksh";
            in
            runCommand name { inherit srcPath; } ''
              mkdir --parents $out/bin/
              for skrip in $srcPath/*
              do
              export neim=$(basename $skrip)
              cp ''$skrip $out/bin/''${neim%.sh}
              done
              ${sd}/bin/sd --string-mode '#!/usr/bin/env mksh' '${mksheBang}' $out/bin/*
              chmod 755 $out/bin/*
            '';

          mkTopDirEntry = name: fileType:
            assert (fileType == "directory");
            mkSkrip name (self + "/${name}");

          topDir = removeAttrs (readDir self) [ "flake.nix" ];

        in
        mapAttrs mkTopDirEntry topDir;
    };
  };
}
