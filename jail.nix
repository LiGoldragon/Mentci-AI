{ pkgs, inputs, inputsPath ? "inputs" }:

pkgs.mkShell {
  name = "mentci-jail";

  buildInputs = [
    pkgs.capnproto
    pkgs.gdb
    pkgs.strace
    pkgs.valgrind
    # Add other "Level 5" tools here as needed
  ];

  shellHook = ''
    echo "Initializing Mentci-AI Level 5 Jail Environment..."

    # Ensure the inputs directory exists
    mkdir -p "${inputsPath}"

    # Link inputs to the defined path for easy access
    # Using 'ln -sfn' to force link creation/update without dereferencing if it's a directory
    ln -sfn ${inputs.criomos} "${inputsPath}/criomos"
    ln -sfn ${inputs.sema} "${inputsPath}/sema"
    ln -sfn ${inputs.lojix} "${inputsPath}/lojix"
    ln -sfn ${inputs.webpublish} "${inputsPath}/webpublish"

    # Also link the current directory as 'mentci-ai' to simulate self-loading
    ln -sfn $(pwd) "${inputsPath}/mentci-ai"

    echo "Linked Level 5 repositories in '${inputsPath}/':"
    ls -l "${inputsPath}/"

    # Display Level 5 Programming Context
    if [ -f Level5-Ai-Coding.md ]; then
      echo -e "\n--- Level 5 Programming Context ---\n"
      cat Level5-Ai-Coding.md
      echo -e "\n-----------------------------------\n"
    fi

    echo "Welcome to the Mentci-AI Jail."
  '';
}
