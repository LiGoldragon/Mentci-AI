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
        execute: async (args: any, context: any) => {
            try {
                // Ensure we run the binary compiled from the Mentci workspace
                const workspaceRoot = process.cwd(); 
                
                // Build it first just in case
                execSync(`cargo build --release -p mentci-mcp-edit`, { cwd: workspaceRoot, stdio: 'ignore' });
                
                const bin = path.join(workspaceRoot, "target", "release", "mentci-mcp-edit");
                
                const cmd = `"${bin}" --lang "${args.lang}" --pattern "${args.pattern.replace(/"/g, '\\"')}" --replace "${args.replace.replace(/"/g, '\\"')}" "${args.file}"`;
                
                const output = execSync(cmd, { cwd: workspaceRoot, encoding: "utf-8" });
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

    // Also register the Pre-Hook and Post-Hook concepts we talked about earlier
    // For now, let's just do a simple session start hook
    pi.on("session_start", async (context) => {
        try {
            const workspaceRoot = process.cwd();
            const output = execSync(`chronos --format am --precision second`, { cwd: workspaceRoot, encoding: "utf-8" }).trim();
            pi.ui.notify({ message: `[mentci-mcp] Session initialized. Solar Time: ${output}`, type: "info" });
        } catch (e) {
            // chronos might not be available
        }
    });
}
