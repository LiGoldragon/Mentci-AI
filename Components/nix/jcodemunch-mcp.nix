{ pkgs }:

pkgs.python3Packages.buildPythonApplication rec {
  pname = "jcodemunch-mcp";
  version = "1.0.0-mentci.1";
  pyproject = true;

  src = pkgs.fetchFromGitHub {
    owner = "LiGoldragon";
    repo = "jcodemunch-mcp";
    rev = "991bd5848583e53c1d8531d105b8e9f2bedd8d21";
    hash = "sha256-hXQ7k+iqJE7w5yvvfa7BpkDgFO8Xb0DbPman9fZLYbY=";
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
