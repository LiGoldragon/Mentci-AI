// Sema-grade Datalog World Ontology
// This component provides the ontological substrate for Mentci Actors.

#[allow(unused_parens, dead_code, unused_imports, non_snake_case, unused_qualifications)]
pub mod datalog_eav_capnp {
    // include!(concat!(env!("OUT_DIR"), "/datalog_eav_capnp.rs"));
}

use std::collections::BTreeSet;

/// Datom is the basic unit of reality in the EAV model.
#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord)]
pub struct Datom {
    pub entity: u64,
    pub attribute: String,
    pub value: Value,
    pub tx: u64,
    pub added: bool,
}

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord)]
pub enum Value {
    Text(String),
    Int(i64),
    Uint(u64),
    Float(u64),
    Bool(bool),
    Blob(Vec<u8>),
}

/// The World is the ontological substrate containing all facts (Datoms).
pub struct World {
    index_eavt: BTreeSet<Datom>,
    index_aevt: BTreeSet<Datom>,
    index_avet: BTreeSet<Datom>,
    index_vaet: BTreeSet<Datom>,
}

impl World {
    pub fn new() -> Self {
        Self {
            index_eavt: BTreeSet::new(),
            index_aevt: BTreeSet::new(),
            index_avet: BTreeSet::new(),
            index_vaet: BTreeSet::new(),
        }
    }

    pub fn evolve(&mut self, tx_id: u64, datoms: Vec<Datom>) -> Result<(), String> {
        for mut datom in datoms {
            datom.tx = tx_id;
            self.index_eavt.insert(datom.clone());
            self.index_aevt.insert(datom.clone());
            self.index_avet.insert(datom.clone());
            self.index_vaet.insert(datom.clone());
        }
        Ok(())
    }

    pub fn perceive(&self) -> &BTreeSet<Datom> {
        &self.index_eavt
    }
}
