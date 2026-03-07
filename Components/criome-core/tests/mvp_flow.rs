use criome_core::contracts::{
    ApproveBatchRequest, CrioNodeProposal, CrioProposal, CrioProposalBatch, CrioUserProposal,
    HorizonRequest, Magnitude, NodeSpecies, UserSpecies,
};
use criome_core::engine::CrioCoreEngine;

#[test]
fn approves_batch_applies_state_and_serves_horizon() {
    let mut engine = CrioCoreEngine::from_memory().expect("engine should initialize");

    let request = ApproveBatchRequest {
        batch: CrioProposalBatch {
            proposals: vec![CrioProposal {
                cluster: "atlas".to_string(),
                nodes: vec![
                    CrioNodeProposal {
                        name: "atlas-center".to_string(),
                        species: NodeSpecies::Center,
                        size: Magnitude::Max,
                        trust: Magnitude::Max,
                    },
                    CrioNodeProposal {
                        name: "atlas-edge".to_string(),
                        species: NodeSpecies::Edge,
                        size: Magnitude::Low,
                        trust: Magnitude::Med,
                    },
                ],
                users: vec![CrioUserProposal {
                    name: "li".to_string(),
                    species: UserSpecies::Code,
                    size: Magnitude::Max,
                }],
            }],
        },
    };

    let approval = engine.approve_batch(request).expect("approval should succeed");
    assert_eq!(approval.decision.rejected.len(), 0);
    assert_eq!(approval.decision.approved.len(), 4);

    let horizon = engine
        .horizon_from_request(HorizonRequest {
            cluster: "atlas".to_string(),
            node: "atlas-center".to_string(),
        })
        .expect("horizon should be returned");

    assert_eq!(horizon.horizon.cluster, "atlas");
    assert_eq!(horizon.horizon.node.name, "atlas-center");
    assert_eq!(horizon.horizon.ex_nodes.len(), 1);
    assert_eq!(horizon.horizon.ex_nodes[0].name, "atlas-edge");
    assert_eq!(horizon.horizon.users.len(), 1);
    assert_eq!(horizon.horizon.users[0].name, "li");
}

#[test]
fn rejects_duplicate_nodes_in_same_cluster_proposal() {
    let mut engine = CrioCoreEngine::from_memory().expect("engine should initialize");

    let request = ApproveBatchRequest {
        batch: CrioProposalBatch {
            proposals: vec![CrioProposal {
                cluster: "atlas".to_string(),
                nodes: vec![
                    CrioNodeProposal {
                        name: "dup".to_string(),
                        species: NodeSpecies::Center,
                        size: Magnitude::Max,
                        trust: Magnitude::Max,
                    },
                    CrioNodeProposal {
                        name: "dup".to_string(),
                        species: NodeSpecies::Edge,
                        size: Magnitude::Low,
                        trust: Magnitude::Low,
                    },
                ],
                users: vec![],
            }],
        },
    };

    let approval = engine.approve_batch(request).expect("approval should succeed");
    assert_eq!(approval.decision.rejected.len(), 1);
    assert!(approval
        .decision
        .rejected
        .iter()
        .any(|r| r.reason.contains("duplicate node")));
}
