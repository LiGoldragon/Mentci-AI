{
  description = "kibord";

  inputs = {
    qmk_firmware = {
      url = "github:qmk/qmk_firmware/bc15c4f4ab81c1e2950dfc1c38cf86dc626573c9";
      flake = false;
    };
    ChibiOS = {
      url = "github:qmk/ChibiOS";
      flake = false;
    };
    ChibiOS-Contrib = {
      url = "github:qmk/ChibiOS-Contrib";
      flake = false;
    };
    uGFX = {
      url = "github:qmk/uGFX";
      flake = false;
    };
    googletest = {
      url = "github:qmk/googletest";
      flake = false;
    };
    lufa = {
      url = "github:qmk/lufa";
      flake = false;
    };
    v-usb = {
      url = "github:qmk/v-usb";
      flake = false;
    };
    printf = {
      url = "github:qmk/printf";
      flake = false;
    };
    kp_boot_32u4 = {
      url = "github:ahtn/kp_boot_32u4";
      flake = false;
    };
    hexdumpSrc = {
      url = "github:sarnold/hexdump";
      flake = false;
    };
  };

  outputs = localFlakes@{ self, qmk_firmware, kp_boot_32u4, hexdumpSrc, ... }: {
    SobUyrld = {
      modz = [ "pkgs" ];


      lamdy = with builtins;
        { self
        , lib
        , pkgsCross
        , stdenv
        , python3
        , python3Packages
        , dfu-programmer
        , dfu-util
        , diffutils
        , git
        , avrdude
        , gcc-arm-embedded
        , teensy-loader-cli
        , hidapi
        , fetchhg
        }:

        let
          inherit (lib) optionals mapAttrsToList optional;
          inherit (stdenv) mkDerivation;

          inherit (python3Packages) buildPythonApplication
            buildPythonPackage fetchPypi intelhex cffi;

          submodulesIndeks = {
            ChibiOS = "lib/chibios";
            ChibiOS-Contrib = "lib/chibios-contrib";
            uGFX = "lib/ugfx";
            googletest = "lib/googletest";
            lufa = "lib/lufa";
            v-usb = "lib/vusb";
            printf = "lib/printf";
          };

          linkSubmodule = n: v:
            let mod = localFlakes.${n};
            in "ln -s ${mod}/* ${v}";

          linkSubmodules = concatStringsSep "\n"
            (mapAttrsToList linkSubmodule submodulesIndeks);

          hjson = buildPythonPackage rec {
            pname = "hjson";
            version = "3.0.1";
            src = fetchPypi {
              inherit pname version;
              sha256 = "1yaimcgz8w0ps1wk28wk9g9zdidp79d14xqqj9rjkvxalvx2f5qx";
            };
            doCheck = false;
          };

          milc = buildPythonPackage rec {
            pname = "milc";
            version = "1.0.10";
            src = fetchPypi {
              inherit pname version;
              sha256 = "1q1p7qrqk78mw67nhv04zgxaq8himmdxmy2vp4fmi7chwgcbpi32";
            };
            propagatedBuildInputs = with python3Packages; [
              appdirs
              argcomplete
              colorama
            ];
            doCheck = false;
          };

          pythonEnv = python3.withPackages (p: with p; [
            # requirements.txt
            appdirs
            argcomplete
            colorama
            hjson
            jsonschema
            milc
            pygments
            # requirements-dev.txt
            nose2
            flake8
            pep8-naming
            yapf
          ]);

          avrlibc = pkgsCross.avr.libcCross;
          avr_incflags = [
            "-isystem ${avrlibc}/avr/include"
            "-B${avrlibc}/avr/lib/avr5"
            "-L${avrlibc}/avr/lib/avr5"
            "-B${avrlibc}/avr/lib/avr35"
            "-L${avrlibc}/avr/lib/avr35"
            "-B${avrlibc}/avr/lib/avr51"
            "-L${avrlibc}/avr/lib/avr51"
          ];

          bypassGccBug = true;
          disableArrayBoundsFlag = "-Wno-array-bounds";

          avrPackages = [
            avrdude
            pkgsCross.avr.buildPackages.gcc
            pkgsCross.avr.buildPackages.binutils
            avrlibc
          ];

          mkQmkOS = { kibord, avr ? false, arm ? false, teensy ? false }:
            let
              inherit (kibord) iuniksDir keyboardModel;
              keymap = builtins.baseNameOf iuniksDir;

            in
            mkDerivation {
              pname = "qmk-os";
              version = qmk_firmware.shortRev;
              src = qmk_firmware;

              buildInputs = [ dfu-programmer dfu-util diffutils git pythonEnv ]
                ++ optionals avr avrPackages
                ++ optionals arm [ gcc-arm-embedded ]
                ++ optionals teensy [ teensy-loader-cli ];

              postPatch = ''
                ${linkSubmodules}
                ln -s ${iuniksDir} ./keyboards/${keyboardModel}/keymaps/${keymap}
              '';

              CFLAGS = optionals avr avr_incflags ++ (optional bypassGccBug disableArrayBoundsFlag);
              ASFLAGS = optionals avr avr_incflags;

              buildPhase = ''
                make ${keyboardModel}:${keymap}
              '';

              installPhase = ''
                mkdir -p $out
                cp ./.build/*hex $out/
              '';
            };


          hexdump = buildPythonPackage {
            pname = "hexdump";
            version = hexdumpSrc.shortRev;
            src = hexdumpSrc;
            doCheck = false;
          };

          easyhid = buildPythonPackage rec {
            pname = "easyhid";
            version = "0.0.9";
            src = fetchPypi {
              inherit pname version;
              sha256 = "68bfed780bb0c2cebebfc1e977093985b0ccb512ad20b8bb030d73b525f3eaf2";
            };
            postPatch = ''
              substituteInPlace easyhid/easyhid.py \
                --replace "ctypes.util.find_library('hidapi-libusb')" \
                "'${hidapi}/lib/libhidapi-libusb.so.0'"
            '';
            buildInputs = [ hidapi cffi ];
          };

          kpBootPackages = [
            intelhex
            cffi
            hexdump
            easyhid
            hidapi
          ];

          kpBootCli = buildPythonApplication {
            name = "kp-boot-cli";
            version = kp_boot_32u4.shortRev;
            src = kp_boot_32u4;
            propagatedBuildInputs = kpBootPackages;
          };

          kpBootloader = mkDerivation {
            name = "kp-bootloader";
            version = kp_boot_32u4.shortRev;
            src = kp_boot_32u4;
            buildInputs = kpBootPackages # May not be needed
              ++ avrPackages;
          };

        in
        {
          inherit kpBootCli kpBootloader;

          LiGoldragon = {
            minidox = mkQmkOS {
              avr = true;
              kibord = {
                iuniksDir = ./maple_computing/minidox/one;
                keyboardModel = "ergodone";
              };
            };
            ergodone = mkQmkOS {
              avr = true;
              kibord = {
                iuniksDir = ./ergodone/coleremak;
                keyboardModel = "ergodone";
              };
            };
          };

        };

    };
  };
}
