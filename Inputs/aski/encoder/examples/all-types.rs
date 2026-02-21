// Serde-derivable schema surface intended for an Aski type-declaration DSL.
//
// The editor/runtime goal is “lossless text + structural edits + incremental semantics”:
// - Text is authoritative bytes (rope substrate).
// - Syntax is a lossless CST with spans/trivia (rowan substrate).
// - Semantics is cached, invalidation-driven queries (salsa substrate).
// - A transaction engine applies typed, node-path edits and emits minimal byte-range patches.
//
// This Rust module exists only to define the Serde-compatible *type universe* that the Aski
// schema DSL must be able to declare 1-to-1, including Rust distinctions (newtypes, tuple
// structs, fixed arrays, and enum variant forms).

use serde::{Deserialize, Serialize};
use std::collections::{BTreeMap, BTreeSet};
use uuid::Uuid;

// Newtype: distinct identity type, not merely a UUID.
#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Hash, Serialize, Deserialize)]
pub struct UserId(pub Uuid);

// Newtype: bytes payload, distinct from Vec<u8>.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Blob(pub Vec<u8>);

// Generic newtype wrapper: preserves type identity across tooling layers.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Wrapped<T>(pub T);

// Unit struct: schema-level marker type.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct UnitStruct;

// Tuple struct: positional fields with explicit arity.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Pair(pub i32, pub i32);

// Error enum: unit variants and a payload-carrying variant.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum ErrorCode {
    NotFound,
    PermissionDenied,
    Invalid(String),
}

// Enum encoding discipline: schema chooses an explicit envelope so formats never guess.
// Aski’s type DSL should treat this as a single canonical enum representation.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(tag = "variant", content = "data")]
pub enum Shape {
    Unit,
    Circle { r: f64 },
    Rect(f64, f64),
    Named(String),
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(tag = "variant", content = "data")]
pub enum Message {
    Ping,
    Text(String),
    Batch(Vec<Message>),
    Kv(BTreeMap<String, String>),
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Status {
    pub ok: bool,
    pub code: Option<u32>,
    pub note: Option<String>,
}

// Coverage specimen: one record that forces the schema DSL to express every Serde shape
// plus Rust-specific distinctions (width/signedness, tuples, arrays, sets, maps, newtypes,
// enums, recursion, and unit).
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct AllTypes {
    // scalars
    pub bool_value: bool,
    pub char_value: char,
    pub string_value: String,

    // signed integers
    pub i8_value: i8,
    pub i16_value: i16,
    pub i32_value: i32,
    pub i64_value: i64,
    pub i128_value: i128,
    pub isize_value: isize,

    // unsigned integers
    pub u8_value: u8,
    pub u16_value: u16,
    pub u32_value: u32,
    pub u64_value: u64,
    pub u128_value: u128,
    pub usize_value: usize,

    // floats
    pub f32_value: f32,
    pub f64_value: f64,

    // option/result
    pub maybe_i64_value: Option<i64>,
    pub outcome_string_or_error_code: Result<String, ErrorCode>,

    // sequences / tuples / fixed arrays
    pub string_vector: Vec<String>,
    pub mixed_tuple: (i32, String, bool),
    pub u16_array_len_3: [u16; 3],

    // sets / maps (including non-string key)
    pub string_set: BTreeSet<String>,
    pub string_to_u32_map: BTreeMap<String, u32>,
    pub user_id_to_i64_map: BTreeMap<UserId, i64>,

    // newtypes
    pub user_id: UserId,
    pub blob_bytes: Blob,
    pub wrapped_pair: Wrapped<Pair>,

    // enums / recursion / nested structs
    pub shape: Shape,
    pub message: Message,
    pub status: Status,

    // unit values
    pub unit_value: (),
    pub unit_struct_value: UnitStruct,
}
