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
    
    # Criome Ecosystem
    ln -sfn ${inputs.criomos} "${inputsPath}/criomos"
    ln -sfn ${inputs.sema} "${inputsPath}/sema"
    ln -sfn ${inputs.lojix} "${inputsPath}/lojix"
    ln -sfn ${inputs.seahawk} "${inputsPath}/seahawk"
    ln -sfn ${inputs.skrips} "${inputsPath}/skrips"
    ln -sfn ${inputs.mkZolaWebsite} "${inputsPath}/mkZolaWebsite"

    # LiGoldragon Ecosystem
    ln -sfn ${inputs.webpublish} "${inputsPath}/webpublish"
    ln -sfn ${inputs.goldragon} "${inputsPath}/goldragon"
    ln -sfn ${inputs.maisiliym} "${inputsPath}/maisiliym"
    ln -sfn ${inputs.ndi} "${inputsPath}/ndi"
    ln -sfn ${inputs.kibord} "${inputsPath}/kibord"

    # Self-reference
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