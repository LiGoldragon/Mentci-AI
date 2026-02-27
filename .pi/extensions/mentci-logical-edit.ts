import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { Text } from "@mariozechner/pi-tui";
import { createRuntime } from "../npm/node_modules/mcporter/dist/index.js";

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

function renderQueryResults(result: any, theme: any): Text {
  const content = result.content?.[0]?.text;
  if (!content) return new Text(theme.fg("muted", "No output."), 0, 0);
  // RAW UI - No complex regex or highlighting for now to restore CPU stability
  return new Text(content, 0, 0);
}

function renderAstResult(result: any, theme: any): Text {
  const content = result.content?.[0]?.text;
  if (!content) return new Text(theme.fg("muted", "No output."), 0, 0);
  return new Text(content, 0, 0);
}

export default function (pi: ExtensionAPI) {
  pi.registerTool({
    name: "logical_run_query",
    label: "logical-edit",
    description: "Query code.",
    parameters: Type.Object({
      project: Type.String(),
      query: Type.String(),
      filePath: Type.Optional(Type.String()),
      language: Type.Optional(Type.String()),
      maxResults: Type.Optional(Type.Number()),
    }),
    execute: async (_toolCallId, args) => {
      try {
        const result = await withLogicalRuntime(async (runtime) => {
          const raw: any = await runtime.callTool("mentci-tree-sitter", "run_query", {
            args: { project: args.project, query: args.query, file_path: args.filePath, language: args.language, max_results: args.maxResults },
          });
          return raw;
        });
        return { content: [{ type: "text", text: JSON.stringify(result, null, 2) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `Error: ${e.message}` }] };
      }
    },
    renderResult(result, _options, theme) {
      return renderQueryResults(result, theme);
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
    }),
    execute: async (_toolCallId, args) => {
      try {
        const result = await withLogicalRuntime(async (runtime) => {
          return await runtime.callTool("mentci-tree-sitter", "get_ast", {
            args: { project: args.project, path: args.path, max_depth: args.maxDepth },
          });
        });
        return { content: [{ type: "text", text: JSON.stringify(result, null, 2) }] };
      } catch (e: any) {
        return { content: [{ type: "text", text: `Error: ${e.message}` }] };
      }
    },
    renderResult(result, _options, theme) {
      return renderAstResult(result, theme);
    },
  });

  pi.registerCommand("logical-reload", {
    description: "Reload Extensions",
    handler: async (_args, ctx) => {
      await ctx.reload();
    }
  });
}
