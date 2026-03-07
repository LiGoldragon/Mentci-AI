{ pkgs }:

pkgs.python3Packages.buildPythonApplication rec {
  pname = "jcodemunch-mcp";
  version = "1.0.0";
  pyproject = true;

  src = pkgs.python3Packages.fetchPypi {
    pname = "jcodemunch_mcp";
    inherit version;
    hash = "sha256-JNjK3PzY9q8uSpEnK3SdaqLfxjhpetM1gZry6c0mqic=";
  };

  build-system = with pkgs.python3Packages; [ hatchling ];

  dependencies = with pkgs.python3Packages; [
    mcp
    httpx
    tree-sitter-language-pack
    pathspec
  ];

  pythonImportsCheck = [ "jcodemunch_mcp" ];
}
