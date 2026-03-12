#!/usr/bin/env node
const path = require("path");
const { McpServer } = require(path.resolve(__dirname, "../../.pi/npm/node_modules/@modelcontextprotocol/sdk/dist/cjs/server/mcp.js"));
const { StdioServerTransport } = require(path.resolve(__dirname, "../../.pi/npm/node_modules/@modelcontextprotocol/sdk/dist/cjs/server/stdio.js"));
const { z } = require(path.resolve(__dirname, "../../.pi/npm/node_modules/zod"));
const { JjWrapper } = require("/nix/store/zcbcys1rlrxa5nbm7fb573l9hp1zmnw3-agentic-jujutsu-2.3.6/libexec/agentic-jujutsu");

const server = new McpServer({
  name: "agentic-jujutsu",
  version: "2.3.6"
});

const jj = new JjWrapper();

server.tool("jj_status", "Show working copy status", {}, async () => {
  try {
    const result = await jj.status();
    return { content: [{ type: "text", text: result.stdout || result.stderr }] };
  } catch (e) {
    return { content: [{ type: "text", text: e.message }], isError: true };
  }
});

server.tool("jj_log", "Show commit history", { limit: z.number().optional().default(10) }, async ({ limit }) => {
  try {
    const result = await jj.log(limit || 10);
    return { content: [{ type: "text", text: result.stdout || result.stderr }] };
  } catch (e) {
    return { content: [{ type: "text", text: e.message }], isError: true };
  }
});

server.tool("jj_diff", "Show changes", { revision: z.string().optional() }, async ({ revision }) => {
  try {
    const result = await jj.diff(revision || "@");
    return { content: [{ type: "text", text: result.stdout || result.stderr }] };
  } catch (e) {
    return { content: [{ type: "text", text: e.message }], isError: true };
  }
});

server.tool("jj_execute", "Execute any jj command", { args: z.array(z.string()) }, async ({ args }) => {
  try {
    const result = await jj.execute(args);
    return { content: [{ type: "text", text: result.stdout || result.stderr }] };
  } catch (e) {
    return { content: [{ type: "text", text: e.message }], isError: true };
  }
});

server.tool("jj_analyze", "Analyze repository for AI", {}, async () => {
  try {
    const status = await jj.status();
    const log = await jj.log(5);
    return { content: [{ type: "text", text: `Status:\n${status.stdout}\n\nRecent History:\n${log.stdout}` }] };
  } catch (e) {
    return { content: [{ type: "text", text: e.message }], isError: true };
  }
});

async function run() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

run().catch(console.error);
