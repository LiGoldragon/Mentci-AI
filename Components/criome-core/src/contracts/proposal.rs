use serde::{Deserialize, Serialize};

use super::species::{Magnitude, NodeSpecies, UserSpecies};
use super::transaction::ApprovalDecision;

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CrioProposalBatch {
    pub proposals: Vec<cluster::Proposal>,
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

pub mod cluster {
    use serde::{Deserialize, Serialize};

    use super::{node, user};

    #[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
    pub struct Proposal {
        pub cluster: String,
        pub nodes: Vec<node::Proposal>,
        pub users: Vec<user::Proposal>,
    }
}

pub mod node {
    use serde::{Deserialize, Serialize};

    use super::{Magnitude, NodeSpecies};

    #[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
    pub struct Proposal {
        pub name: String,
        pub species: NodeSpecies,
        pub size: Magnitude,
        pub trust: Magnitude,
    }

    #[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
    pub struct State {
        pub name: String,
        pub species: NodeSpecies,
        pub size: Magnitude,
        pub trust: Magnitude,
    }
}

pub mod user {
    use serde::{Deserialize, Serialize};

    use super::{Magnitude, UserSpecies};

    #[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
    pub struct Proposal {
        pub name: String,
        pub species: UserSpecies,
        pub size: Magnitude,
    }

    #[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
    pub struct State {
        pub name: String,
        pub species: UserSpecies,
        pub size: Magnitude,
    }
}
