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
  'function': '\x1b[34m',      // Blue
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
  'type': '\x1b[36m'
};

/**
 * Robust highlighter that maps all hljs classes to ANSI.
 */
function highlightCode(code: string, lang: string, theme: any): string {
  if (!code) return "";
  try {
    const res = hljs.highlight(code, { language: lang });
    let html = res.value;
    if (!html) return code;

    // Greedy regex to catch any span with an hljs class
    html = html.replace(/<span[^>]*class="[^"]*hljs-([^" ]+)[^"]*"[^>]*>/g, (_match, group) => {
        return COLOR_MAP[group] || '\x1b[37m';
    });
    
    html = html.replace(/<\/span>/g, '\x1b[0m');
    
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

function renderQueryResults(result: any, theme: any, args: any): Text {
  const content = result.content?.[0]?.text;
  if (!content) return new Text(theme.fg("error", "No match content."), 0, 0);

  let finalOutput = "";
  try {
    let rawData = JSON.parse(content);
    let results: any[] = [];
    let callArgs = args;

    if (rawData.results && Array.isArray(rawData.results)) {
        results = rawData.results;
        callArgs = rawData.args || args;
    } else if (Array.isArray(rawData)) {
        results = rawData;
    } else if (typeof rawData === 'object' && rawData.file) {
        results = [rawData];
    }

    const intentObj = {
      project: callArgs?.project || "mentci",
      language: callArgs?.language || "rust",
      query: callArgs?.query || "unknown"
    };
    
    finalOutput += theme.fg("toolTitle", theme.bold(";; Logical Query Intent")) + "\n";
    finalOutput += highlightCode(stringifyEDN(intentObj), "clojure", theme) + "\n";
    finalOutput += theme.fg("muted", "─".repeat(50)) + "\n\n";

    if (results.length === 0) {
      finalOutput += theme.fg("muted", "No matches found.");
    } else {
      for (const match of results) {
        const file = match.file || "unknown";
        const line = match.line || "?";
        const capture = match.capture ? theme.fg("accent", `@${match.capture}`) : "";
        finalOutput += theme.fg("toolTitle", `${file}:${line}`) + " " + capture + "\n";
        if (match.text) {
          const lang = file.endsWith(".rs") ? "rust" : "typescript";
          const highlighted = highlightCode(match.text, lang, theme);
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
      const start = node.start_point;
      if (start) line += theme.fg("muted", ` [${start.row + 1}:${start.column}]`);
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
    description: "Register a project directory for logical code analysis.",
    parameters: Type.Object({
      path: Type.String({ description: "Project root path (e.g. .)" }),
      name: Type.Optional(Type.String({ description: "Project alias" })),
      description: Type.Optional(Type.String({ description: "Optional project description" })),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await withLogicalRuntime((runtime) =>
          runtime.callTool("mentci-tree-sitter", "register_project_tool", {
            args: { path: args.path, name: args.name, description: args.description },
          })
        );
        const data = (result as any).structuredContent?.result || (result as any).content?.[0]?.text || result;
        return { content: [{ type: "text", text: toTextResult(data) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `logical_register_project error: ${e?.message || e}` }] };
      }
    },
    renderCall(args, theme) {
      return new Text(theme.fg("toolTitle", theme.bold("logical_register_project ")) + theme.fg("accent", args.name || args.path || "project"), 0, 0);
    },
  });

  pi.registerTool({
    name: "logical_get_ast",
    label: "logical-edit",
    description: "Get AST for a file through logical analysis engine.",
    parameters: Type.Object({
      project: Type.String({ description: "Registered project name" }),
      path: Type.String({ description: "File path relative to project root" }),
      maxDepth: Type.Optional(Type.Number({ description: "AST depth limit" })),
      includeText: Type.Optional(Type.Boolean({ description: "Include node text" })),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await withLogicalRuntime(async (runtime) => {
          return await runtime.callTool("mentci-tree-sitter", "get_ast", {
            args: { project: args.project, path: args.path, max_depth: args.maxDepth, include_text: args.includeText },
          });
        });
        const data = (result as any).structuredContent?.result || (result as any).content?.[0]?.text || result;
        return { content: [{ type: "text", text: toTextResult(data) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `logical_get_ast error: ${e?.message || e}` }] };
      }
    },
    renderCall(args, theme) {
      return new Text(theme.fg("toolTitle", theme.bold("logical_get_ast ")) + theme.fg("accent", args.path || "file"), 0, 0);
    },
    renderResult(result, _options, theme) {
      return renderAstResult(result, theme);
    },
  });

  pi.registerTool({
    name: "logical_run_query",
    label: "logical-edit",
    description: "Run a syntax query via logical analysis engine.",
    parameters: Type.Object({
      project: Type.String({ description: "Registered project name" }),
      query: Type.String({ description: "Tree-sitter query string" }),
      filePath: Type.Optional(Type.String({ description: "Optional file path" })),
      language: Type.Optional(Type.String({ description: "Optional language" })),
      maxResults: Type.Optional(Type.Number({ description: "Max matches" })),
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
            results = data.map((match: any) => {
                let snippet = match.text?.trim() || "";
                return { file: match.file, line: match.line || (match.start?.row ?? 0) + 1, capture: match.capture, text: snippet };
            });
          } else if (data && typeof data === 'object' && data.file) {
              results = [{ file: data.file, line: data.line || (data.start?.row ?? 0) + 1, capture: data.capture, text: data.text?.trim() }];
          }
          return { results, args };
        });
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `logical_run_query error: ${e?.message || e}` }] };
      }
    },
    renderCall(args, theme) {
      return new Text(theme.fg("toolTitle", theme.bold("logical_run_query")), 0, 0);
    },
    renderResult(result, options, theme) {
      return renderQueryResults(result, theme, (options as any).args);
    },
  });

  pi.registerTool({
    name: "logical_reset_runtime",
    label: "logical-edit",
    description: "Reset logical editing runtime connection.",
    parameters: Type.Object({}),
    execute: async () => {
      await resetRuntime();
      return { content: [{ type: "text", text: "logical runtime reset" }] };
    },
    renderCall(_args, theme) {
      return new Text(theme.fg("toolTitle", theme.bold("logical_reset_runtime")), 0, 0);
    },
  });
}
