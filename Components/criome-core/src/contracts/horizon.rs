use serde::{Deserialize, Serialize};

use super::{CrioNode, CrioUser};

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct HorizonRequest {
    pub cluster: String,
    pub node: String,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct HorizonResponse {
    pub revision: u64,
    pub horizon: CrioHorizon,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioHorizon {
    pub cluster: String,
    pub node: CrioNode,
    pub ex_nodes: Vec<CrioNode>,
    pub users: Vec<CrioUser>,
}
