{ inputs, attractor_src, attractor_docs_src }:

{
  inherit (inputs) criomos sema lojix seahawk skrips mkZolaWebsite;
  inherit (inputs) webpublish goldragon maisiliym kibord bookofsol bookofgoldragon aski opencode;
  inherit (inputs) leash activeadmin;
  attractor = attractor_src;
  attractor-docs = attractor_docs_src;
}
