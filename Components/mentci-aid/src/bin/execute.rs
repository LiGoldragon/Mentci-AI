use std::collections::BTreeMap;
use std::env;
use std::fs;
use std::path::{Path, PathBuf};
use std::process::{Command, ExitStatus};

struct ExecuteInput {
    args: Vec<String>,
    scripts_root: PathBuf,
    cwd: PathBuf,
    bb_bin: String,
}

struct ExecuteOutput {
    exit_code: i32,
}

struct ScriptCatalogInput {
    scripts_root: PathBuf,
}

struct ScriptCatalog {
    by_name: BTreeMap<String, PathBuf>,
}

struct ScriptCall {
    script_name: String,
    script_args: Vec<String>,
}

struct ScriptRunInput {
    bb_bin: String,
    script_path: PathBuf,
    script_args: Vec<String>,
    cwd: PathBuf,
}

struct ScriptRunOutput {
    exit_code: i32,
}

trait ExecuteFlow {
    fn to_output_from_input(&self, input: ExecuteInput) -> ExecuteOutput;
}

trait ScriptCatalogFlow {
    fn from_input(input: ScriptCatalogInput) -> Result<ScriptCatalog, String>;
    fn to_names(&self) -> Vec<String>;
    fn to_path_from_name(&self, name: &str) -> Option<PathBuf>;
}

trait ScriptCallFlow {
    fn from_args(args: Vec<String>) -> Result<ScriptCall, String>;
}

trait ScriptRunFlow {
    fn to_output_from_input(&self, input: ScriptRunInput) -> Result<ScriptRunOutput, String>;
}

struct ExecuteTool;
struct ScriptRunner;

impl ScriptCatalogFlow for ScriptCatalog {
    fn from_input(input: ScriptCatalogInput) -> Result<ScriptCatalog, String> {
        let mut by_name = BTreeMap::new();
        let entries = fs::read_dir(&input.scripts_root).map_err(|error| {
            format!(
                "failed to list scripts root '{}': {error}",
                input.scripts_root.display()
            )
        })?;
        for entry_result in entries {
            let entry = entry_result.map_err(|error| {
                format!(
                    "failed to read scripts root entry '{}': {error}",
                    input.scripts_root.display()
                )
            })?;
            let path = entry.path();
            if !path.is_dir() {
                continue;
            }
            let name = entry.file_name().to_string_lossy().to_string();
            let main_path = path.join("main.clj");
            if main_path.exists() {
                by_name.insert(name, main_path);
            }
        }
        if by_name.is_empty() {
            return Err(format!(
                "no script entrypoints found under '{}'",
                input.scripts_root.display()
            ));
        }
        Ok(ScriptCatalog { by_name })
    }

    fn to_names(&self) -> Vec<String> {
        self.by_name.keys().cloned().collect()
    }

    fn to_path_from_name(&self, name: &str) -> Option<PathBuf> {
        self.by_name.get(name).cloned()
    }
}

impl ScriptCallFlow for ScriptCall {
    fn from_args(args: Vec<String>) -> Result<ScriptCall, String> {
        let mut iter = args.into_iter();
        let script_name = iter.next().ok_or_else(|| "missing script name".to_string())?;
        let script_args = iter.collect::<Vec<_>>();
        Ok(ScriptCall {
            script_name,
            script_args,
        })
    }
}

impl ScriptRunFlow for ScriptRunner {
    fn to_output_from_input(&self, input: ScriptRunInput) -> Result<ScriptRunOutput, String> {
        let status = Command::new(input.bb_bin)
            .arg(input.script_path)
            .args(input.script_args)
            .current_dir(input.cwd)
            .status()
            .map_err(|error| format!("failed to spawn bb for script: {error}"))?;
        Ok(ScriptRunOutput {
            exit_code: to_exit_code_from_status(status),
        })
    }
}

impl ExecuteFlow for ExecuteTool {
    fn to_output_from_input(&self, input: ExecuteInput) -> ExecuteOutput {
        let catalog_result = ScriptCatalog::from_input(ScriptCatalogInput {
            scripts_root: input.scripts_root.clone(),
        });
        let catalog = match catalog_result {
            Ok(value) => value,
            Err(error) => {
                eprintln!("{error}");
                return ExecuteOutput { exit_code: 2 };
            }
        };

        if input.args.is_empty() || to_help_flag_from_value(&input.args[0]) {
            to_help_output_from_catalog(&input.scripts_root, &catalog);
            return ExecuteOutput { exit_code: 0 };
        }

        if input.args[0] == "list" {
            for name in catalog.to_names() {
                println!("{name}");
            }
            return ExecuteOutput { exit_code: 0 };
        }

        let call = match ScriptCall::from_args(input.args) {
            Ok(value) => value,
            Err(error) => {
                eprintln!("{error}");
                return ExecuteOutput { exit_code: 2 };
            }
        };

        let script_path = match catalog.to_path_from_name(&call.script_name) {
            Some(value) => value,
            None => {
                eprintln!(
                    "unknown script '{}'. run `execute list` to inspect available commands.",
                    call.script_name
                );
                return ExecuteOutput { exit_code: 2 };
            }
        };

        let runner = ScriptRunner;
        match runner.to_output_from_input(ScriptRunInput {
            bb_bin: input.bb_bin,
            script_path,
            script_args: call.script_args,
            cwd: input.cwd,
        }) {
            Ok(output) => ExecuteOutput {
                exit_code: output.exit_code,
            },
            Err(error) => {
                eprintln!("{error}");
                ExecuteOutput { exit_code: 127 }
            }
        }
    }
}

fn to_exit_code_from_status(status: ExitStatus) -> i32 {
    status.code().unwrap_or(1)
}

fn to_help_flag_from_value(value: &str) -> bool {
    value == "--help" || value == "-h" || value == "help"
}

fn to_help_output_from_catalog(scripts_root: &Path, catalog: &ScriptCatalog) {
    println!("execute: unified script tool");
    println!("usage:");
    println!("  execute list");
    println!("  execute <script> [args...]");
    println!("options:");
    println!("  -h, --help                  show help");
    println!("env:");
    println!("  MENTCI_SCRIPTS_DIR          override scripts root");
    println!("  MENTCI_BB_BIN               override babashka executable");
    println!();
    println!("scripts root: {}", scripts_root.display());
    println!("available scripts:");
    for name in catalog.to_names() {
        println!("  {name}");
    }
}

fn main() {
    let args = env::args().skip(1).collect::<Vec<_>>();
    let scripts_root = env::var("MENTCI_SCRIPTS_DIR")
        .map(PathBuf::from)
        .unwrap_or_else(|_| PathBuf::from("Components/scripts"));
    let cwd = env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let bb_bin = env::var("MENTCI_BB_BIN").unwrap_or_else(|_| "bb".to_string());
    let output = ExecuteTool.to_output_from_input(ExecuteInput {
        args,
        scripts_root,
        cwd,
        bb_bin,
    });
    std::process::exit(output.exit_code);
}
