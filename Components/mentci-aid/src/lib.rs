pub mod atom_filesystem_capnp {
    include!(concat!(env!("OUT_DIR"), "/atom_filesystem_capnp.rs"));
}

#[allow(unused_parens)]
pub mod mentci_capnp {
    include!(concat!(env!("OUT_DIR"), "/mentci_capnp.rs"));
}

#[allow(unused_parens)]
pub mod mentci_box_capnp {
    include!(concat!(env!("OUT_DIR"), "/mentci_box_capnp.rs"));
}

pub mod actors;
pub mod sandbox;
pub mod dot_loader;
pub mod edn_loader;
pub mod attractor_validator;
pub mod jail_bootstrap;
