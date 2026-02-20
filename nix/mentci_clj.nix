{ pkgs, scripts_dir }:

pkgs.stdenv.mkDerivation {
  pname = "mentci-clj-orchestrator";
  version = "0.1.0";
  src = scripts_dir;
  nativeBuildInputs = [ pkgs.makeWrapper ];
  buildInputs = [ pkgs.babashka ];
  installPhase = ''
    mkdir -p $out/bin
    cp *.clj $out/bin/
    for f in $out/bin/*.clj; do
      chmod +x "$f"
      mv "$f" "$f.orig"
      makeWrapper ${pkgs.babashka}/bin/bb "$f" --add-flags "$f.orig"
    done
  '';
}
