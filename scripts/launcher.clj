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

(defn validate-config [config]
  (when-not (m/validate types/JailConfig config)
    (throw (ex-info "Invalid Jail Configuration"
                    {:errors (me/humanize (m/explain types/JailConfig config))}))))

(defn provision-input [name category source-path inputs-root is-impure]
  (let [target-dir (io/file inputs-root category)
        target-path (io/file target-dir name)]
    (.mkdirs target-dir)
    
    (if is-impure
      ;; Impure Mode: Mutable Copy
      (if (and (.exists target-path) 
               (.isDirectory target-path) 
               (not (java.nio.file.Files/isSymbolicLink (.toPath target-path))))
        ;; It's already a real directory (user managed?), skip
        (println (str "Skipping " name " (Mutable): Already exists as a directory."))
        
        ;; Else, it's either missing or a symlink (from previous pure run). Replace it.
        (do
          (when (.exists target-path)
            (io/delete-file target-path)) ;; Deletes symlink
          (println (str "Materializing " name " (Mutable)..."))
          (let [{:keys [exit err]} (sh "rsync" "-aL" "--chmod=u+w" (str source-path "/") (.getPath target-path))]
            (when-not (zero? exit)
              (println (str "Error materializing " name ": " err))))))
      
      ;; Pure Mode: Symlink (Immutable)
      (do
        ;; Clean up if it exists
        (when (.exists target-path)
          (if (and (.isDirectory target-path) (not (java.nio.file.Files/isSymbolicLink (.toPath target-path))))
            ;; If it was a directory (from impure run), warn and skip? Or delete?
            ;; Safety: Warn and skip. Pure jail shouldn't destroy user work.
            (println (str "Warning: " name " is a mutable directory. Skipping symlink in Pure mode."))
            
            ;; Else delete (it's a symlink or file)
            (io/delete-file target-path)))
            
        (when-not (.exists target-path)
          (try
            (java.nio.file.Files/createSymbolicLink 
              (.toPath target-path) 
              (.toPath (io/file source-path))
              (into-array java.nio.file.attribute.FileAttribute []))
            (catch Exception e
              (binding [*out* *err*]
                (println (str "Error linking " name ": " (.getMessage e)))))))))))

(defn keywordize-keys [m]
  (into {} (for [[k v] m] [(keyword k) (if (map? v) (keywordize-keys v) v)])))

(defn find-jail-config [data]
  (cond
    (and (map? data) (get data "inputsPath")) data
    (and (map? data) (get data :inputsPath)) data
    (map? data) (some find-jail-config (vals data))
    (string? data) (try (find-jail-config (json/parse-string data)) (catch Exception _ nil))
    (coll? data) (some find-jail-config data)
    :else nil))

(defn main []
  (println "Initializing Mentci-AI Level 5 Jail Environment (Clojure/Babashka)...")
  (let [attrs-path (or (System/getenv "NIX_ATTRS_JSON_FILE") ".attrs.json")
        attrs-file (io/file attrs-path)
        full-config (if (.exists attrs-file)
                      (json/parse-string (slurp attrs-file))
                      (when-let [env-val (System/getenv "jailConfig")]
                        (json/parse-string env-val)))
                    config-raw (find-jail-config full-config)]
            (if-not config-raw
              (do (println "Error: Configuration not found.") (System/exit 1))
              (let [config (keywordize-keys config-raw)
                    _ (validate-config config)
                    inputs-path (get config :inputsPath "inputs")
                    inputs-root (io/file inputs-path)
                    input-manifest (get config :inputManifest {})
                    mentci-mode (System/getenv "MENTCI_MODE")
                    is-impure (or (= mentci-mode "ADMIN")
                                  (.exists (io/file "/usr/local")) 
                                  (System/getenv "NIX_SHELL_PRESERVE_PROMPT"))
                    mode (if is-impure "IMPURE" "PURE")
                    ro-indicator (if is-impure "RW (Admin)" "RO (Pure)")]
                (println (format "Running in %s mode (%s)" mode ro-indicator))
                ;; Load Whitelist
        (let [whitelist-path (io/file "agent-inputs.edn")
              whitelist (if (.exists whitelist-path)
                          (-> (slurp whitelist-path) edn/read-string :whitelist)
                          #{})]
          (println (format "Mounting inputs from whitelist: %s" whitelist))
          
          (println "Launching Nix Jail...")
          (doseq [[category items] input-manifest]
            (doseq [[item-name path] items]
              (let [name-str (clojure.core/name item-name)]
                (if (contains? whitelist name-str)
                  (do
                    (println (str "Mounting " name-str "..."))
                    (provision-input name-str (clojure.core/name category) path inputs-root is-impure))
                  (println (str "Skipping " name-str " (Not in whitelist)")))))))
        
        (println "Jail Launcher Complete.")
        (let [context-file (io/file "Level5-Ai-Coding.md")]
          (when (.exists context-file)
            (println "\n--- Level 5 Programming Context ---\n")
            (println (slurp context-file))
            (println "\n-----------------------------------\n")))
        (println "Welcome to the Mentci-AI Jail.")))))

(main)
