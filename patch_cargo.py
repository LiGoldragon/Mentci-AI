import sys

with open("Components/mentci-mcp/Cargo.toml", "r") as f:
    text = f.read()

text = text.replace('tree-sitter = "0.22"', 'tree-sitter = "0.26"')
text = text.replace('tree-sitter-rust = "0.22"', 'tree-sitter-rust = "0.24"')

with open("Components/mentci-mcp/Cargo.toml", "w") as f:
    f.write(text)
