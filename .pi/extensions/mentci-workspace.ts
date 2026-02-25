import type { ExtensionAPI } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { execSync } from "child_process";
import * as path from "path";
import * as fs from "fs";

export default function (pi: ExtensionAPI) {
    // 1. Structural Edit Tool Registration
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
        execute: async (args: any) => {
            try {
                const workspaceRoot = process.cwd();
                execSync(`cargo build --release -p mentci-mcp-edit`, { cwd: workspaceRoot, stdio: 'ignore' });
                const bin = path.join(workspaceRoot, "target", "release", "mentci-mcp-edit");
                const cmd = `"${bin}" --lang "${args.lang}" --pattern "${args.pattern.replace(/"/g, '\\"')}" --replace "${args.replace.replace(/"/g, '\\"')}" "${args.file}"`;
                const output = execSync(cmd, { cwd: workspaceRoot, encoding: "utf-8" });
                
                // Trigger auto-commit (Post-Hook)
                try {
                    execSync(`jj commit -m "intent: structurally edit ${args.file}"`, { cwd: workspaceRoot, stdio: 'ignore' });
                    pi.ui.notify({ message: `Auto-committed structural edit to ${args.file}`, type: "success" });
                } catch (e) {
                    pi.ui.notify({ message: `Failed to auto-commit: ${e}`, type: "error" });
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

    // 2. Pre-Hook Context Injection
    pi.on("session_start", async (context) => {
        try {
            const workspaceRoot = process.cwd();
            const chronos = execSync(`chronos --format am --precision second`, { cwd: workspaceRoot, encoding: "utf-8" }).trim();
            const jjStatus = execSync(`jj status --no-pager`, { cwd: workspaceRoot, encoding: "utf-8" }).trim();
            
            // Send an immediate notification to the UI
            pi.ui.notify({ message: `[Mentci] Solar: ${chronos}`, type: "info" });
            
            // Automatically append this state as a silent context update
            // (If context modification isn't directly exposed here, we simulate it via logs or notifications)
            console.log(`\n--- Pre-Hook Injected State ---\nTime: ${chronos}\n${jjStatus}\n-------------------------------`);
        } catch (e) {
            // Ignore if commands fail
        }
    });

    // 3. Post-Hook: Cap'n Proto / Style Gate Check (The "Brainstorming" Implementation)
    pi.on("tool_call", async (ctx: any) => {
        const { toolName, args } = ctx;
        if ((toolName === "write" || toolName === "edit" || toolName === "structural_edit") && args.file?.endsWith(".rs")) {
            // Log a TUI warning checking for Capnp dependency
            pi.ui.notify({ message: `[Arrangement Proxy] Analyzing ${args.file} for Cap'n Proto dependency...`, type: "warning" });
            
            // Naive check: does a .capnp file exist in the Components directory?
            try {
                const schemaExists = execSync(`find Components/ -name "*.capnp" | wc -l`, { encoding: "utf-8" }).trim();
                if (parseInt(schemaExists) === 0) {
                    // We don't block the tool execution yet, but we loudly warn the agent.
                    pi.ui.notify({ message: `[Violation Risk] Writing Rust logic before defining Cap'n Proto Schema!`, type: "error" });
                }
            } catch (e) {}
        }
    });
}
