# Pi Extensions and Dependencies Management

## 1. Current Situation
Historically, `pi` configuration was tracked via `.pi/extensions.edn` (and `.pi/settings.json`), but vendored npm artifacts like `package.json`, `package-lock.json`, and occasionally `.gitignore` inside `.pi/npm` were slipping into the `jj` repository history. 
This led to the repository tracking non-nix dynamically generated dependency artifacts, dirtying the store, and producing unnecessarily large git/jj commits containing node module states.

## 2. Review of Extensions
The repository previously had the following `npm` extensions enabled via `pi`:
- **`npm:@aliou/pi-linkup`**: Retained. Critical dependency for web search/Linkup integrations required by the agent architecture.
- **`npm:@juanibiapina/pi-plan`**: Retained. Provides planning capabilities.
- **`npm:@oh-my-pi/subagents`**: Retained. Essential core for launching sub-agents.
- **`npm:@oh-my-pi/lsp`**: Retained. Used for code-aware tooling and LSP logic.
- **`npm:mcporter`**: **Removed**. Not a core requirement; can be dropped to reduce dependency overhead.
- **`npm:@oh-my-pi/omp-stats`**: **Removed**. Utility to report stats, mostly unnecessary for core functionality.

## 3. Implemented Fixes
- **Nix Dependency Update**: The core `pi-mono` flake input has been updated via `nix flake update pi-mono`.
- **Extension Removal**: Removed `mcporter` and `omp-stats` from `.pi/extensions.edn` and `.pi/settings.json`.
- **Clean Untracking**: Used `jj file untrack` to forcibly stop tracking `.pi/npm/package.json`, `.pi/npm/package-lock.json`, and `.pi/npm/.gitignore`.
- **Gitignore Modernization**: Replaced the overly specific `.pi/npm/node_modules/` rule in `.gitignore` with a broader `/.pi/npm/` wildcard. This delegates the entire `.pi/npm` folder to the local machine’s ephemeral cache managed directly by the `pi` CLI.
- **Agent Skill Update**: Modified `.pi/skills/independent-developer/SKILL.md` directly. The independent developer is now structurally bound to actively ask questions after delivering a report, ensuring higher efficiency and continuous alignment with the human operator.

## 4. Streamlining Future State Management
- **Rule of Thumb**: Non-nix pi dependencies are strictly controlled by the declarative list in `.pi/extensions.edn` and `.pi/settings.json`. 
- Developers should only change the `.edn`/`.json` arrays and let `pi update` handle the internal fetch cycle. 
- Because `/.pi/npm/` is now entirely ignored by `jj` (and Git), updating dependencies won't ever dirty the VCS state again.
- **Updating**: Simply running `pi update` safely synchronizes the ignored `.pi/npm` directory to the required state.

### Questions for the Operator
1. Are there any other specific `.pi` TS extensions (like `mentci-stt-ui.ts` or `mentci-workspace.ts`) that you feel might need a refactor or pruning?
2. Do you want me to merge this setup into `main` and complete the session, or is there another component under `.pi/` you'd like me to focus on before wrapping up?