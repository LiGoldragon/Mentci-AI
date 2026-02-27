import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { Text } from "@mariozechner/pi-tui";
import { createRuntime } from "../npm/node_modules/mcporter/dist/index.js";
import hljs from "../npm/node_modules/highlight.js/lib/index.js";
import * as fs from "fs";
import * as path from "path";

async function resetRuntime(): Promise<void> {
  return;
}

function toTextResult(result: unknown): string {
  return JSON.stringify(result);
}

/**
 * Mirror Hook: Writes the final UI string to a predictable location.
 */
function mirrorUI(text: string) {
  try {
    const mirrorPath = "/home/li/git/Mentci-AI/.mentci/ui_mirror.txt";
    fs.appendFileSync(mirrorPath, "\n--- SNAPSHOT ---\n" + text, "utf-8");
  } catch {}
}

const COLOR_MAP: Record<string, string> = {
  'keyword': '\x1b[35m',      // Purple
  'string': '\x1b[32m',       // Green
  'comment': '\x1b[90m',      // Grey
  'function': '\x1b[34m',     // Blue
  'title': '\x1b[34m',
  'params': '\x1b[37m',       // White
  'number': '\x1b[33m',       // Yellow
  'built_in': '\x1b[36m',     // Cyan
  'attr': '\x1b[36m',
  'symbol': '\x1b[33m',       // Yellow
  'punctuation': '\x1b[37m',
  'meta': '\x1b[90m',
  'operator': '\x1b[37m',
  'variable': '\x1b[37m',
  'type': '\x1b[36m',
  'property': '\x1b[36m'
};

/**
 * Extremely aggressive highlighter that strips ALL spans and replaces with ANSI.
 */
function highlightCode(code: string, lang: string): string {
  if (!code) return "";
  try {
    const res = hljs.highlight(code, { language: lang });
    let html = res.value;
    if (!html) return code;

    // 1. Map known hljs classes to ANSI
    // We use a loose match to catch spans with multiple classes or different attribute orders
    html = html.replace(/<span[^>]+class=["'][^"']*hljs-([^" ]+)[^"']*["'][^>]*>/g, (_match, group) => {
        return COLOR_MAP[group] || '\x1b[37m';
    });
    
    // 2. Close all spans
    html = html.replace(/<\/span>/g, '\x1b[0m');
    
    // 3. Nuke any remaining stray HTML tags (safety)
    html = html.replace(/<[^>]+>/g, '');
    
    // 4. Decode entities
    return html
      .replace(/&quot;/g, '"')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&amp;/g, '&');
  } catch (e) {
    return code; 
  }
}

function stringifyEDN(obj: any): string {
  if (obj === null || obj === undefined) return "nil";
  if (typeof obj === "string") return `"${obj.replace(/"/g, '\\"')}"`;
  if (typeof obj === "number") return obj.toString();
  if (typeof obj === "boolean") return obj.toString();
  if (Array.isArray(obj)) return "[" + obj.map(stringifyEDN).join(" ") + "]";
  if (typeof obj === "object") {
    const entries = Object.entries(obj).map(([k, v]) => `:${k} ${stringifyEDN(v)}`).join("\n  ");
    return `{\n  ${entries}\n}`;
  }
  return String(obj);
}

function renderQueryResults(result: any, theme: any): Text {
  const content = result.content?.[0]?.text;
  if (!content) return new Text(theme.fg("error", "No match content."), 0, 0);

  let finalOutput = "";
  try {
    const envelope = JSON.parse(content);
    const results = envelope.results || [];
    const callArgs = envelope.args || {};

    // 1. Amazing Header: The EDN Intent block
    finalOutput += theme.fg("toolTitle", theme.bold(";; Logical Query Intent")) + "\n";
    finalOutput += highlightCode(stringifyEDN(callArgs), "clojure") + "\n";
    finalOutput += theme.fg("muted", "─".repeat(50)) + "\n\n";

    if (!Array.isArray(results) || results.length === 0) {
      finalOutput += theme.fg("muted", "No matches found.");
    } else {
      for (const match of results) {
        const file = match.file || "unknown";
        const line = match.line || "?";
        const capture = match.capture ? theme.fg("accent", `@${match.capture}`) : "";
        finalOutput += theme.fg("toolTitle", `${file}:${line}`) + " " + capture + "\n";
        if (match.text) {
          const lang = file.endsWith(".rs") ? "rust" : "typescript";
          const highlighted = highlightCode(match.text, lang);
          finalOutput += highlighted.split("\n").map((l: string) => "  " + l).join("\n") + "\n\n";
        }
      }
    }
    const res = finalOutput.trim();
    mirrorUI(res);
    return new Text(res, 0, 0);
  } catch (e: any) {
    const err = theme.fg("error", `Render Error: ${e.message}`) + "\n" + content;
    mirrorUI(err);
    return new Text(err, 0, 0);
  }
}

