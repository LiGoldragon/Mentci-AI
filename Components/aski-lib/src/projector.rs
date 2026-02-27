use anyhow::Result;
use edn_rs::edn::Edn;
use std::str::FromStr;

/// Converts EDN string into Cap'n Proto text format.
pub fn edn_to_capnp_txt(edn_str: &str) -> Result<String> {
    let parsed = Edn::from_str(edn_str).map_err(|e| anyhow::anyhow!("Failed to parse EDN: {}", e))?;
    Ok(edn_val_to_capnp(&parsed, 0))
}

fn edn_val_to_capnp(edn: &Edn, indent: usize) -> String {
    let ind = "  ".repeat(indent);
    match edn {
        Edn::Map(_) => {
            let mut fields = Vec::new();
            if let Some(iter) = edn.map_iter() {
                for (k, v) in iter {
                    let key_name = k.trim_start_matches(':');
                    fields.push(format!("{}{} = {}", "  ".repeat(indent + 1), key_name, edn_val_to_capnp(v, indent + 1)));
                }
            }
            if fields.is_empty() {
                "()".to_string()
            } else {
                format!("(\n{}\n{})", fields.join(",\n"), ind)
            }
        }
        Edn::Vector(_) | Edn::List(_) => {
            let mut items = Vec::new();
            if let Some(iter) = edn.iter_some() {
                for item in iter {
                    items.push(edn_val_to_capnp(item, indent + 1));
                }
            }
            if items.is_empty() {
                "[]".to_string()
            } else {
                format!("[\n{}{}\n{}]", "  ".repeat(indent + 1), items.join(format!(",\n{}", "  ".repeat(indent + 1)).as_str()), ind)
            }
        }
        Edn::Str(s) => format!("\"{}\"", s.replace('\"', "\\\"")),
        Edn::Int(i) => i.to_string(),
        Edn::UInt(u) => u.to_string(),
        Edn::Double(d) => d.to_string(),
        Edn::Bool(b) => if *b { "true".to_string() } else { "false".to_string() },
        Edn::Key(k) => format!("{}", k.trim_start_matches(':')), // Emit unquoted for capnp enums
        Edn::Symbol(s) => s.to_string(),
        Edn::Char(c) => format!("'{}'", c),
        Edn::Rational(r) => r.to_string(),
        Edn::Nil | Edn::Empty => "void".to_string(),
        Edn::Tagged(_, inner) => edn_val_to_capnp(inner, indent),
        #[allow(unreachable_patterns)]
        _ => "void".to_string(),
    }
}
