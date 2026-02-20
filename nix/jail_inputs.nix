{ inputs, attractor_docs }:

{
  inherit (inputs) criomos sema lojix seahawk skrips mkZolaWebsite;
  inherit (inputs) webpublish goldragon maisiliym kibord aski attractor opencode;
  attractor-docs = attractor_docs;
}
