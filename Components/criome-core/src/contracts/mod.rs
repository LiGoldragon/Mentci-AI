pub mod horizon;
pub mod proposal;
pub mod species;
pub mod transaction;

pub use horizon::{CrioHorizon, HorizonRequest, HorizonResponse};
pub use proposal::{ApproveBatchRequest, ApproveBatchResponse, CrioProposalBatch};
pub use proposal::{cluster, node, user};
pub use species::{Magnitude, NodeSpecies, UserSpecies};
pub use transaction::{ApprovalDecision, CrioTransaction, RejectedProposal};

pub type CrioProposal = cluster::Proposal;
pub type CrioNodeProposal = node::Proposal;
pub type CrioUserProposal = user::Proposal;
pub type CrioNode = node::State;
pub type CrioUser = user::State;
