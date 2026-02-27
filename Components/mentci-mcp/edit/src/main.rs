use ast_grep_language::{LanguageExt, SupportLang};
use clap::{Parser, ValueEnum};
use regex::Regex;
use std::fs;
use std::path::PathBuf;
use std::process::{Command, Stdio};

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Args {
    /// Language of the target file (rust, javascript, typescript, python, nix, bash, capnp)
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

#[derive(Clone)]
enum AppLang {
    Ast(SupportLang),
    Capnp,
    Text,
}

#[derive(Debug, Clone)]
struct CapnpFieldDecl {
    name: String,
    ordinal: String,
    ty: String,
    default_value: Option<String>,
}

fn normalize_ws(input: &str) -> String {
    input.split_whitespace().collect::<Vec<_>>().join(" ")
}

fn parse_capnp_field_decl(line: &str) -> Option<CapnpFieldDecl> {
    let re = Regex::new(
        r"^\s*([A-Za-z_][A-Za-z0-9_]*)\s*@([0-9]+)\s*:\s*([^;=]+?)(?:\s*=\s*([^;]+))?\s*;\s*$",
    )
    .ok()?;
    let caps = re.captures(line)?;

    let name = caps.get(1)?.as_str().to_string();
    let ordinal = caps.get(2)?.as_str().to_string();
    let ty = normalize_ws(caps.get(3)?.as_str());
    let default_value = caps.get(4).map(|m| normalize_ws(m.as_str()));

    Some(CapnpFieldDecl {
        name,
        ordinal,
        ty,
        default_value,
    })
}

fn capnp_field_equals(a: &CapnpFieldDecl, b: &CapnpFieldDecl) -> bool {
    a.name == b.name && a.ordinal == b.ordinal && a.ty == b.ty && a.default_value == b.default_value
}

fn apply_capnp_replace(source: &str, pattern: &str, replace: &str) -> anyhow::Result<String> {
    let pattern_decl = parse_capnp_field_decl(pattern).ok_or_else(|| {
        anyhow::anyhow!(
            "capnp structural_edit currently supports field declarations only (e.g. 'foo @1 :Text;')"
        )
    })?;

    let replacement_trimmed = replace.trim_end();
    if !replacement_trimmed.ends_with(';') {
        return Err(anyhow::anyhow!(
            "capnp replacement must end with ';' for field declarations"
        ));
    }

    let mut replaced = false;
    let mut out_lines = Vec::new();

    for line in source.lines() {
        if !replaced {
            if let Some(line_decl) = parse_capnp_field_decl(line) {
                if capnp_field_equals(&line_decl, &pattern_decl) {
                    let indent = line.chars().take_while(|c| c.is_whitespace()).collect::<String>();
                    let replacement_line = if replace.chars().next().is_some_and(|c| c.is_whitespace()) {
                        replacement_trimmed.to_string()
                    } else {
                        format!("{}{}", indent, replacement_trimmed)
                    };
                    out_lines.push(replacement_line);
                    replaced = true;
                    continue;
                }
            }
        }
        out_lines.push(line.to_string());
    }

    if !replaced {
        return Err(anyhow::anyhow!(
            "No matches found for capnp field pattern. Use exact semantic field declaration (name, ordinal, type, default)."
        ));
    }

    let mut result = out_lines.join("\n");
    if source.ends_with('\n') {
        result.push('\n');
    }
    Ok(result)
}

fn parse_lang(lang: &str) -> Option<AppLang> {
    match lang.to_lowercase().as_str() {
        "rust" | "rs" => Some(AppLang::Ast(SupportLang::Rust)),
        "javascript" | "js" => Some(AppLang::Ast(SupportLang::JavaScript)),
        "typescript" | "ts" => Some(AppLang::Ast(SupportLang::TypeScript)),
        "python" | "py" => Some(AppLang::Ast(SupportLang::Python)),
        "nix" => Some(AppLang::Ast(SupportLang::Nix)),
        "bash" | "sh" => Some(AppLang::Ast(SupportLang::Bash)),
        "json" => Some(AppLang::Ast(SupportLang::Json)),
        "capnp" => Some(AppLang::Capnp),
        "text" | "markdown" | "md" | "yaml" | "yml" => Some(AppLang::Text),
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
        .arg("--side-by-side")
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

fn render_pi_diff(before: &str, after: &str) -> String {
    use similar::ChangeTag;

    fn inline_pair(old_line: &str, new_line: &str) -> (String, String) {
        let old_clean = old_line.strip_suffix('\n').unwrap_or(old_line);
        let new_clean = new_line.strip_suffix('\n').unwrap_or(new_line);

        let char_diff = similar::TextDiff::from_chars(old_clean, new_clean);
        let mut old_out = String::new();
        let mut new_out = String::new();

        for change in char_diff.iter_all_changes() {
            match change.tag() {
                ChangeTag::Equal => {
                    old_out.push_str(change.value());
                    new_out.push_str(change.value());
                }
                ChangeTag::Delete => {
                    old_out.push_str("\x1b[31m");
                    old_out.push_str(change.value());
                    old_out.push_str("\x1b[0m");
                }
                ChangeTag::Insert => {
                    new_out.push_str("\x1b[32m");
                    new_out.push_str(change.value());
                    new_out.push_str("\x1b[0m");
                }
            }
        }

        (old_out, new_out)
    }

    let diff = similar::TextDiff::from_lines(before, after);
    let mut out = String::new();

    for group in diff.grouped_ops(3) {
        for op in group {
            let changes: Vec<_> = diff.iter_changes(&op).collect();
            let mut i = 0;
            while i < changes.len() {
                let current = changes[i];
                if current.tag() == ChangeTag::Delete
                    && i + 1 < changes.len()
                    && changes[i + 1].tag() == ChangeTag::Insert
                {
                    let (old_inline, new_inline) = inline_pair(current.value(), changes[i + 1].value());
                    out.push('-');
                    out.push_str(&old_inline);
                    out.push('\n');
                    out.push('+');
                    out.push_str(&new_inline);
                    out.push('\n');
                    i += 2;
                    continue;
                }

                match current.tag() {
                    ChangeTag::Equal => {
                        out.push(' ');
                        out.push_str(current.value());
                    }
                    ChangeTag::Delete => {
                        out.push('-');
                        out.push_str("\x1b[31m");
                        out.push_str(current.value().strip_suffix('\n').unwrap_or(current.value()));
                        out.push_str("\x1b[0m\n");
                    }
                    ChangeTag::Insert => {
                        out.push('+');
                        out.push_str("\x1b[32m");
                        out.push_str(current.value().strip_suffix('\n').unwrap_or(current.value()));
                        out.push_str("\x1b[0m\n");
                    }
                }
                i += 1;
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

    let new_src = match lang {
        AppLang::Ast(ast_lang) => {
            let ast = ast_lang.ast_grep(&src);
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
            String::from_utf8(bytes)?
        }
        AppLang::Capnp => apply_capnp_replace(&src, &args.pattern, &args.replace)?,
        AppLang::Text => {
            if !src.contains(&args.pattern) {
                println!("No exact match found for text pattern.");
                return Ok(());
            }
            src.replace(&args.pattern, &args.replace)
        }
    };

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
            println!("{}", render_pi_diff(&src, &new_src));
        }
        DiffStyle::Delta => {
            if let Some(delta) = render_delta(&unified) {
                println!("{}", delta);
            } else {
                println!("{}", render_pi_diff(&src, &new_src));
            }
        }
        DiffStyle::Auto => {
            if let Some(delta) = render_delta(&unified) {
                println!("{}", delta);
            } else {
                println!("{}", render_pi_diff(&src, &new_src));
            }
        }
    }

    Ok(())
}
