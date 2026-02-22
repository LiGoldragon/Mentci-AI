{
  astriz = {
    balboa = {
      spici = "sentyr";
      mycin = {
        spici = "metyl";
        ark = "arm64";
        modyl = "rock64";
        korz = 4;
      };
      preCriomes = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIPvxPTAPPKe6Ne9MW4C04Ihe0g4NS12xydci/JW59pIe";
      };
      io = {
        disks = {
          "/" = {
            device = "/dev/disk/by-label/NIXOS_SD";
            fsType = "ext4";
          };
        };
      };
    };

    hades = {
      spici = "router";
      saiz = 2;
      trost = 3;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "Thinkpadx250";
        korz = 2;
      };
      neksysIp = "5::6/128";
      linkLocalIPs = [ ];
      io = {
        kibord = "colemak";
        butlodyr = "uefi";
        disks = {
          "/" = {
            device = "/dev/disk/by-uuid/85f4a2b7-e168-4252-8805-989a4cf8a81c";
            fsType = "ext4";
          };
          "/boot" = {
            device = "/dev/disk/by-uuid/AEF9-C68A";
            fsType = "vfat";
          };
        };
        swapDevices = [ { device = "/dev/disk/by-uuid/17a56c75-f8fc-428c-b8a4-df3d9449fcf7"; } ];
      };
      preCriomes = {
        niksPreCriome = "0J1372hbJdemn5rVicpVSOVjB2cm8uyI3ypkFoRihRM=";
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIOQascm6i8E72qmmcaw114fSStgy3syEnNp3hv2qJvbr";
        yggdrasil = {
          preCriome = "e48fd5a04f1c2fd1f73707dd4dcc1e7e1513c5575b4eb585bffd38b347a8dbf1";
          address = "200:36e0:54bf:61c7:a05c:1191:f045:6467";
          subnet = "300:36e0:54bf:61c7";
        };
      };
    };

    tiger = {
      spici = "edjTesting";
      saiz = 3;
      trost = 3;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "ThinkPadE15Gen2Intel";
        korz = 4;
      };
      neksysIp = "5::4/128";
      linkLocalIPs = [ ];
      io = {
        kibord = "colemak";
        butlodyr = "uefi";
        disks = {
          "/" = {
            device = "/dev/disk/by-uuid/158a5a78-3dbc-4c80-99b6-96e433bd836a";
            fsType = "ext4";
          };
          "/boot" = {
            device = "/dev/disk/by-uuid/50F3-9E04";
            fsType = "vfat";
          };
        };
        swapDevices = [ { device = "/dev/disk/by-uuid/4dcf2ad2-4677-483f-9900-046f0f53053a"; } ];
      };
      preCriomes = {
        niksPreCriome = "/1SYuET5/IIscPvZcgZyrfsMcRiCFcmV051+0U5YXVM=";
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIKeoCIfvbUDue17FBglmKF1N2cKH9CWsS5EcXOyBxq5T";
        yggdrasil = {
          preCriome = "38ed4c7c9ffd2e79361af3202f406810069fa37ff991e6d1d434a529ba75dfb3";
          address = "202:3895:9c1b:16:8c36:4f28:66fe:85fc";
          subnet = "302:3895:9c1b:16";
        };
      };
    };

    xerxes = {
      spici = "edjTesting";
      saiz = 3;
      trost = 3;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "ThinkPadX240";
        korz = 2;
      };

      neksysIp = "5::1/128";
      linkLocalIPs = [
        {
          spici = "ethernet";
          suffix = "aec6:ecad:34e0:b41f";
        }
      ];
      wireguardPreCriome = "oOeqiGZC+oaIL0rKirFyiJ4N6qDQWICZ2wRelLMOR0o=";

      io = {
        kibord = "colemak";
        butlodyr = "uefi";
        disks = {
          "/" = {
            device = "/dev/disk/by-uuid/c5788e39-39da-41f1-9f6c-4137abb0c406";
            fsType = "btrfs";
            options = [ "subvol=__active/root" ];
          };
          "/nix" = {
            device = "/dev/disk/by-uuid/c5788e39-39da-41f1-9f6c-4137abb0c406";
            fsType = "btrfs";
            options = [ "subvol=__active/nix" ];
          };
          "/home" = {
            device = "/dev/disk/by-uuid/c5788e39-39da-41f1-9f6c-4137abb0c406";
            fsType = "btrfs";
            options = [ "subvol=__active/home" ];
          };
          "/var" = {
            device = "/dev/disk/by-uuid/c5788e39-39da-41f1-9f6c-4137abb0c406";
            fsType = "btrfs";
            options = [ "subvol=__active/var" ];
          };
          "/boot" = {
            device = "/dev/disk/by-uuid/0A84-F61D";
            fsType = "vfat";
          };
        };
        swapDevices = [
          { device = "/dev/disk/by-uuid/03a014a2-9d6c-4c8e-b4be-b01415ad1beb"; }
        ];
      };
      preCriomes = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIE172GOItdwxQwJhnVeaSuHIwYrcj5bENafKL9y/mKZX";
        niksPreCriome = "AAqjPi1kOaOWyZrwupTpHNL/7SYjYmaeDu1J+hNtVFA=";
        yggdrasil = {
          preCriome = "2748bfce76cf7c2a8d6d73241fadd4cad2a32f631df6a4be4bfb33969b0490e0";
          address = "202:c5ba:18c:4984:1eab:9494:66df:291";
          subnet = "302:c5ba:18c:4984";
        };
      };
    };

    dante = {
      spici = "router";
      saiz = 2;
      trost = 3;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "ThinkPadX230";
        korz = 2;
      };
      neksysIp = "5::2/128";
      linkLocalIPs = [
        {
          spici = "ethernet";
          suffix = "3e97:eff:fea3:30d2";
        }
      ];
      wireguardPreCriome = "3HeLSQr+lbJp4w9oUILEVWrBpYYCoQzxkARWDCBf5j8=";
      io = {
        kibord = "colemak";
        butlodyr = "uefi";
        disks = {
          "/" = {
            device = "/dev/disk/by-uuid/6d6f8638-a0a7-49f9-81c2-2143b42ef3d5";
            fsType = "xfs";
          };
          "/boot" = {
            device = "/dev/disk/by-uuid/DE95-8F37";
            fsType = "vfat";
          };
        };
        swapDevices = [ ];
      };
      preCriomes = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIDjXtLUY5gSvogSTzU54x/EryOgNVZ2HkCYQL1d9lKEB";
        yggdrasil = {
          preCriome = "c9eca45c87e2f4478eec5488e2183fcf5536a34352201dec785c74dd1946268c";
          address = "200:6c26:b746:f03a:1770:e227:56ee:3bcf";
          subnet = "300:6c26:b746:f03a";
        };
        niksPreCriome = "cV4q5ycwjEd/VhSKhZolX8VVhv47MHSfatOBNq7bjuE=";
      };
    };

    juno = {
      spici = "haibrid";
      saiz = 3;
      trost = 3;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "ThinkPadX230";
        korz = 2;
      };
      io = {
        kibord = "colemak";
        butlodyr = "uefi";
        disks = {
          "/" = {
            device = "/dev/disk/by-uuid/63ff7f56-4f1f-4676-9b69-1b729bfcb555";
            fsType = "xfs";
          };
          "/home" = {
            device = "/dev/disk/by-uuid/fdfc8c28-7ea3-4cbc-962b-e156114e765d";
            fsType = "xfs";
          };
          "/boot" = {
            device = "/dev/disk/by-uuid/293E-399F";
            fsType = "vfat";
          };
        };
        swapDevices = [ ];
      };
      preCriomes = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAICWYATms2fd9JVJNrMetB8uNNP/ZKNG9/QhJBkyJ/6VX";
        yggdrasil = {
          preCriome = "d7cbcd3122a0ba4d0ca1f662ecbab4bc376343ece193749c3f94a6e1b24f42db";
          address = "200:5068:659d:babe:8b65:e6bc:133a:268a";
          subnet = "300:5068:659d:babe";
        };
        niksPreCriome = "Mx+wLIfTWjYm2b+gI6X5vxY0eGtScNB1+51DxIZbeus=";
      };
    };

    klio = {
      spici = "edj";
      saiz = 3;
      trost = 2;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "ThinkPadX250";
        korz = 2;
      };
      io = {
        kibord = "colemak";
        butlodyr = "uefi";
        disks = {
          "/" = {
            device = "/dev/disk/by-uuid/ac609ecf-be8e-49ee-a892-2552e74d97e3";
            fsType = "ext4";
          };
          "/boot" = {
            device = "/dev/disk/by-uuid/BBE9-75C7";
            fsType = "vfat";
          };
        };
        swapDevices = [ { device = "/dev/disk/by-uuid/f86fc799-cbc2-473b-94ce-abfc1aa040f2"; } ];
      };
      preCriomes = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAID7E6ca/f2Bkz5Z9ZX4ASHXPLF7TYI/Z4C0/Is/Ljpyu";
        niksPreCriome = "7N3PTqwWdA0p4XNjupKhLhVD5D0cEFcY/mUk3FRo5lI=";
        yggdrasil = {
          preCriome = "cef05ea020845caf73f2b3b3c15d27e3d55dd35b5155692a5464b7920b6b03b9";
          address = "200:621f:42bf:bef7:46a1:181a:9898:7d45";
          subnet = "300:621f:42bf:bef7";
        };
      };
    };

    eibetik = {
      spici = "sentyr";
      saiz = 1;
      trost = 2;
      mycin = {
        spici = "pod";
        ubyrAstri = "xerxes";
        ubyrUser = "li";
        korz = 1;
      };
    };
  };

  users = {
    li = {
      spici = "Onlimityd";
      saiz = 3;
      kibord = "colemak";
      githubId = "LiGoldragon";
      preCriomes = {
        dante = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIK769R2iKyr5rgBvR9OFeSN2kdo8h+LtXVUjzdFLf4vl";
          keygrip = "AD305831DD33E62F9AD587718D4E5E6999CD84EA";
        };
        tiger = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAILTm644m4foET1j+DGHl0gfEyGD9A4YltsNOidDUn3BG";
          keygrip = "1785B4D2813D7ED1913A05FA74FF42B9153EEFF0";
        };
        xerxes = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAICiHPTciuu3Yn8my604bWPo+T/9cIIMOqbrzPjqAt88/";
          keygrip = "DE688BEA4BAC3595A263EC1B76373B7C275899D9";
        };
      };
    };

    bird = {
      spici = "Sema";
      saiz = 3;
      kibord = "colemak";
      preCriomes = {
        klio = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIKTc1b3BgAHoY1R7gY8YsaPI7AeTZzDyWJPhlyEkjv/e";
          keygrip = "2B02ECA0AE0D9A027C013234DEB7888B37E70DBF";
        };
        tiger = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIGhaPt/4ytqbJ9oPk5DE5WEK23N36pZRbvaEuqr/MtfF";
          keygrip = "943502A0A9D25EF34C16E2ED2A120C26279C4248";
        };
      };
    };

    maikro = {
      spici = "Niks";
      saiz = 1;
      kibord = "colemak";
      preCriomes = {
        xerxes = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIEygb1Ft1hIB+ExPGLq08im9rFYvOeYXX+NetgqjI3Db";
          keygrip = "63A149ECC539BAF2B001C57E9A55A566BCD48446";
        };
      };

    };
  };

  domeinz = { };

  trost = {
    metastra = 3;
    metastriz = { };
    astriz = {
      balboa = 3;
      xerxes = 3;
      dante = 3;
      eibetik = 2;
      juno = 2;
      tiger = 3;
    };
    users = {
      maikro = 3;
      li = 3;
      bird = 2;
    };
  };
}
