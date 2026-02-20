#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[cheshire.core :as json]
         '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def JailMainInput
  [:map])

(def ProvisionInput
  [:map
   [:name :string]
   [:inputType :string]
   [:sourcePath :string]
   [:inputsRoot :string]
   [:isImpure :boolean]])

(def KeywordizeKeysInput
  [:map
   [:data :map]])

(def FindJailConfigInput
  [:map
   [:data :any]])

(defn* validate-config [:=> [:cat types/JailConfig] :any] [config]
  config)

(defn* provision-input [:=> [:cat ProvisionInput] :any] [input]
  (let [{:keys [name inputType sourcePath inputsRoot isImpure]} input
        target-root (io/file inputsRoot)
        target-path (io/file target-root name)]
    (.mkdirs target-root)
    (if isImpure
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
          (println (str "Materializing " name " [" inputType "] (Mutable)..."))
          (let [{:keys [exit err]} (sh "rsync" "-aL" "--chmod=u+w" (str sourcePath "/") (.getPath target-path))]
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
              (.toPath (io/file sourcePath))
              (into-array java.nio.file.attribute.FileAttribute []))
            (catch Exception e
              (binding [*out* *err*]
                (println (str "Error linking " name ": " (.getMessage e)))))))))))

(defn* keywordize-keys [:=> [:cat KeywordizeKeysInput] :map] [input]
  (let [data (:data input)]
    (into {} (for [[k v] data] [(keyword k) (if (map? v) (keywordize-keys {:data v}) v)]))))

(defn* find-jail-config [:=> [:cat FindJailConfigInput] [:maybe :map]] [input]
  (let [data (:data input)]
    (cond
      (and (map? data) (get data "inputsPath")) data
      (and (map? data) (get data :inputsPath)) data
      (map? data) (some #(find-jail-config {:data %}) (vals data))
      (string? data) (try (find-jail-config {:data (json/parse-string data)}) (catch Exception _ nil))
      (coll? data) (some #(find-jail-config {:data %}) data)
      :else nil)))

(defn* main [:=> [:cat JailMainInput] :any] [_]
  (println "Initializing Mentci-AI Level 5 Jail Environment (Clojure/Babashka)...")
  (let [attrs-path (or (System/getenv "NIX_ATTRS_JSON_FILE") ".attrs.json")
        attrs-file (io/file attrs-path)
        full-config (if (.exists attrs-file)
                      (json/parse-string (slurp attrs-file))
                      (when-let [env-val (System/getenv "jailConfig")]
                        (json/parse-string env-val)))
        config-raw (find-jail-config {:data full-config})]
    (if-not config-raw
      (do (println "Error: Configuration not found.") (System/exit 1))
      (let [config (keywordize-keys {:data config-raw})
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
          (doseq [[item-name item-spec] input-manifest]
            (let [name-str (clojure.core/name item-name)
                  source-path (:sourcePath item-spec)
                  input-type (:inputType item-spec "unknown")]
              (if (contains? whitelist name-str)
                (do
                  (println (str "Mounting " name-str " [" input-type "]..."))
                  (provision-input {:name name-str
                                    :inputType input-type
                                    :sourcePath source-path
                                    :inputsRoot (.getPath inputs-root)
                                    :isImpure is-impure}))
                (println (str "Skipping " name-str " (Not in whitelist)"))))))
        (println "Jail Launcher Complete.")
        (let [context-file (io/file "docs/guides/Level5-Ai-Coding.md")]
          (when (.exists context-file)
            (println "\n--- Level 5 Programming Context ---\n")
            (println (slurp context-file))
            (println "\n-----------------------------------\n")))
        (println "Welcome to the Mentci-AI Jail.")))))

(main {})
