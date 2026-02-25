use ast_grep_language::{LanguageExt, SupportLang};
use clap::{Parser, ValueEnum};
use std::fs;
use std::path::PathBuf;
use std::process::{Command, Stdio};

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Args {
    /// Language of the target file (rust, javascript, typescript, python, nix, bash)
    #[arg(short, long)]
    lang: String,

    /// The pattern to search for
    #[arg(short, long)]
    pattern: String,

    /// The text to replace it with
    #[arg(short, long)]
    replace: String,

    /// Diff output style
    #[arg(long, value_enum, default_value_t = DiffStyle::Auto)]
    diff: DiffStyle,

    /// Path to the file to modify
    file: PathBuf,
}

#[derive(Copy, Clone, Debug, ValueEnum)]
enum DiffStyle {
    Auto,
    Pi,
    Delta,
    Unified,
    None,
}

fn parse_lang(lang: &str) -> Option<SupportLang> {
    match lang.to_lowercase().as_str() {
        "rust" | "rs" => Some(SupportLang::Rust),
        "javascript" | "js" => Some(SupportLang::JavaScript),
        "typescript" | "ts" => Some(SupportLang::TypeScript),
        "python" | "py" => Some(SupportLang::Python),
        "nix" => Some(SupportLang::Nix),
        "bash" | "sh" => Some(SupportLang::Bash),
        _ => None,
    }
}

fn unified_diff(before: &str, after: &str, file: &str) -> String {
    let diff = similar::TextDiff::from_lines(before, after);
    diff.unified_diff()
        .context_radius(3)
        .header(&format!("a/{file}"), &format!("b/{file}"))
        .to_string()
}

fn render_delta(unified: &str) -> Option<String> {
    let mut child = Command::new("delta")
        .arg("--paging=never")
        .arg("--line-numbers")
        .arg("--true-color=always")
        .arg("--24-bit-color=always")
        .arg("--side-by-side=false")
        .arg("--keep-plus-minus-markers")
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .spawn()
        .ok()?;

    if let Some(mut stdin) = child.stdin.take() {
        use std::io::Write;
        if stdin.write_all(unified.as_bytes()).is_err() {
            return None;
        }
    }

    let output = child.wait_with_output().ok()?;
    if !output.status.success() {
        return None;
    }
    Some(String::from_utf8_lossy(&output.stdout).to_string())
}

fn render_pi_diff(before: &str, after: &str, file: &str) -> String {
    use similar::ChangeTag;

    let diff = similar::TextDiff::from_lines(before, after);
    let mut out = String::new();
    out.push_str(&format!("--- a/{file}\n+++ b/{file}\n"));

    for group in diff.grouped_ops(3) {
        for op in group {
            for change in diff.iter_changes(&op) {
                match change.tag() {
                    ChangeTag::Equal => {
                        out.push(' ');
                        out.push_str(change.value());
                    }
                    ChangeTag::Delete => {
                        out.push_str("\x1b[31m-");
                        out.push_str(change.value());
                        out.push_str("\x1b[0m");
                    }
                    ChangeTag::Insert => {
                        out.push_str("\x1b[32m+");
                        out.push_str(change.value());
                        out.push_str("\x1b[0m");
                    }
                }
            }
        }
    }

    out
}

fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    let lang = parse_lang(&args.lang)
        .ok_or_else(|| anyhow::anyhow!("Unsupported language: {}", args.lang))?;
    let src = fs::read_to_string(&args.file)?;

    let ast = lang.ast_grep(&src);
    let root = ast.root();

    let mut edits = root.replace_all(&*args.pattern, &*args.replace);

    if edits.is_empty() {
        println!("No matches found for pattern.");
        return Ok(());
    }

    edits.sort_by(|a, b| b.position.cmp(&a.position));

    let mut bytes = src.clone().into_bytes();
    for edit in edits {
        let pos = edit.position;
        let len = edit.deleted_length;
        bytes.splice(pos..pos + len, edit.inserted_text);
    }

    let new_src = String::from_utf8(bytes)?;
    fs::write(&args.file, &new_src)?;

    let unified = unified_diff(&src, &new_src, &args.file.display().to_string());
    match args.diff {
        DiffStyle::None => {
            println!("Successfully applied edits to {}", args.file.display());
        }
        DiffStyle::Unified => {
            println!("{}", unified);
        }
        DiffStyle::Pi => {
            println!("{}", render_pi_diff(&src, &new_src, &args.file.display().to_string()));
        }
        DiffStyle::Delta => {
            if let Some(delta) = render_delta(&unified) {
                println!("{}", delta);
            } else {
                println!("{}", render_pi_diff(&src, &new_src, &args.file.display().to_string()));
            }
        }
        DiffStyle::Auto => {
            if let Some(delta) = render_delta(&unified) {
                println!("{}", delta);
            } else {
                println!("{}", render_pi_diff(&src, &new_src, &args.file.display().to_string()));
            }
        }
    }

    Ok(())
}
