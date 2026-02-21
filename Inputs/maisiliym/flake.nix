{
  description = "Maisiliym metastri";

  outputs = _: {
    NeksysProposal = import ./datom.nix;
  };
}