async function withLogicalRuntime<T>(fn: (runtime: any) => Promise<T>): Promise<T> {
  const runtime = await createRuntime({ rootDir: process.cwd() });
  try {
    await runtime.callTool("mentci-tree-sitter", "register_project_tool", {
      args: { path: ".", name: "mentci" },
    }).catch(() => undefined);
    return await fn(runtime);
  } finally {
    await runtime.close();
  }
}

function renderAstResult(result: any, theme: any): Text {
  const content = result.content?.[0]?.text;
  if (!content) return new Text(theme.fg("error", "Empty AST."), 0, 0);
  try {
    const parsed = JSON.parse(content);
    const tree = parsed.tree;
    if (!tree) { mirrorUI(content); return new Text(content, 0, 0); }
    let out = theme.fg("toolTitle", `${parsed.file} [${parsed.language}]`) + "\n";
    function formatNode(node: any, depth: number): string {
      const indent = "  ".repeat(depth);
      let line = `${indent}${theme.fg("accent", node.type)}`;
      if (node.start_point) line += theme.fg("muted", ` [${node.start_point.row + 1}:${node.start_point.column}]`);
      if (node.text && node.text.length < 60 && !node.children_count) line += " " + theme.fg("success", JSON.stringify(node.text));
      let res = line + "\n";
      if (node.children) for (const child of node.children) res += formatNode(child, depth + 1);
      else if (node.truncated) res += `${indent}  ...\n`;
      return res;
    }
    out += formatNode(tree, 0);
    const res = out.trim();
    mirrorUI(res);
    return new Text(res, 0, 0);
  } catch (e: any) {
    const err = theme.fg("error", `AST Error: ${e.message}`) + "\n" + content;
    mirrorUI(err);
    return new Text(err, 0, 0);
  }
}

export default function (pi: ExtensionAPI) {
  pi.registerTool({
    name: "logical_register_project",
    label: "logical-edit",
    description: "Register a project directory.",
    parameters: Type.Object({
      path: Type.String(),
      name: Type.Optional(Type.String()),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await withLogicalRuntime((runtime) =>
          runtime.callTool("mentci-tree-sitter", "register_project_tool", {
            args: { path: args.path, name: args.name },
          })
        );
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `Error: ${e.message}` }] };
      }
    },
  });

  pi.registerTool({
    name: "logical_get_ast",
    label: "logical-edit",
    description: "Get AST.",
    parameters: Type.Object({
      project: Type.String(),
      path: Type.String(),
      maxDepth: Type.Optional(Type.Number()),
      includeText: Type.Optional(Type.Boolean()),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await withLogicalRuntime(async (runtime) => {
          return await runtime.callTool("mentci-tree-sitter", "get_ast", {
            args: { project: args.project, path: args.path, max_depth: args.maxDepth, include_text: args.includeText },
          });
        });
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `Error: ${e.message}` }] };
      }
    },
    renderResult(result, _options, theme) {
      return renderAstResult(result, theme);
    },
  });

  pi.registerTool({
    name: "logical_run_query",
    label: "logical-edit",
    description: "Run structural syntax query.",
    parameters: Type.Object({
      project: Type.String(),
      query: Type.String(),
      filePath: Type.Optional(Type.String()),
      language: Type.Optional(Type.String()),
      maxResults: Type.Optional(Type.Number()),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await withLogicalRuntime(async (runtime) => {
          const raw: any = await runtime.callTool("mentci-tree-sitter", "run_query", {
            args: { project: args.project, query: args.query, file_path: args.filePath, language: args.language, max_results: args.maxResults },
          });

          const contentStr = (raw as any).content?.[0]?.text;
          let data = raw;
          if (contentStr) { try { data = JSON.parse(contentStr); } catch { data = contentStr; } }
          if (data && data.content && Array.isArray(data.content)) { try { data = JSON.parse(data.content[0].text); } catch {} }

          let results = [];
          if (Array.isArray(data)) {
            results = data.map((match: any) => ({
              file: match.file,
              line: match.line || (match.start?.row ?? 0) + 1,
              capture: match.capture,
              text: match.text?.trim()
            }));
          } else if (data && typeof data === 'object' && data.file) {
              results = [{ file: data.file, line: data.line || (data.start?.row ?? 0) + 1, capture: data.capture, text: data.text?.trim() }];
          }
          return { results, args };
        });
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `Error: ${e.message}` }] };
      }
    },
    renderResult(result, _options, theme) {
      return renderQueryResults(result, theme);
    },
  });

  pi.registerTool({
    name: "logical_debug_view",
    label: "logical-edit",
    description: "View the UI mirror file (agent only).",
    parameters: Type.Object({}),
    execute: async () => {
      try {
        const mirrorPath = "/home/li/git/Mentci-AI/.mentci/ui_mirror.txt";
        const text = fs.readFileSync(mirrorPath, "utf-8");
        return { content: [{ type: "text", text }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `No mirror found: ${e.message}` }] };
      }
    }
  });

  pi.registerCommand("logical-reload", {
    description: "Reload Extensions",
    handler: async (_args, ctx) => {
      ctx.ui.notify("Reloading...", "info");
      await ctx.reload();
    }
  });
}
