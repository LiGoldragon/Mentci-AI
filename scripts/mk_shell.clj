#!/usr/bin/env bb

(require '[clojure.java.io :as io]
         '[cheshire.core :as json]
         '[clojure.string :as str])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Pure Clojure implementation of Nix mkShell environment logic.
;; Rewriting all parts of the environment construction in Clojure.

(defn main []
  (let [spec-json (System/getenv "spec")
        spec (json/parse-string spec-json true)
        out (System/getenv "out")
        out-bin (io/file out "bin")
        setup-file (io/file out "setup")]
    
    (.mkdirs out-bin)
    (.mkdirs (io/file out)) ; Ensure out exists
    
    (let [shell-name (:name spec)
          packages (:packages spec)
          shell-hook (:shellHook spec)
          env (:env spec)
          
          ;; Construct PATH
          bin-paths (map #(str % "/bin") packages)
          path-str (str/join ":" bin-paths)
          
          ;; Generate setup script (Bash shim that invokes the logic)
          ;; In a Level 5 system, we want to override defaults.
          setup-content
          (str "# Mentci-AI Environment Setup (Clojure-Generated)\n"
               "export PATH=\"" path-str ":$PATH\"\n"
               (str/join "\n" (for [[k v] env] (str "export " (clojure.core/name k) "=\"" v "\"")))
               "\n"
               "# Run User Hook\n"
               shell-hook
               "\n")]
      
      (spit setup-file setup-content)
      (println (str "Generated shell setup for " shell-name " at " setup-file)))))

(main)
