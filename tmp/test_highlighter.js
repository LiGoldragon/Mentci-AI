const hljs = require('/home/li/git/Mentci-AI/.pi/npm/node_modules/highlight.js/lib/index.js');

const COLOR_MAP = {
  'keyword': '\x1b[35m',
  'string': '\x1b[32m',
  'comment': '\x1b[90m',
  'function': '\x1b[34m',
  'title': '\x1b[34m',
  'params': '\x1b[37m',
  'number': '\x1b[33m',
  'built_in': '\x1b[36m',
  'attr': '\x1b[36m',
  'symbol': '\x1b[33m',
  'punctuation': '\x1b[37m',
  'meta': '\x1b[90m',
  'operator': '\x1b[37m',
  'variable': '\x1b[37m',
  'type': '\x1b[36m'
};

function highlightCode(code, lang) {
  if (!code) return "";
  try {
    const res = hljs.highlight(code, { language: lang });
    let html = res.value;
    if (!html) return code;
    html = html.replace(/<span class="hljs-([^"]+)">/g, (_match, group) => {
        const baseClass = group.split(' ')[0];
        return COLOR_MAP[baseClass] || '\x1b[37m';
    });
    html = html.replace(/<\/span>/g, '\x1b[0m');
    return html.replace(/&quot;/g, '"').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&');
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

const intentObj = { project: "mentci", language: "rust", query: "(function_item) @fn" };
const edn = `;; Logical Query Intent\n${stringifyEDN(intentObj)}`;

console.log("--- HIGHLIGHTER TEST ---");
const result = highlightCode(edn, "clojure");
console.log(result);
if (result.includes("<span")) {
    console.log("FAIL: Unreplaced spans found!");
} else {
    console.log("SUCCESS: All spans replaced with ANSI.");
}
console.log("------------------------");
