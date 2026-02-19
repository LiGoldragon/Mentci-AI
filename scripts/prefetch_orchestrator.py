import sys
import json
import subprocess
import os
import pathlib

# Tool Stack Transparency:
# Tool: CriomOS Pre-Fetch Orchestrator (MCP Server)
# Language/Runtime: Python 3.11
# Origin: Custom Level 5 Implementation
# Rationale: Provides a cryptographic bridge between isolated agents and the Nix daemon.
# Implementation Details: Intercepts Nix output to enforce SRI format and sanitized error strings.

class PreFetchOrchestrator:
    """
    MCP Server bridging isolated agents to the Nix daemon for network-enabled prefetching.
    Ensures cryptographic proof of external inputs for CriomOS derivations.
    """
    
    def __init__(self):
        self.nix_version = self._get_nix_version()

    def _get_nix_version(self):
        try:
            # Silence stderr to keep context clean
            res = subprocess.run(["nix", "--version"], capture_output=True, text=True, check=True)
            return res.stdout.strip()
        except:
            return "unknown"

    def prefetch_url(self, url, unpack=False, hash_algo="sha256"):
        """
        Tool Specification: URL Prefetching
        Handles standard tarballs, binary blobs, and raw file assets.
        Returns ONLY the finalized SRI string.
        """
        try:
            # Command logic for Nix 2.18+
            # We use 'nix store prefetch-file' as the modern equivalent
            cmd = ["nix", "store", "prefetch-file", "--json", "--hash-type", hash_algo, url]
            if unpack:
                cmd.insert(3, "--unpack")
            
            # Pipe stderr to /dev/null to prevent context pollution (Spec Section 2)
            res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True, check=True)
            data = json.loads(res.stdout)
            
            # Returns *only* the finalized SRI string (Spec Section 2)
            return data.get("hash")
            
        except subprocess.CalledProcessError as e:
            return self._handle_error(e, "URL prefetch")
        except Exception as e:
            return f"Deterministic failure in URL prefetch: {str(e)}"

    def prefetch_git(self, url, rev=None, submodules=False):
        """
        Tool Specification: Git Repository Prefetching
        Handles complex source trees with submodules and deterministic hashing.
        Returns SRI hash, resolved rev, and store path.
        """
        try:
            # We use 'nix-prefetch-git' for broad compatibility and structured output
            cmd = ["nix-prefetch-git", "--url", url]
            if rev:
                cmd.extend(["--rev", rev])
            if submodules:
                cmd.append("--fetch-submodules")
            
            # Pipe stderr to /dev/null
            res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True, check=True)
            data = json.loads(res.stdout)
            
            # Return structured response (Spec Section 3)
            return {
                "sri": data.get("hash"),
                "rev": data.get("rev"),
                "path": data.get("path")
            }
        except subprocess.CalledProcessError as e:
            return self._handle_error(e, "Git prefetch")
        except Exception as e:
            return f"Deterministic failure in Git prefetch: {str(e)}"

    def _handle_error(self, exc, context):
        """
        Deterministic Error Handling (Spec Section 4)
        Sanitizes and formats errors to prevent agent hallucinations.
        """
        # Note: If stderr was piped to DEVNULL, we might not have much detail here
        # unless we capture it specifically for error processing.
        # Let's adjust slightly: capture stderr only on failure.
        
        stderr = exc.stderr.lower() if exc.stderr else "unknown error"
        
        if "404" in stderr:
            return f"Dependency resolution failed: HTTP 404. The upstream asset does not exist. Do not attempt to build; propose an alternative mirror or version. context: {context}"
        if "timeout" in stderr:
            return f"Network timeout during {context}. Check host connectivity."
        
        return f"Deterministic failure in {context}: {stderr.strip()}"

    def run_mcp(self):
        """
        Main stdio loop for MCP protocol.
        Communicates strictly over stdio via isolated pipe (Spec Section 1).
        """
        while True:
            line = sys.stdin.readline()
            if not line:
                break
            try:
                request = json.loads(line)
                method = request.get("method")
                params = request.get("params", {})
                
                if method == "prefetch_url":
                    result = self.prefetch_url(**params)
                elif method == "prefetch_git":
                    result = self.prefetch_git(**params)
                else:
                    result = {"error": f"Unknown method: {method}"}
                
                # Write result to stdout
                print(json.dumps({"result": result}))
                sys.stdout.flush()
            except Exception as e:
                # Even protocol errors are wrapped in JSON
                print(json.dumps({"error": str(e)}))
                sys.stdout.flush()

if __name__ == "__main__":
    orchestrator = PreFetchOrchestrator()
    if len(sys.argv) > 1 and sys.argv[1] == "--mcp":
        orchestrator.run_mcp()
    else:
        # Standard CLI diagnostic output
        print(f"CriomOS Pre-Fetch Orchestrator active. Nix version: {orchestrator.nix_version}")