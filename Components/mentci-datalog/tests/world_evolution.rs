mod world;
use world::{World, Datom, Value};

#[test]
fn test_world_evolution() {
    let mut world = World::new();
    
    // Create facts: Entity 1 is named "Sema"
    let fact1 = Datom {
        entity: 1,
        attribute: "name".to_string(),
        value: Value::Text("Sema".to_string()),
        tx: 0, // will be set by evolve
        added: true,
    };
    
    world.evolve(100, vec![fact1]).expect("Evolution failed");
    
    let perception = world.perceive();
    assert_eq!(perception.len(), 1);
    
    let datom = perception.iter().next().unwrap();
    assert_eq!(datom.tx, 100);
    assert_eq!(datom.entity, 1);
}

#[test]
fn test_fact_retraction() {
    let mut world = World::new();
    
    // Assert fact
    world.evolve(101, vec![Datom {
        entity: 2,
        attribute: "status".to_string(),
        value: Value::Text("active".to_string()),
        tx: 0,
        added: true,
    }]).unwrap();
    
    // Retract fact (Datomic style)
    world.evolve(102, vec![Datom {
        entity: 2,
        attribute: "status".to_string(),
        value: Value::Text("active".to_string()),
        tx: 0,
        added: false,
    }]).unwrap();
    
    let perception = world.perceive();
    assert_eq!(perception.len(), 2); // Both assertion and retraction exist in the World (History)
}

fn main() {
    // Basic runtime check since we're running as a script-like test
    test_world_evolution();
    test_fact_retraction();
    println!("World Ontology Verification: PASSED");
}
