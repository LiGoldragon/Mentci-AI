const hljs = require('/home/li/git/Mentci-AI/.pi/npm/node_modules/highlight.js/lib/index.js');

function highlightCode(code, lang) {
  if (!code) return "";
  try {
    const res = hljs.highlight(code, { language: lang });
    const highlighted = res.value;
    if (!highlighted) return code;
    return highlighted
      .replace(/<span class="hljs-keyword">/g, '\x1b[35m')
      .replace(/<span class="hljs-string">/g, '\x1b[32m')
      .replace(/<span class="hljs-comment">/g, '\x1b[90m')
      .replace(/<span class="hljs-function">/g, '\x1b[34m')
      .replace(/<span class="hljs-title( function_)?">/g, '\x1b[34m')
      .replace(/<span class="hljs-params">/g, '\x1b[37m')
      .replace(/<span class="hljs-number">/g, '\x1b[33m')
      .replace(/<span class="hljs-built_in">/g, '\x1b[36m')
      .replace(/<span class="hljs-attr">/g, '\x1b[36m')
      .replace(/<span class="hljs-symbol">/g, '\x1b[33m')
      .replace(/<span class="hljs-punctuation">/g, '\x1b[37m')
      .replace(/<span class="hljs-meta">/g, '\x1b[90m')
      .replace(/<\/span>/g, '\x1b[0m')
      .replace(/&quot;/g, '"')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&amp;/g, '&');
  } catch (e) {
    return code; 
  }
}

function stringifyEDN(obj) {
  if (obj === null || obj === undefined) return "nil";
  if (typeof obj === "string") return `"${obj.replace(/"/g, '\\"')}"`;
  if (typeof obj === "number") return obj.toString();
  if (typeof obj === "boolean") return obj.toString();
  if (Array.isArray(obj)) {
    return "[" + obj.map(stringifyEDN).join(" ") + "]";
  }
  if (typeof obj === "object") {
    const entries = Object.entries(obj)
      .map(([k, v]) => `:${k} ${stringifyEDN(v)}`)
      .join("\n  ");
    return `{\n  ${entries}\n}`;
  }
  return String(obj);
}

const args = { project: "mentci", language: "rust", query: "(function_item) @fn" };
const results = [{"file":"test.rs","line":1,"capture":"fn","text":"fn main() {}"}];
const intentObj = { project: args.project, language: args.language, query: args.query };

const edn = `;; Logical Query Intent\n${stringifyEDN(intentObj)}`;
console.log("--- RENDERING TEST ---");
console.log(highlightCode(edn, "clojure"));
console.log("----------------------");
