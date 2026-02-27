import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { Text } from "@mariozechner/pi-tui";
import { createRuntime } from "../npm/node_modules/mcporter/dist/index.js";

async function resetRuntime(): Promise<void> {
  // No persistent runtime cache now; kept as explicit no-op for compatibility.
  return;
}

function toTextResult(result: unknown): string {
  // For the LLM context, we want the leanest possible valid JSON string.
  return JSON.stringify(result);
}

function renderQueryResults(result: any, theme: any): Text {
  const content = result.content?.[0]?.text;
  if (!content) return new Text(theme.fg("muted", "No output."), 0, 0);

  try {
    const parsed = JSON.parse(content);
    if (!Array.isArray(parsed) || parsed.length === 0) {
      return new Text(theme.fg("muted", "No matches found."), 0, 0);
    }

    let out = "";
    for (const match of parsed) {
      const file = match.file || "unknown";
      const line = match.line || "?";
      const capture = match.capture ? theme.fg("accent", `@${match.capture}`) : "";
      
      out += theme.fg("toolTitle", `${file}:${line}`) + " " + capture + "\n";
      
      if (match.text) {
        // Indent and dim the code snippet
        const lines = match.text.split("\n");
        const snippet = lines
          .map((l: string) => "  " + theme.fg("muted", l))
          .join("\n");
        out += snippet + "\n\n";
      }
    }
    return new Text(out.trim(), 0, 0);
  } catch {
    return new Text(content, 0, 0);
  }
}

async function withLogicalRuntime<T>(fn: (runtime: any) => Promise<T>): Promise<T> {
  const runtime = await createRuntime({ rootDir: process.cwd() });
  try {
    // Idempotent project registration MUST happen in the SAME process as the query.
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
  if (!content) return new Text(theme.fg("muted", "No output."), 0, 0);

  try {
    const parsed = JSON.parse(content);
    const tree = parsed.tree;
    if (!tree) return new Text(content, 0, 0);

    let out = theme.fg("toolTitle", `${parsed.file} [${parsed.language}]`) + "\n";

    function formatNode(node: any, depth: number): string {
      const indent = "  ".repeat(depth);
      let line = `${indent}${theme.fg("accent", node.type)}`;
      
      const start = node.start_point;
      if (start) {
        line += theme.fg("muted", ` [${start.row + 1}:${start.column}]`);
      }

      if (node.text && node.text.length < 60 && !node.children_count) {
        line += " " + theme.fg("success", JSON.stringify(node.text));
      }

      let res = line + "\n";
      if (node.children) {
        for (const child of node.children) {
          res += formatNode(child, depth + 1);
        }
      } else if (node.truncated) {
        res += `${indent}  ...\n`;
      }
      return res;
    }

    out += formatNode(tree, 0);
    return new Text(out.trim(), 0, 0);
  } catch {
    return new Text(content, 0, 0);
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
            args: {
              path: args.path,
              name: args.name,
              description: args.description,
            },
          })
        );
        const text = toTextResult((result as any).content?.[0]?.text || result);
        return { content: [{ type: "text", text }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `logical_register_project error: ${e?.message || e}` }] };
      }
    },
    renderCall(args, theme) {
      const text =
        theme.fg("toolTitle", theme.bold("logical_register_project ")) +
        theme.fg("accent", args.name || args.path || "project");
      return new Text(text, 0, 0);
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
          const raw: any = await runtime.callTool("mentci-tree-sitter", "get_ast", {
            args: {
              project: args.project,
              path: args.path,
              max_depth: args.maxDepth,
              include_text: args.includeText,
            },
          });
          return raw;
        });
        const text = toTextResult((result as any).content?.[0]?.text || result);
        return { content: [{ type: "text", text }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `logical_get_ast error: ${e?.message || e}` }] };
      }
    },
    renderCall(args, theme) {
      const text =
        theme.fg("toolTitle", theme.bold("logical_get_ast ")) +
        theme.fg("accent", args.path || "file");
      return new Text(text, 0, 0);
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
            args: {
              project: args.project,
              query: args.query,
              file_path: args.filePath,
              language: args.language,
              max_results: args.maxResults,
            },
          });

          const data = (raw as any).structuredContent?.result || (raw as any).content?.[0]?.text || raw;

          if (Array.isArray(data)) {
            return data.map((match: any) => ({
              file: match.file,
              line: (match.start?.row ?? 0) + 1,
              capture: match.capture,
              text: match.text?.trim()
            }));
          }
          return data;
        });
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `logical_run_query error: ${e?.message || e}` }] };
      }
    },
    renderCall(_args, theme) {
      const text = theme.fg("toolTitle", theme.bold("logical_run_query"));
      return new Text(text, 0, 0);
    },
    renderResult(result, _options, theme) {
      return renderQueryResults(result, theme);
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
      const text = theme.fg("toolTitle", theme.bold("logical_reset_runtime"));
      return new Text(text, 0, 0);
    },
  });
}
