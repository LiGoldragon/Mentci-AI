use anyhow::{Context, Result};
use edn_rs::Edn;
use std::collections::BTreeMap;
use std::path::{Path, PathBuf};
use serde::{Serialize, Deserialize};
use std::str::FromStr;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FsIndex {
    pub kind: String,
    pub title: String,
    pub entries: Vec<String>,
}

pub struct IndexReader;

impl IndexReader {
    pub fn from_file(path: &Path) -> Result<FsIndex> {
        let content = std::fs::read_to_string(path)
            .with_context(|| format!("failed to read index file: {}", path.display()))?;
        Self::from_str(&content)
    }

    pub fn from_str(content: &str) -> Result<FsIndex> {
        let edn = Edn::from_str(content)
            .map_err(|e| anyhow::anyhow!("failed to parse EDN: {}", e))?;
        
        let map = match edn {
            Edn::Map(m) => m.to_map(),
            _ => anyhow::bail!("index.edn must be a map"),
        };

        let kind = get_keyword_value(&map, "kind")?;
        let title = get_string_value(&map, "title")?;
        let entries = get_vector_values(&map, "entries")?;

        Ok(FsIndex {
            kind,
            title,
            entries,
        })
    }
}

fn get_keyword_value(map: &BTreeMap<String, Edn>, key: &str) -> Result<String> {
    let key_with_colon = format!(":{}", key);
    let val = map.get(&key_with_colon).context(format!("missing :{} key", key))?;
    match val {
        Edn::Key(k) => Ok(k.clone().trim_matches(':').to_string()),
        _ => anyhow::bail!(":{} must be a keyword", key),
    }
}

fn get_string_value(map: &BTreeMap<String, Edn>, key: &str) -> Result<String> {
    let key_with_colon = format!(":{}", key);
    let val = map.get(&key_with_colon).context(format!("missing :{} key", key))?;
    match val {
        Edn::Str(s) => Ok(s.clone().trim_matches('"').to_string()),
        _ => anyhow::bail!(":{} must be a string", key),
    }
}

fn get_vector_values(map: &BTreeMap<String, Edn>, key: &str) -> Result<Vec<String>> {
    let key_with_colon = format!(":{}", key);
    let val = map.get(&key_with_colon).context(format!("missing :{} key", key))?;
    match val {
        Edn::Vector(v) => {
            v.clone().to_vec().iter()
                .map(|e| match e {
                    Edn::Str(s) => Ok(s.clone().trim_matches('"').to_string()),
                    _ => anyhow::bail!("entries must be strings"),
                })
                .collect()
        }
        _ => anyhow::bail!(":{} must be a vector", key),
    }
}

pub struct FsScanner;

impl FsScanner {
    pub fn scan_dir(dir: &Path) -> Result<Vec<PathBuf>> {
        let index_path = dir.join("index.edn");
        if !index_path.exists() {
            return Ok(Vec::new());
        }

        let index = IndexReader::from_file(&index_path)?;
        let mut resolved_paths = Vec::new();

        for entry in index.entries {
            let path = dir.join(&entry);
            resolved_paths.push(path);
        }

        Ok(resolved_paths)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::fs::File;
    use std::io::Write;
    use tempfile::tempdir;

    #[test]
    fn test_relative_path_resolution() -> Result<()> {
        let dir = tempdir()?;
        let index_path = dir.path().join("index.edn");
        let mut file = File::create(&index_path)?;
        writeln!(file, "{{:kind :directory-index :title \"Test\" :entries [\"a.txt\" \"b/\"]}}")?;

        let paths = FsScanner::scan_dir(dir.path())?;
        assert_eq!(paths.len(), 2);
        assert!(paths.contains(&dir.path().join("a.txt")));
        assert!(paths.contains(&dir.path().join("b/")));

        Ok(())
    }
}
