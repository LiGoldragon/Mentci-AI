import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { Text } from "@mariozechner/pi-tui";
import { createRuntime, type Runtime } from "mcporter";

let runtimePromise: Promise<Runtime> | null = null;

async function getRuntime(): Promise<Runtime> {
  if (!runtimePromise) {
    runtimePromise = createRuntime({ rootDir: process.cwd() });
  }
  return runtimePromise;
}

function toTextResult(result: unknown): string {
  if (typeof result === "string") return result;
  try {
    return JSON.stringify(result, null, 2);
  } catch {
    return String(result);
  }
}

async function callWrale(toolName: string, args: Record<string, unknown>) {
  const runtime = await getRuntime();
  return runtime.callTool("wrale-tree-sitter", toolName, { args });
}

export default function (pi: ExtensionAPI) {
  pi.registerTool({
    name: "wrale_register_project",
    label: "wrale-tree-sitter",
    description: "Register a project directory with wrale tree-sitter MCP server.",
    parameters: Type.Object({
      path: Type.String({ description: "Project root path (e.g. .)" }),
      name: Type.Optional(Type.String({ description: "Project alias" })),
      description: Type.Optional(Type.String({ description: "Optional project description" })),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await callWrale("register_project_tool", {
          path: args.path,
          name: args.name,
          description: args.description,
        });
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `wrale_register_project error: ${e?.message || e}` }] };
      }
    },
    renderCall(args, theme) {
      const text =
        theme.fg("toolTitle", theme.bold("wrale_register_project ")) +
        theme.fg("accent", args.name || args.path || "project");
      return new Text(text, 0, 0);
    },
  });

  pi.registerTool({
    name: "wrale_get_ast",
    label: "wrale-tree-sitter",
    description: "Get AST for a file through wrale tree-sitter MCP server.",
    parameters: Type.Object({
      project: Type.String({ description: "Registered project name" }),
      path: Type.String({ description: "File path relative to project root" }),
      maxDepth: Type.Optional(Type.Number({ description: "AST depth limit" })),
      includeText: Type.Optional(Type.Boolean({ description: "Include node text" })),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await callWrale("get_ast", {
          project: args.project,
          path: args.path,
          max_depth: args.maxDepth,
          include_text: args.includeText,
        });
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `wrale_get_ast error: ${e?.message || e}` }] };
      }
    },
    renderCall(args, theme) {
      const text =
        theme.fg("toolTitle", theme.bold("wrale_get_ast ")) +
        theme.fg("accent", args.path || "file");
      return new Text(text, 0, 0);
    },
  });

  pi.registerTool({
    name: "wrale_run_query",
    label: "wrale-tree-sitter",
    description: "Run a tree-sitter query via wrale server.",
    parameters: Type.Object({
      project: Type.String({ description: "Registered project name" }),
      query: Type.String({ description: "Tree-sitter query string" }),
      filePath: Type.Optional(Type.String({ description: "Optional file path" })),
      language: Type.Optional(Type.String({ description: "Optional language" })),
      maxResults: Type.Optional(Type.Number({ description: "Max matches" })),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const result = await callWrale("run_query", {
          project: args.project,
          query: args.query,
          file_path: args.filePath,
          language: args.language,
          max_results: args.maxResults,
        });
        return { content: [{ type: "text", text: toTextResult(result) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `wrale_run_query error: ${e?.message || e}` }] };
      }
    },
    renderCall(_args, theme) {
      const text = theme.fg("toolTitle", theme.bold("wrale_run_query"));
      return new Text(text, 0, 0);
    },
  });
}
