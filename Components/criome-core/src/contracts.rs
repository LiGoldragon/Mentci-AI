use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioProposalBatch {
    pub proposals: Vec<CrioProposal>,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioProposal {
    pub cluster: String,
    pub nodes: Vec<CrioNodeProposal>,
    pub users: Vec<CrioUserProposal>,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioNodeProposal {
    pub name: String,
    pub species: String,
    pub size: u8,
    pub trust: u8,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioUserProposal {
    pub name: String,
    pub species: String,
    pub size: u8,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct ApproveBatchRequest {
    pub batch: CrioProposalBatch,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct ApproveBatchResponse {
    pub decision: ApprovalDecision,
    pub state_revision: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct ApprovalDecision {
    pub approved: Vec<CrioTransaction>,
    pub rejected: Vec<RejectedProposal>,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct RejectedProposal {
    pub cluster: String,
    pub target: String,
    pub reason: String,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(tag = "kind", rename_all = "snake_case")]
pub enum CrioTransaction {
    UpsertCluster {
        cluster: String,
    },
    UpsertNode {
        cluster: String,
        node: CrioNode,
    },
    UpsertUser {
        cluster: String,
        user: CrioUser,
    },
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioNode {
    pub name: String,
    pub species: String,
    pub size: u8,
    pub trust: u8,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioUser {
    pub name: String,
    pub species: String,
    pub size: u8,
}

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
