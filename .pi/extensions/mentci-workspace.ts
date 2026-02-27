import type { ExtensionAPI, Theme } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { execSync } from "child_process";
import * as fs from "fs";
import * as path from "path";
import { Text } from "@mariozechner/pi-tui";

function highlightInlinePair(oldLine: string, newLine: string, theme: Theme): [string, string] {
  const oldBody = oldLine.slice(1);
  const newBody = newLine.slice(1);

  let prefix = 0;
  while (prefix < oldBody.length && prefix < newBody.length && oldBody[prefix] === newBody[prefix]) {
    prefix++;
  }

  let oldSuffix = oldBody.length;
  let newSuffix = newBody.length;
  while (oldSuffix > prefix && newSuffix > prefix && oldBody[oldSuffix - 1] === newBody[newSuffix - 1]) {
    oldSuffix--;
    newSuffix--;
  }

  const oldPrefix = oldBody.slice(0, prefix);
  const oldChanged = oldBody.slice(prefix, oldSuffix);
  const oldTail = oldBody.slice(oldSuffix);

  const newPrefix = newBody.slice(0, prefix);
  const newChanged = newBody.slice(prefix, newSuffix);
  const newTail = newBody.slice(newSuffix);

  const oldRendered =
    theme.fg("error", "-") +
    oldPrefix +
    (oldChanged.length > 0 ? theme.fg("error", theme.bold(oldChanged)) : "") +
    oldTail;

  const newRendered =
    theme.fg("success", "+") +
    newPrefix +
    (newChanged.length > 0 ? theme.fg("success", theme.bold(newChanged)) : "") +
    newTail;

  return [oldRendered, newRendered];
}

function cleanUnifiedDiffText(raw: string): string {
  const lines = raw.split(/\r?\n/);
  const out: string[] = [];

  for (const line of lines) {
    if (line.startsWith("--- ") || line.startsWith("+++ ") || line.startsWith("@@")) continue;
    if (line.trim().length === 0) continue;
    if (line === "No matches found for pattern.") {
      out.push(line);
      continue;
    }
    out.push(line);
  }

  return out.join("\n");
}

function renderUnifiedDiffForPi(raw: string, theme: Theme): string {
  const lines = raw.split(/\r?\n/);
  const out: string[] = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    // Drop noisy file/hunk headers; path already appears in tool call row.
    if (line.startsWith("--- ") || line.startsWith("+++ ") || line.startsWith("@@")) {
      continue;
    }

    if (line.startsWith("-") && !line.startsWith("---") && i + 1 < lines.length) {
      const next = lines[i + 1];
      if (next.startsWith("+") && !next.startsWith("+++")) {
        const [oldRendered, newRendered] = highlightInlinePair(line, next, theme);
        out.push(oldRendered);
        out.push(newRendered);
        i += 1;
        continue;
      }
    }

    if (line.startsWith("-") && !line.startsWith("---")) {
      out.push(theme.fg("error", line));
      continue;
    }

    if (line.startsWith("+") && !line.startsWith("+++")) {
      out.push(theme.fg("success", line));
      continue;
    }

    if (line.startsWith(" ")) {
      out.push(theme.fg("muted", line.slice(1)));
      continue;
    }

    if (line.trim().length > 0) {
      out.push(line);
    }
  }

  return out.join("\n");
}

function loadSemaProgrammerSkill(workspaceRoot: string): string | null {
  const skillDir = path.join(workspaceRoot, ".pi", "skills", "sema-programmer");
  const compiledPath = path.join(skillDir, "absolute_rules.md");
  const skillPath = path.join(skillDir, "SKILL.md");

  try {
    if (fs.existsSync(compiledPath)) {
      return fs.readFileSync(compiledPath, "utf-8").trim();
    }
    if (fs.existsSync(skillPath)) {
      return fs.readFileSync(skillPath, "utf-8").trim();
    }
    return null;
  } catch {
    return null;
  }
}

