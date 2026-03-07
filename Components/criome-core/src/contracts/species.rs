use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
#[serde(rename_all = "snake_case")]
pub enum Magnitude {
    Min,
    Low,
    Med,
    Max,
}

impl Magnitude {
    pub fn to_u8(self) -> u8 {
        match self {
            Self::Min => 0,
            Self::Low => 1,
            Self::Med => 2,
            Self::Max => 3,
        }
    }
}

impl NodeSpecies {
    pub fn as_str(self) -> &'static str {
        match self {
            Self::Center => "center",
            Self::Hybrid => "hybrid",
            Self::Edge => "edge",
            Self::EdgeTesting => "edge_testing",
            Self::MediaBroadcast => "media_broadcast",
            Self::Router => "router",
            Self::RouterTesting => "router_testing",
        }
    }
}

impl UserSpecies {
    pub fn as_str(self) -> &'static str {
        match self {
            Self::Code => "code",
            Self::Multimedia => "multimedia",
            Self::Unlimited => "unlimited",
        }
    }
}

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "snake_case")]
pub enum NodeSpecies {
    Center,
    Hybrid,
    Edge,
    EdgeTesting,
    MediaBroadcast,
    Router,
    RouterTesting,
}

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "snake_case")]
pub enum UserSpecies {
    Code,
    Multimedia,
    Unlimited,
}
