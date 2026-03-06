use std::env;
use std::fs;

use criome_core::contracts::{ApproveBatchRequest, HorizonRequest};
use criome_core::engine::CrioCoreEngine;

fn main() {
    if let Err(error) = run() {
        eprintln!("criad error: {error}");
        std::process::exit(1);
    }
}

fn run() -> Result<(), String> {
    let args = env::args().collect::<Vec<String>>();
    if args.len() < 2 {
        return Err("usage: criad <approve|horizon> ...".to_string());
    }

    match args[1].as_str() {
        "approve" => run_approve(args),
        "horizon" => run_horizon(args),
        _ => Err("usage: criad <approve|horizon> ...".to_string()),
    }
}

fn run_approve(args: Vec<String>) -> Result<(), String> {
    if args.len() != 3 {
        return Err("usage: criad approve <proposal_batch.json>".to_string());
    }
    let payload = fs::read_to_string(&args[2]).map_err(|e| e.to_string())?;
    let request: ApproveBatchRequest = serde_json::from_str(&payload).map_err(|e| e.to_string())?;

    let mut engine = CrioCoreEngine::from_memory().map_err(|e| e.to_string())?;
    let response = engine.approve_batch(request).map_err(|e| e.to_string())?;

    println!(
        "{}",
        serde_json::to_string_pretty(&response).map_err(|e| e.to_string())?
    );
    Ok(())
}

fn run_horizon(args: Vec<String>) -> Result<(), String> {
    if args.len() != 4 {
        return Err("usage: criad horizon <cluster> <node>".to_string());
    }
    let engine = CrioCoreEngine::from_memory().map_err(|e| e.to_string())?;
    // MVP note: horizon currently requires state loaded in this process,
    // so this command is only useful after wiring persistence/daemon loop.
    let response = engine
        .horizon_from_request(HorizonRequest {
            cluster: args[2].clone(),
            node: args[3].clone(),
        })
        .map_err(|e| e.to_string())?;

    println!(
        "{}",
        serde_json::to_string_pretty(&response).map_err(|e| e.to_string())?
    );
    Ok(())
}
