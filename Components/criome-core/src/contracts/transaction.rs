use serde::{Deserialize, Serialize};

use super::{CrioNode, CrioUser};

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
