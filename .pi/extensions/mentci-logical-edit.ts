import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { Text } from "@mariozechner/pi-tui";
import { createRuntime } from "../npm/node_modules/mcporter/dist/index.js";
import hljs from "../npm/node_modules/highlight.js/lib/index.js";

async function resetRuntime(): Promise<void> {
  return;
}

function toTextResult(result: unknown): string {
  return JSON.stringify(result);
}

/**
 * Converts highlight.js HTML output to Pi-TUI Text objects with ANSI-like coloring.
 */
function highlightCode(code: string, lang: string, theme: any): string {
  if (!code) return "";
  try {
    // highlight.js returns { value: '...' }
    const res = hljs.highlight(code, { language: lang });
    const highlighted = res.value;
    
    if (!highlighted) return code;

    return highlighted
      .replace(/<span class="hljs-keyword">/g, '\x1b[35m')      // Purple
      .replace(/<span class="hljs-string">/g, '\x1b[32m')       // Green
      .replace(/<span class="hljs-comment">/g, '\x1b[90m')      // Grey
      .replace(/<span class="hljs-function">/g, '\x1b[34m')     // Blue
      .replace(/<span class="hljs-title( function_)?">/g, '\x1b[34m')
      .replace(/<span class="hljs-params">/g, '\x1b[37m')      // White
      .replace(/<span class="hljs-number">/g, '\x1b[33m')      // Yellow
      .replace(/<span class="hljs-built_in">/g, '\x1b[36m')    // Cyan
      .replace(/<span class="hljs-attr">/g, '\x1b[36m')
      .replace(/<\/span>/g, '\x1b[0m')
      .replace(/&quot;/g, '"')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&amp;/g, '&');
  } catch (e) {
    return code; // Fallback to raw code on highter error
  }
}

/**
 * Manual EDN stringification for simple objects.
 */
function stringifyEDN(obj: any): string {
  if (obj === null || obj === undefined) return "nil";
  if (typeof obj === "string") return `"${obj.replace(/"/g, '\\"')}"`;
  if (typeof obj === "number") return obj.toString();
  if (typeof obj === "boolean") return obj.toString();
  if (Array.isArray(obj)) {
    return "[" + obj.map(stringifyEDN).join(" ") + "]";
  }
  if (typeof obj === "object") {
    const entries = Object.entries(obj)
      .map(([k, v]) => `:${k} ${stringifyEDN(v)}`)
      .join("\n  ");
    return `{\n  ${entries}\n}`;
  }
  return String(obj);
}

function renderQueryResults(result: any, theme: any, args: any): Text {
  const content = result.content?.[0]?.text;
  if (!content) return new Text(theme.fg("error", "Empty tool result."), 0, 0);

  try {
    let data = JSON.parse(content);
    
    // Normalize data to array
    if (!Array.isArray(data)) {
        if (data && typeof data === 'object' && data.file) {
            data = [data];
        } else {
            return new Text(theme.fg("error", "Result is not a valid match set.") + "\n" + content, 0, 0);
        }
    }

    let out = "";
    
    // 1. Amazing Header: The EDN Intent block
    // Note: args is the second parameter in renderResult(result, args, theme)
    const queryStr = args?.query || "unknown";
    const project = args?.project || "mentci";
    const language = args?.language || "rust";

    const intentObj = {
      project,
      language,
      query: queryStr
    };
    
    out += theme.fg("toolTitle", theme.bold(";; Logical Query Intent")) + "\n";
    out += highlightCode(stringifyEDN(intentObj), "clojure", theme) + "\n";
    out += theme.fg("muted", "─".repeat(50)) + "\n\n";

    if (data.length === 0) {
      out += theme.fg("muted", "No matches found in the codebase.");
      return new Text(out, 0, 0);
    }

    // 2. High-Density Matches
    for (const match of data) {
      const file = match.file || "unknown";
      const line = match.line || "?";
      const capture = match.capture ? theme.fg("accent", `@${match.capture}`) : "";
      
      out += theme.fg("toolTitle", `${file}:${line}`) + " " + capture + "\n";
      
      if (match.text) {
        const lang = file.endsWith(".rs") ? "rust" : file.endsWith(".ts") ? "typescript" : "text";
        const highlighted = highlightCode(match.text, lang, theme);
        // Indent snippets for better visual separation
        out += highlighted.split("\n").map((l: string) => "  " + l).join("\n") + "\n\n";
      }
    }

    return new Text(out.trim(), 0, 0);
  } catch (e: any) {
    return new Text(
      theme.fg("error", `Rendering Exception: ${e.message}`) + 
      "\n" + theme.fg("muted", e.stack || "") + 
      "\n\nRAW CONTENT:\n" + content, 
      0, 0
    );
  }
}

async function withLogicalRuntime<T>(fn: (runtime: any) => Promise<T>): Promise<T> {
  const runtime = await createRuntime({ rootDir: process.cwd() });
  try {
    // Ensure project is always registered in the same process
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
  if (!content) return new Text(theme.fg("error", "Empty AST result."), 0, 0);

  try {
    const parsed = JSON.parse(content);
    const tree = parsed.tree;
    if (!tree) return new Text(theme.fg("error", "No AST tree found in result.") + "\n" + content, 0, 0);

    let out = theme.fg("toolTitle", `${parsed.file} [${parsed.language}]`) + "\n";

    function formatNode(node: any, depth: number): string {
      const indent = "  ".repeat(depth);
      let line = `${indent}${theme.fg("accent", node.type)}`;
      const start = node.start_point;
      if (start) line += theme.fg("muted", ` [${start.row + 1}:${start.column}]`);
      if (node.text && node.text.length < 60 && !node.children_count) line += " " + theme.fg("success", JSON.stringify(node.text));
      let res = line + "\n";
      if (node.children) {
        for (const child of node.children) res += formatNode(child, depth + 1);
      } else if (node.truncated) res += `${indent}  ...\n`;
      return res;
    }

    out += formatNode(tree, 0);
    return new Text(out.trim(), 0, 0);
  } catch (e: any) {
    return new Text(theme.fg("error", `AST Rendering Exception: ${e.message}`) + "\n\n" + content, 0, 0);
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

          // DEPOT: Extract data from the double-wrapped mcporter envelope.
          const data = (raw as any).structuredContent?.result || (raw as any).content?.[0]?.text || raw;

          let final = data;
          if (typeof data === 'string') {
              try { final = JSON.parse(data); } catch { final = data; }
          }
          
          if (final && final.content && Array.isArray(final.content)) {
               try { final = JSON.parse(final.content[0].text); } catch {}
          }

          if (Array.isArray(final)) {
            return final.map((match: any) => ({
              file: match.file,
              line: (match.start?.row ?? 0) + 1,
              capture: match.capture,
              text: match.text?.trim()
            }));
          }
          return final;
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
      // options.args contains the tool call arguments
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
