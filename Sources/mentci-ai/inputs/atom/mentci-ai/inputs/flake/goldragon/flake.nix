{
  description = "Li Goldragon's Kriom";

  outputs = { self }:
    { NeksysProposal = import ./datom.nix; };
}
