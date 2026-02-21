{ pkgs, scripts_dir }:

pkgs.stdenvNoCC.mkDerivation {
  pname = "mentci-jj";
  version = "0.1.0";
  src = scripts_dir;
  dontBuild = true;
  installPhase = ''
    mkdir -p $out/bin $out/lib/mentci/scripts
    cp -R "$src"/* "$out/lib/mentci/scripts/"

    cat > "$out/bin/mentci-jj" <<EOF
#!/usr/bin/env bash
exec ${pkgs.babashka}/bin/bb "$out/lib/mentci/scripts/jj_workflow/main.clj" --runtime "\$(pwd)/workspace/.mentci/runtime.json" "\$@"
EOF
    chmod +x "$out/bin/mentci-jj"
  '';
}
