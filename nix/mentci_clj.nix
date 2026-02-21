{ pkgs, scripts_dir }:

pkgs.stdenv.mkDerivation {
  pname = "mentci-clj-orchestrator";
  version = "0.1.0";
  src = scripts_dir;
  nativeBuildInputs = [ pkgs.makeWrapper ];
  buildInputs = [ pkgs.babashka ];
  installPhase = ''
    mkdir -p $out/bin $out/lib/mentci/scripts
    cp -R * $out/lib/mentci/scripts/
    
    # Wrap every main.clj found in subdirectories
    for main_file in $(find $out/lib/mentci/scripts -name "main.clj"); do
      script_dir=$(dirname "$main_file")
      script_name=$(basename "$script_dir")
      
      makeWrapper ${pkgs.babashka}/bin/bb "$out/bin/$script_name" \
        --add-flags "$main_file"
    done
  '';
}
