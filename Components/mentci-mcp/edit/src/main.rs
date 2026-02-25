use ast_grep_language::{LanguageExt, SupportLang};
use clap::Parser;
use std::fs;
use std::path::PathBuf;

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

    /// Path to the file to modify
    file: PathBuf,
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

fn main() -> anyhow::Result<()> {
    let args = Args::parse();
    
    let lang = parse_lang(&args.lang).ok_or_else(|| anyhow::anyhow!("Unsupported language: {}", args.lang))?;
    let src = fs::read_to_string(&args.file)?;
    
    let ast = lang.ast_grep(&src);
    let root = ast.root();
    
    let mut edits = root.replace_all(&*args.pattern, &*args.replace);
    
    if edits.is_empty() {
        println!("No matches found for pattern.");
        return Ok(());
    }
    
    edits.sort_by(|a, b| b.position.cmp(&a.position));
    
    let mut bytes = src.into_bytes();
    for edit in edits {
        let pos = edit.position;
        let len = edit.deleted_length;
        bytes.splice(pos..pos+len, edit.inserted_text);
    }
    
    let new_src = String::from_utf8(bytes)?;
    fs::write(&args.file, new_src)?;
    
    println!("Successfully applied edits to {}", args.file.display());
    
    Ok(())
}
