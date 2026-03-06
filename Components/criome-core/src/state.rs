use std::collections::BTreeMap;

use crate::contracts::{CrioNode, CrioTransaction, CrioUser};

#[derive(Debug, Clone, Default)]
pub struct CrioState {
    pub revision: u64,
    pub clusters: BTreeMap<String, CrioClusterState>,
}

#[derive(Debug, Clone, Default)]
pub struct CrioClusterState {
    pub nodes: BTreeMap<String, CrioNode>,
    pub users: BTreeMap<String, CrioUser>,
}

impl CrioState {
    pub fn apply_transaction(&mut self, transaction: CrioTransaction) {
        match transaction {
            CrioTransaction::UpsertCluster { cluster } => {
                self.clusters.entry(cluster).or_default();
                self.revision += 1;
            }
            CrioTransaction::UpsertNode { cluster, node } => {
                let cluster_state = self.clusters.entry(cluster).or_default();
                cluster_state.nodes.insert(node.name.clone(), node);
                self.revision += 1;
            }
            CrioTransaction::UpsertUser { cluster, user } => {
                let cluster_state = self.clusters.entry(cluster).or_default();
                cluster_state.users.insert(user.name.clone(), user);
                self.revision += 1;
            }
        }
    }
}
