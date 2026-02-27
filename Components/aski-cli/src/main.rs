use anyhow::Result;
use std::io::Read;

fn main() -> Result<()> {
    let mut args = std::env::args().skip(1);
    match args.next().as_deref() {
        Some("edn2capnp") => {
            let mut input = String::new();
            std::io::stdin().read_to_string(&mut input)?;
            let output = aski_lib::projector::edn_to_capnp_txt(&input)?;
            println!("{}", output);
        }
        _ => {
            println!("Usage: aski-cli edn2capnp < input.edn");
        }
    }
    Ok(())
}
