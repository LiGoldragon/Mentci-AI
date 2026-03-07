#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ChronosDefaults;

impl ChronosDefaults {
    pub fn format() -> &'static str {
        "am"
    }

    pub fn precision() -> &'static str {
        "second"
    }

    pub fn default_args() -> [&'static str; 4] {
        ["--format", Self::format(), "--precision", Self::precision()]
    }
}

pub fn apply_defaults(input: &[String]) -> Vec<String> {
    let has_format = has_flag(input, "--format");
    let has_precision = has_flag(input, "--precision");

    let mut output = Vec::new();

    if !has_format {
        output.push("--format".to_string());
        output.push(ChronosDefaults::format().to_string());
    }

    if !has_precision {
        output.push("--precision".to_string());
        output.push(ChronosDefaults::precision().to_string());
    }

    output.extend(input.iter().cloned());
    output
}

fn has_flag(input: &[String], name: &str) -> bool {
    input.iter().any(|value| value == name)
}

#[cfg(test)]
mod tests {
    use super::{apply_defaults, ChronosDefaults};

    #[test]
    fn default_args_match_am_second_profile() {
        assert_eq!(
            ChronosDefaults::default_args(),
            ["--format", "am", "--precision", "second"]
        );
    }

    #[test]
    fn applies_missing_defaults() {
        let args = vec!["--unix".to_string(), "0".to_string()];
        let merged = apply_defaults(&args);
        assert_eq!(
            merged,
            vec![
                "--format".to_string(),
                "am".to_string(),
                "--precision".to_string(),
                "second".to_string(),
                "--unix".to_string(),
                "0".to_string(),
            ]
        );
    }

    #[test]
    fn keeps_user_format_and_precision() {
        let args = vec![
            "--format".to_string(),
            "json".to_string(),
            "--precision".to_string(),
            "minute".to_string(),
        ];
        let merged = apply_defaults(&args);
        assert_eq!(merged, args);
    }
}
