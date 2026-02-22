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
      priKriomz = {
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

    tiger = {
      spici = "edj";
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
        swapDevices = [{ device = "/dev/disk/by-uuid/4dcf2ad2-4677-483f-9900-046f0f53053a"; }];
      };
      priKriomz = {
        niksPriKriom = "P2pDdZrmbLO8EBUuwLvt+FJlqh4ZRVczgGV/Tmr1Bis=";
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIKeoCIfvbUDue17FBglmKF1N2cKH9CWsS5EcXOyBxq5T";
        yggdrasil = {
          priKriom = "38ed4c7c9ffd2e79361af3202f406810069fa37ff991e6d1d434a529ba75dfb3";
          address = "202:3895:9c1b:16:8c36:4f28:66fe:85fc";
          subnet = "302:3895:9c1b:16";
        };
      };
    };

    xerxes = {
      spici = "haibrid";
      saiz = 3;
      trost = 3;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "ThinkPadX240";
        korz = 2;
      };

      neksysIp = "5::1/128";
      linkLocalIPs = [{ spici = "ethernet"; suffix = "aec6:ecad:34e0:b41f"; }];
      wireguardPriKriom = "oOeqiGZC+oaIL0rKirFyiJ4N6qDQWICZ2wRelLMOR0o=";

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
      priKriomz = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIA0GR4qcTTTgkdes3aYFz998tWC6w0wss4nRDfvAFBda";
        niksPriKriom = "AAqjPi1kOaOWyZrwupTpHNL/7SYjYmaeDu1J+hNtVFA=";
        yggdrasil = {
          priKriom = "2748bfce76cf7c2a8d6d73241fadd4cad2a32f631df6a4be4bfb33969b0490e0";
          address = "202:c5ba:18c:4984:1eab:9494:66df:291";
          subnet = "302:c5ba:18c:4984";
        };
      };
    };

    dante = {
      spici = "sentyr";
      saiz = 2;
      trost = 3;
      mycin = {
        spici = "metyl";
        ark = "x86-64";
        modyl = "ThinkPadX230";
        korz = 2;
      };
      neksysIp = "5::2/128";
      linkLocalIPs = [{ spici = "ethernet"; suffix = "3e97:eff:fea3:30d2"; }];
      wireguardPriKriom = "3HeLSQr+lbJp4w9oUILEVWrBpYYCoQzxkARWDCBf5j8=";
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
      priKriomz = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIDjXtLUY5gSvogSTzU54x/EryOgNVZ2HkCYQL1d9lKEB";
        yggdrasil = {
          priKriom = "c9eca45c87e2f4478eec5488e2183fcf5536a34352201dec785c74dd1946268c";
          address = "200:6c26:b746:f03a:1770:e227:56ee:3bcf";
          subnet = "300:6c26:b746:f03a";
        };
        niksPriKriom = "cV4q5ycwjEd/VhSKhZolX8VVhv47MHSfatOBNq7bjuE=";
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
      priKriomz = {
        eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAICWYATms2fd9JVJNrMetB8uNNP/ZKNG9/QhJBkyJ/6VX";
        yggdrasil = {
          priKriom = "d7cbcd3122a0ba4d0ca1f662ecbab4bc376343ece193749c3f94a6e1b24f42db";
          address = "200:5068:659d:babe:8b65:e6bc:133a:268a";
          subnet = "300:5068:659d:babe";
        };
        niksPriKriom = "Mx+wLIfTWjYm2b+gI6X5vxY0eGtScNB1+51DxIZbeus=";
      };
    };

    eibetik = {
      spici = "sentyr";
      saiz = 1;
      trost = 2;
      mycin = {
        spici = "pod";
        ubyrAstri = "xerxes";
        ubyrKrimyn = "li";
        korz = 1;
      };
    };
  };

  krimynz = {
    li = {
      spici = "Onlimityd";
      saiz = 3;
      kibord = "colemak";
      githubId = "LiGoldragon";
      priKriomz = {
        dante = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIK769R2iKyr5rgBvR9OFeSN2kdo8h+LtXVUjzdFLf4vl";
          keygrip = "AD305831DD33E62F9AD587718D4E5E6999CD84EA";
        };
        tiger = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAILTm644m4foET1j+DGHl0gfEyGD9A4YltsNOidDUn3BG";
          keygrip = "1785B4D2813D7ED1913A05FA74FF42B9153EEFF0";
        };
        xerxes = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIEygb1Ft1hIB+ExPGLq08im9rFYvOeYXX+NetgqjI3Db";
          keygrip = "63A149ECC539BAF2B001C57E9A55A566BCD48446";
        };
      };
    };

    bird = {
      spici = "Sema";
      saiz = 3;
      kibord = "colemak";
      priKriomz = {
        juno = {
          eseseitc = "AAAAC3NzaC1lZDI1NTE5AAAAIKTc1b3BgAHoY1R7gY8YsaPI7AeTZzDyWJPhlyEkjv/e";
          keygrip = "2B02ECA0AE0D9A027C013234DEB7888B37E70DBF";
        };
      };
    };

    maikro = {
      spici = "Niks";
      saiz = 1;
      kibord = "colemak";
      priKriomz = {
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
    krimynz = {
      maikro = 3;
      li = 3;
      bird = 2;
    };
  };
}
