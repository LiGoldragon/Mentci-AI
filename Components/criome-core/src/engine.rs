use std::collections::BTreeSet;

use cozo::{DbInstance, ScriptMutability};
use thiserror::Error;

use crate::contracts::{
    ApprovalDecision, ApproveBatchRequest, ApproveBatchResponse, CrioHorizon, CrioNode,
    CrioProposal, CrioTransaction, HorizonRequest, HorizonResponse, RejectedProposal,
};
use crate::state::CrioState;

#[derive(Debug, Error)]
pub enum CrioCoreError {
    #[error("cozo error: {0}")]
    Cozo(String),
    #[error("cluster not found: {0}")]
    ClusterNotFound(String),
    #[error("node not found: {cluster}/{node}")]
    NodeNotFound { cluster: String, node: String },
}

pub struct CrioCoreEngine {
    state: CrioState,
    cozo: CrioCozoStore,
}

impl CrioCoreEngine {
    pub fn from_memory() -> Result<Self, CrioCoreError> {
        let cozo = CrioCozoStore::from_memory()?;
        Ok(Self {
            state: CrioState::default(),
            cozo,
        })
    }

    pub fn approve_batch(
        &mut self,
        input: ApproveBatchRequest,
    ) -> Result<ApproveBatchResponse, CrioCoreError> {
        let decision = self.decision_from_proposals(input.batch.proposals);
        for tx in decision.approved.iter().cloned() {
            self.cozo.apply_transaction(tx.clone())?;
            self.state.apply_transaction(tx);
        }
        Ok(ApproveBatchResponse {
            decision,
            state_revision: self.state.revision,
        })
    }

    pub fn horizon_from_request(
        &self,
        input: HorizonRequest,
    ) -> Result<HorizonResponse, CrioCoreError> {
        let cluster_state = self
            .state
            .clusters
            .get(&input.cluster)
            .ok_or_else(|| CrioCoreError::ClusterNotFound(input.cluster.clone()))?;
        let focus_node = cluster_state
            .nodes
            .get(&input.node)
            .cloned()
            .ok_or_else(|| CrioCoreError::NodeNotFound {
                cluster: input.cluster.clone(),
                node: input.node.clone(),
            })?;

        let ex_nodes = cluster_state
            .nodes
            .values()
            .filter(|n| n.name != input.node)
            .cloned()
            .collect::<Vec<CrioNode>>();

        let users = cluster_state.users.values().cloned().collect();

        Ok(HorizonResponse {
            revision: self.state.revision,
            horizon: CrioHorizon {
                cluster: input.cluster,
                node: focus_node,
                ex_nodes,
                users,
            },
        })
    }

    fn decision_from_proposals(&self, proposals: Vec<CrioProposal>) -> ApprovalDecision {
        let mut approved = Vec::new();
        let mut rejected = Vec::new();

        for proposal in proposals {
            approved.push(CrioTransaction::UpsertCluster {
                cluster: proposal.cluster.clone(),
            });

            let mut seen_nodes: BTreeSet<String> = BTreeSet::new();
            for node in proposal.nodes {
                if !seen_nodes.insert(node.name.clone()) {
                    rejected.push(RejectedProposal {
                        cluster: proposal.cluster.clone(),
                        target: node.name,
                        reason: "duplicate node in proposal".to_string(),
                    });
                    continue;
                }
                approved.push(CrioTransaction::UpsertNode {
                    cluster: proposal.cluster.clone(),
                    node: crate::contracts::CrioNode {
                        name: node.name,
                        species: node.species,
                        size: node.size,
                        trust: node.trust,
                    },
                });
            }

            let mut seen_users: BTreeSet<String> = BTreeSet::new();
            for user in proposal.users {
                if !seen_users.insert(user.name.clone()) {
                    rejected.push(RejectedProposal {
                        cluster: proposal.cluster.clone(),
                        target: user.name,
                        reason: "duplicate user in proposal".to_string(),
                    });
                    continue;
                }
                approved.push(CrioTransaction::UpsertUser {
                    cluster: proposal.cluster.clone(),
                    user: crate::contracts::CrioUser {
                        name: user.name,
                        species: user.species,
                        size: user.size,
                    },
                });
            }
        }

        ApprovalDecision { approved, rejected }
    }
}

struct CrioCozoStore {
    db: DbInstance,
}

impl CrioCozoStore {
    fn from_memory() -> Result<Self, CrioCoreError> {
        let db = DbInstance::new("mem", "", Default::default())
            .map_err(|e| CrioCoreError::Cozo(e.to_string()))?;
        let mut store = Self { db };
        store.ensure_schema()?;
        Ok(store)
    }

    fn ensure_schema(&mut self) -> Result<(), CrioCoreError> {
        self.run(
            ":create cluster {name: String}",
            ScriptMutability::Mutable,
        )
        .map(|_| ())?;
        self.run(
            ":create node {cluster: String, name: String => species: String, size: Int, trust: Int}",
            ScriptMutability::Mutable,
        )
        .map(|_| ())?;
        self.run(
            ":create user {cluster: String, name: String => species: String, size: Int}",
            ScriptMutability::Mutable,
        )
        .map(|_| ())
    }

    fn apply_transaction(&mut self, transaction: CrioTransaction) -> Result<(), CrioCoreError> {
        match transaction {
            CrioTransaction::UpsertCluster { cluster } => {
                let script = format!(
                    "? [name] <- [[\"{}\"]]\n:put cluster {{name}}",
                    escape(&cluster)
                );
                self.run(&script, ScriptMutability::Mutable).map(|_| ())
            }
            CrioTransaction::UpsertNode { cluster, node } => {
                let script = format!(
                    "? [cluster, name, species, size, trust] <- [[\"{}\", \"{}\", \"{}\", {}, {}]]\n:put node {{cluster, name => species, size, trust}}",
                    escape(&cluster),
                    escape(&node.name),
                    escape(node.species.as_str()),
                    node.size.to_u8(),
                    node.trust.to_u8()
                );
                self.run(&script, ScriptMutability::Mutable).map(|_| ())
            }
            CrioTransaction::UpsertUser { cluster, user } => {
                let script = format!(
                    "? [cluster, name, species, size] <- [[\"{}\", \"{}\", \"{}\", {}]]\n:put user {{cluster, name => species, size}}",
                    escape(&cluster),
                    escape(&user.name),
                    escape(user.species.as_str()),
                    user.size.to_u8()
                );
                self.run(&script, ScriptMutability::Mutable).map(|_| ())
            }
        }
    }

    fn run(
        &mut self,
        script: &str,
        mutability: ScriptMutability,
    ) -> Result<cozo::NamedRows, CrioCoreError> {
        self.db
            .run_script(script, Default::default(), mutability)
            .map_err(|e| CrioCoreError::Cozo(e.to_string()))
    }
}

fn escape(value: &str) -> String {
    value.replace('"', "\\\"")
}
