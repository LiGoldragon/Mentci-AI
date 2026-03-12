{ pkgs }:
pkgs.python3Packages.buildPythonPackage {
  pname = "any-llm";
  version = "1.10.0";
  src = pkgs.runCommand "any-llm-src" {} ''
    mkdir -p $out/any_llm
    touch $out/any_llm/__init__.py
    cat <<EOF > $out/any_llm/exceptions.py
    class AnyLLMError(Exception): pass
    class ProviderError(AnyLLMError): pass
    EOF
    cat <<EOF > $out/pyproject.toml
    [build-system]
    requires = ['setuptools']
    build-backend = 'setuptools.build_meta'
    [project]
    name = 'any-llm'
    version = '1.10.0'
    EOF
  '';
  pyproject = true;
  nativeBuildInputs = [ pkgs.python3Packages.setuptools ];
}
