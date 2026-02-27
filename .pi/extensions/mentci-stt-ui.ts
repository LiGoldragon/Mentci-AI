import type { ExtensionAPI, Theme } from "@mariozechner/pi-coding-agent";
import { Type } from "@sinclair/typebox";
import { execSync } from "child_process";
import * as path from "path";
import { Text } from "@mariozechner/pi-tui";

export default function (pi: ExtensionAPI) {
  pi.registerTool({
    name: "mentci_stt_transcribe",
    label: "mentci-stt",
    description: "Transcribe a voice recording using the Mentci STT engine (Cap'n Proto based).",
    parameters: Type.Object({
      audio: Type.String({ description: "Relative path to the .opus voice recording" }),
      capnp: Type.String({ 
        description: "Relative path to the prebuilt Cap'n Proto binary request message",
        default: "Components/mentci-stt/data/default_request.bin" 
      }),
    }),
    execute: async (_toolCallId: string, args: any) => {
      try {
        const workspaceRoot = process.cwd();
        // Use the wrapper to ensure env vars (API keys) are injected
        const cmd = \`mentci-stt --audio "\${args.audio}" --capnp "\${args.capnp}"\`;
        const output = execSync(cmd, { cwd: workspaceRoot, encoding: "utf-8" }).trim();

        return {
          content: [{ type: "text", text: output || "Transcription complete (no text output)." }],
        };
      } catch (e: any) {
        return {
          content: [{ type: "text", text: \`Error executing mentci-stt: \${e.message}\n\${e.stdout}\n\${e.stderr}\` }],
        };
      }
    },

    renderCall(args, theme) {
      const audioFile = typeof args.audio === "string" ? path.basename(args.audio) : "unknown";
      const text =
        theme.fg("toolTitle", theme.bold("mentci-stt ")) +
        theme.fg("accent", audioFile);
      return new Text(text, 0, 0);
    },

    renderResult(result, _options, theme) {
      const first = result.content?.[0];
      const raw = first?.type === "text" ? first.text : "";
      return new Text(theme.fg("success", raw), 0, 0);
    },
  });

  // Hotkey to trigger transcription on the latest .opus file
  pi.registerCommand("stt-latest", {
    description: "Transcribe the most recent .opus file in .voice-recordings/",
    execute: async (ctx) => {
      try {
        const workspaceRoot = process.cwd();
        const latestAudio = execSync(\`ls -t .voice-recordings/*.opus | head -n 1\`, {
          cwd: workspaceRoot,
          encoding: "utf-8",
        }).trim();

        if (!latestAudio) {
          ctx.ui.notify("No .opus files found in .voice-recordings/", "error");
          return;
        }

        ctx.ui.notify(\`Transcribing \${path.basename(latestAudio)}...\`, "info");
        
        // We use the default capnp request bin
        const capnp = "Components/mentci-stt/data/default_request.bin";
        const cmd = \`mentci-stt --audio "\${latestAudio}" --capnp "\${capnp}"\`;
        const output = execSync(cmd, { cwd: workspaceRoot, encoding: "utf-8" }).trim();

        // Inject the result into the editor
        ctx.ui.inject(\`Transcript of \${path.basename(latestAudio)}:\\n\\n\${output}\\n\`);
      } catch (e: any) {
        ctx.ui.notify(\`STT failed: \${e.message}\`, "error");
      }
    }
  });
}
