#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[cheshire.core :as json]
         '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me])

;; Load local types
(load-file (str (.getParent (io/file *file*)) "/types.clj"))

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Typing: Malli
;; Rationale: High-level data manipulation for Nix structured attributes and filesystem orchestration.

(defn validate-config [config]
  (when-not (m/validate :types/JailConfig config)
    (throw (ex-info "Invalid Jail Configuration"
                    {:errors (me/humanize (m/explain :types/JailConfig config))}))))

(defn link-input [name category source-path inputs-root]
  (let [target-dir (io/file inputs-root category)
        target-path (io/file target-dir name)]
    (.mkdirs target-dir)
    ;; Remove existing symlink or directory
    (when (.exists target-path)
      (if (and (.isDirectory target-path) (not (java.nio.file.Files/isSymbolicLink (.toPath target-path))))
        (run! #(io/delete-file % true) (reverse (file-seq target-path)))
        (io/delete-file target-path)))
    ;; Create symlink
    (try
      (java.nio.file.Files/createSymbolicLink 
        (.toPath target-path) 
        (.toPath (io/file source-path))
        (into-array java.nio.file.attribute.FileAttribute []))
      (catch Exception e
        (binding [*out* *err*]
          (println (str "Error linking " name ": " (.getMessage e))))))))

(defn keywordize-keys [m]
  (into {} (for [[k v] m] [(keyword k) (if (map? v) (keywordize-keys v) v)])))

(defn main []
  (println "Initializing Mentci-AI Level 5 Jail Environment (Clojure/Babashka)...")
  
  ;; 1. Load Structured Attributes from Nix
  (let [attrs-file (io/file ".attrs.json")
        config (if (.exists attrs-file)
                 (get (json/parse-string (slurp attrs-file)) "jailConfig")
                 (when-let [config-env (System/getenv "jailConfig")]
                   (json/parse-string config-env)))]
    
    (if-not config
      (do (binding [*out* *err*] (println "Error: .attrs.json not found and jailConfig not in env."))
          (System/exit 1))
      
      (let [config (keywordize-keys config)] ; Ensure keys are keywords for Malli
        (validate-config config)
        (let [inputs-path-str (get config :inputsPath "inputs")
              inputs-root (io/file inputs-path-str)
              input-manifest (get config :inputManifest {})]
        
        ;; 2. Determine Purity Mode
        (let [is-impure (or (.exists (io/file "/usr/local")) (System/getenv "NIX_SHELL_PRESERVE_PROMPT"))
              mode (if is-impure "IMPURE" "PURE")
              ro-indicator (if is-impure "RW (Impure)" "RO (Pure)")]
          (println (format "Running in %s mode (%s)" mode ro-indicator)))

        ;; 3. Jail Launcher Logic
        (println "Launching Nix Jail...")
        (println "Organizing inputs from data manifest...")
        
        (doseq [[category items] input-manifest]
          (doseq [[name path] items]
            (link-input name category path inputs-root)))

        (println "Jail Launcher Complete.")
        (println (str "Inputs available in '" (.getAbsolutePath inputs-root) "':"))
        
        ;; 4. Display Context
        (let [context-file (io/file "Level5-Ai-Coding.md")]
          (when (.exists context-file)
            (println "\n--- Level 5 Programming Context ---\n")
            (println (slurp context-file))
            (println "\n-----------------------------------\n")))

        (println "Welcome to the Mentci-AI Jail.")))))

(main)