export default function (pi: ExtensionAPI) {
  pi.registerTool({
    name: "structural_edit",
    label: "mentci-mcp-edit",
    description: [
      "Make surgical structural edits to code files using AST pattern matching instead of brittle Regex.",
      "Uses ast-grep underneath. The replacement will exactly preserve surrounding AST.",
      "Supported languages: rust, js, ts, python, nix, bash, capnp (field-declaration structural mode).",
    ].join(" "),
    parameters: Type.Object({
      file: Type.String({ description: "Relative path to the file to modify" }),
      lang: Type.String({ description: "Language of the file (e.g., rust, ts, python, nix)" }),
      pattern: Type.String({ description: "The AST pattern to search for (e.g. 'fn $A() { $$$B }')" }),
      replace: Type.String({ description: "The AST replacement string (e.g. 'fn renamed() { $$$B }')" }),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const workspaceRoot = process.cwd();
        const bin = path.join(workspaceRoot, "target", "release", "mentci-mcp-edit");
        const cmd = `"${bin}" --lang "${args.lang}" --pattern "${args.pattern.replace(/"/g, '\\"')}" --replace "${args.replace.replace(/"/g, '\\"')}" --diff pi "${args.file}"`;
        const output = execSync(cmd, { cwd: workspaceRoot, encoding: "utf-8" }).trim();

        try {
          execSync(`jj commit -m "intent: structurally edit ${args.file}"`, {
            cwd: workspaceRoot,
            stdio: "ignore",
          });
          console.log(`Auto-committed structural edit to ${args.file}`);
        } catch (e) {
          console.error(`Failed to auto-commit: ${e}`);
        }

        return {
          content: [{ type: "text", text: output || "No output." }],
        };
      } catch (e: any) {
        return {
          content: [{ type: "text", text: `Error executing structural_edit: ${e.message}\n${e.stdout}\n${e.stderr}` }],
        };
      }
    },

    renderCall(args, theme) {
      const shortFile = typeof args.file === "string" ? args.file.split("/").slice(-2).join("/") : "";
      const text =
        theme.fg("toolTitle", theme.bold("structural_edit ")) +
        theme.fg("accent", shortFile) +
        theme.fg("muted", ` [${args.lang}]`);
      return new Text(text, 0, 0);
    },

    renderResult(result, _options, theme) {
      const first = result.content?.[0];
      const raw = first?.type === "text" ? first.text : "";
      return new Text(renderUnifiedDiffForPi(raw, theme), 0, 0);
    },
  });

  pi.on("session_start", async () => {
    try {
      const workspaceRoot = process.cwd();
      const chronos = execSync(`chronos --format am --precision second`, {
        cwd: workspaceRoot,
        encoding: "utf-8",
      }).trim();
      const jjStatus = execSync(`jj status --no-pager`, {
        cwd: workspaceRoot,
        encoding: "utf-8",
      }).trim();
      console.log(`\n--- Pre-Hook Injected State ---\nTime: ${chronos}\n${jjStatus}\n-------------------------------`);
    } catch (_e) {}
  });

  pi.on("before_agent_start", async () => {
    // We inject every turn to ensure the latest Absolute Rules bypass /compact decay.
    // The policy engine compiler ensures this text is dense and token-efficient.
    const workspaceRoot = process.cwd();
    const skillText = loadSemaProgrammerSkill(workspaceRoot);
    if (!skillText) return undefined;

    return {
      message: {
        customType: "sema-programmer-injection",
        display: false,
        content: `<system-reminder>\nAdaptive Authority: sema-programmer\n\n${skillText}\n</system-reminder>`,
      },
    };
  });

  pi.on("tool_call", async (ctx: any) => {
    const toolName = ctx?.toolName;
    const args = ctx?.args || {};
    const file = args.file || args.path;
    if ((toolName === "write" || toolName === "edit" || toolName === "structural_edit") && file && file.endsWith(".rs")) {
      console.log(`[Arrangement Proxy] Analyzing ${file} for Cap'n Proto dependency...`);
      try {
        const schemaExists = execSync(`find Components/ -name "*.capnp" | wc -l`, {
          encoding: "utf-8",
        }).trim();
        if (parseInt(schemaExists) === 0) {
          console.warn(`[Violation Risk] Writing Rust logic before defining Cap'n Proto Schema!`);
        }
      } catch (_e) {}
    }
  });
}
