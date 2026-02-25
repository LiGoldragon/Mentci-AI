import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { execSync } from "child_process";
import * as path from "path";

export default function (pi: ExtensionAPI) {
    pi.registerTool({
        name: "structural_edit",
        label: "mentci-mcp-edit",
        description: [
            "Make surgical structural edits to code files using AST pattern matching instead of brittle Regex.",
            "Uses ast-grep underneath. The replacement will exactly preserve surrounding AST.",
            "Supported languages: rust, js, ts, python, nix, bash."
        ].join(" "),
        parameters: Type.Object({
            file: Type.String({ description: "Relative path to the file to modify" }),
            lang: Type.String({ description: "Language of the file (e.g., rust, ts, python, nix)" }),
            pattern: Type.String({ description: "The AST pattern to search for (e.g. 'fn $A() { $$$B }')" }),
            replace: Type.String({ description: "The AST replacement string (e.g. 'fn renamed() { $$$B }')" })
        }),
        execute: async (toolCallId: string, args: any) => {
            try {
                const workspaceRoot = process.cwd();
                const bin = path.join(workspaceRoot, "target", "release", "mentci-mcp-edit");
                const cmd = `"${bin}" --lang "${args.lang}" --pattern "${args.pattern.replace(/"/g, '\\"')}" --replace "${args.replace.replace(/"/g, '\\"')}" "${args.file}"`;
                const output = execSync(cmd, { cwd: workspaceRoot, encoding: "utf-8" });
                
                try {
                    execSync(`jj commit -m "intent: structurally edit ${args.file}"`, { cwd: workspaceRoot, stdio: 'ignore' });
                    // pi.ui.notify is deprecated/not available here on newer pi versions.
                    console.log(`Auto-committed structural edit to ${args.file}`);
                } catch (e) {
                    console.error(`Failed to auto-commit: ${e}`);
                }

                return {
                    content: [{ type: "text", text: output.trim() || "No output, execution might have failed or succeeded silently." }]
                };
            } catch (e: any) {
                return {
                    content: [{ type: "text", text: `Error executing structural_edit: ${e.message}\n${e.stdout}\n${e.stderr}` }]
                };
            }
        }
    });

    pi.on("session_start", async (context) => {
        try {
            const workspaceRoot = process.cwd();
            const chronos = execSync(`chronos --format am --precision second`, { cwd: workspaceRoot, encoding: "utf-8" }).trim();
            const jjStatus = execSync(`jj status --no-pager`, { cwd: workspaceRoot, encoding: "utf-8" }).trim();
            console.log(`\n--- Pre-Hook Injected State ---\nTime: ${chronos}\n${jjStatus}\n-------------------------------`);
        } catch (e) {}
    });

    pi.on("tool_call", async (ctx: any) => {
        const toolName = ctx?.toolName;
        const args = ctx?.args || {};
        const file = args.file || args.path;
        if ((toolName === "write" || toolName === "edit" || toolName === "structural_edit") && file && file.endsWith(".rs")) {
            console.log(`[Arrangement Proxy] Analyzing ${file} for Cap'n Proto dependency...`);
            try {
                const schemaExists = execSync(`find Components/ -name "*.capnp" | wc -l`, { encoding: "utf-8" }).trim();
                if (parseInt(schemaExists) === 0) {
                    console.warn(`[Violation Risk] Writing Rust logic before defining Cap'n Proto Schema!`);
                }
            } catch (e) {}
        }
    });
}
