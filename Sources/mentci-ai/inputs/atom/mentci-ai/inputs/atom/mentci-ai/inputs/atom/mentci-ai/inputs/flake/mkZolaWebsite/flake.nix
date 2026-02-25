{
  description = "Make zola website";

  outputs = { self }: {
    type = "worldFunction";

    function = { lib, zola, stdenv, kynvyrt, reseter-css, base16-styles, buttons }:

      mkZolaWebsiteInputs@{ name ? false, theme ? "google-light", src }:
      let
        inherit (lib) concatMapStringsSep;


        indexContentString = builtins.readFile (src + /_index.md);
        indexSplitStrings = lib.splitString "+++\n" indexContentString;

        indexFrontMatter = builtins.fromTOML (builtins.elemAt indexSplitStrings 1);

        matrixID = indexFrontMatter.matrixID;

        title = mkZolaWebsiteInputs.name or indexFrontMatter.title;

        zolaConfig = {
          inherit title;
          base_url = "/";
          compile_sass = true;
          build_search_index = true;
          markdown =
            { highlight_code = true; };
          extra = { matrix.id = matrixID; };
        };

        zolaConfigTomlFile = kynvyrt {
          neim = "config";
          valiu = zolaConfig;
          format = "toml";
        };

        scssPackages = [ reseter-css buttons ];

        linkSassLibrary = package: ''
          ln -s ${package}/lib/scss ./sass/${package.name}
        '';

        linkSassLibrariesShell = concatMapStringsSep "\n" linkSassLibrary scssPackages;

      in
      stdenv.mkDerivation {
        name = title + "-zolaWebsite";
        version = src.shortRev or "unversioned";
        src = self;

        nativeBuildInputs = [ zola ];

        buildPhase = ''
          ${linkSassLibrariesShell}
          ln -s ${base16-styles}/lib/scss/base16-${theme}.scss ./sass/_base16-theme.scss
          ln -s ${zolaConfigTomlFile} ./config.toml
          ln -s ${src} ./content
          zola build
        '';

        installPhase = ''
          mkdir -p $out
          cp -r public/* $out
        '';
      };

  };
}
