{ inputs, attractor_src, attractor_docs_src }:

{
  inherit (inputs) criomos sema lojix seahawk skrips mkZolaWebsite;
  inherit (inputs) webpublish goldragon maisiliym kibord aski opencode;
  inherit (inputs) comply cxdb activeadmin;
  attractor = attractor_src;
  attractor-docs = attractor_docs_src;
}
