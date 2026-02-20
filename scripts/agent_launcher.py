#!/usr/bin/env python3
import argparse
import os
import subprocess
import sys

DEFAULT_GOPASS_PREFIX = "mentci/ai"
DEFAULT_COMMAND = ["opencode"]
ENV_BY_PROVIDER = {
    "deepseek": "DEEPSEEK_API_KEY",
    "openai": "OPENAI_API_KEY",
    "anthropic": "ANTHROPIC_API_KEY",
}


def _read_gopass_secret(entry: str) -> str:
    try:
        result = subprocess.run(
            ["gopass", "show", "--password", entry],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
    except FileNotFoundError:
        raise RuntimeError("gopass not found in PATH") from None
    except subprocess.CalledProcessError as exc:
        detail = exc.stderr.strip() or exc.stdout.strip()
        raise RuntimeError(f"gopass failed for {entry}: {detail}") from None
    return result.stdout.strip()


def _resolve_entry(prefix: str, provider: str, entry_override: str | None) -> str:
    if entry_override:
        return entry_override
    return f"{prefix}/{provider}/api-key"


def _resolve_env_var(provider: str, env_override: str | None) -> str:
    if env_override:
        return env_override
    env_var = ENV_BY_PROVIDER.get(provider)
    if not env_var:
        raise RuntimeError(
            f"Unknown provider '{provider}'. Use --env-var or extend ENV_BY_PROVIDER."
        )
    return env_var


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Launch an AI agent with API keys pulled from gopass."
    )
    parser.add_argument("--provider", required=True, help="Provider name (deepseek).")
    parser.add_argument(
        "--gopass-prefix",
        default=os.environ.get("MENTCI_GOPASS_PREFIX", DEFAULT_GOPASS_PREFIX),
        help="Entry prefix for gopass (default: mentci/ai).",
    )
    parser.add_argument(
        "--entry",
        help="Explicit gopass entry override (default: <prefix>/<provider>/api-key).",
    )
    parser.add_argument(
        "--env-var",
        help="Explicit environment variable name for the API key.",
    )
    parser.add_argument(
        "command",
        nargs=argparse.REMAINDER,
        help="Command to execute (default: opencode).",
    )
    args = parser.parse_args()

    entry = _resolve_entry(args.gopass_prefix, args.provider, args.entry)
    env_var = _resolve_env_var(args.provider, args.env_var)
    api_key = _read_gopass_secret(entry)

    cmd = args.command if args.command else DEFAULT_COMMAND
    if cmd and cmd[0] == "--":
        cmd = cmd[1:]
    if not cmd:
        cmd = DEFAULT_COMMAND

    env = os.environ.copy()
    env[env_var] = api_key
    return subprocess.call(cmd, env=env)


if __name__ == "__main__":
    raise SystemExit(main())
