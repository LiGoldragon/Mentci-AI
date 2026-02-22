#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[cheshire.core :as json]
         '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl enable!]])

(enable!)

(def JailMainInput
  [:map])

(def ProvisionInput
  [:map
   [:name :string]
   [:inputType :string]
   [:sourcePath :string]
   [:srcPath [:maybe :string]]
   [:inputsRoot :string]
   [:isImpure :boolean]])

(def KeywordizeKeysInput
  [:map
   [:data :map]])

(def FindJailConfigInput
  [:map
   [:data :any]])

(def ComponentEntry
  [:map
   [:id :string]
   [:path :string]
   [:interfaces [:vector :string]]
   [:status [:enum :active :deprecated :experimental]]])

(def ComponentIndex
  [:map
   [:version :int]
   [:components [:vector ComponentEntry]]])

(def LoadComponentIndexInput
  [:map
   [:indexPath :string]])

(def ResolveComponentIndexInput
  [:map
   [:index ComponentIndex]])

(def WriteComponentRegistryInput
  [:map
   [:registry :map]
   [:outputsPath :string]
   [:registryPath [:maybe :string]]])

(defprotocol LauncherOps
  (validate-config-for [this config])
  (provision-input-for [this input])
  (keywordize-keys-for [this input])
  (find-jail-config-for [this input])
  (load-component-index-for [this input])
  (resolve-component-index-for [this input])
  (write-component-registry-for [this input]))

(defrecord DefaultLauncher [])

(impl DefaultLauncher LauncherOps validate-config-for types/JailConfig :any
  [this config]
  config)

(impl DefaultLauncher LauncherOps provision-input-for ProvisionInput :any
  [this input]
  (let [{:keys [name inputType sourcePath srcPath inputsRoot isImpure]} input
        target-root (io/file inputsRoot)
        target-path (io/file target-root name)
        final-source (or srcPath sourcePath)]
    (.mkdirs target-root)
    (if isImpure
      (if (and (.exists target-path)
               (.isDirectory target-path)
               (not (java.nio.file.Files/isSymbolicLink (.toPath target-path))))
        (println (str "Skipping " name " (Mutable): Already exists as a directory."))
        (do
          (when (.exists target-path)
            (io/delete-file target-path))
          (println (str "Materializing " name " [" inputType "] (Mutable)..."))
          (let [{:keys [exit err]} (sh "rsync" "-aL" "--chmod=u+w" (str final-source "/") (.getPath target-path))]
            (when-not (zero? exit)
              (println (str "Error materializing " name ": " err))))))
      (do
        (when (.exists target-path)
          (if (and (.isDirectory target-path) (not (java.nio.file.Files/isSymbolicLink (.toPath target-path))))
            (println (str "Warning: " name " is a mutable directory. Skipping symlink in Pure mode."))
            (io/delete-file target-path)))
        (when-not (.exists target-path)
          (try
            (java.nio.file.Files/createSymbolicLink
              (.toPath target-path)
              (.toPath (io/file final-source))
              (into-array java.nio.file.attribute.FileAttribute []))
            (catch Exception e
              (binding [*out* *err*]
                (println (str "Error linking " name ": " (.getMessage e)))))))))))

(impl DefaultLauncher LauncherOps keywordize-keys-for KeywordizeKeysInput :map
  [this input]
  (let [data (:data input)]
    (into {} (for [[k v] data] [(keyword k) (if (map? v) (keywordize-keys-for this {:data v}) v)]))))

(impl DefaultLauncher LauncherOps find-jail-config-for FindJailConfigInput [:maybe :map]
  [this input]
  (let [data (:data input)]
    (cond
      (and (map? data) (get data "inputsPath")) data
      (and (map? data) (get data :inputsPath)) data
      (map? data) (some #(find-jail-config-for this {:data %}) (vals data))
      (string? data) (try (find-jail-config-for this {:data (json/parse-string data)}) (catch Exception _ nil))
      (coll? data) (some #(find-jail-config-for this {:data %}) data)
      :else nil)))

(impl DefaultLauncher LauncherOps load-component-index-for LoadComponentIndexInput ComponentIndex
  [this input]
  (let [index-file (io/file (:indexPath input))]
    (when-not (.exists index-file)
      (binding [*out* *err*]
        (println (str "Component index not found: " (:indexPath input)))
        (System/exit 1)))
    (edn/read-string (slurp index-file))))

(impl DefaultLauncher LauncherOps resolve-component-index-for ResolveComponentIndexInput :map
  [this input]
  (let [components (:components (:index input))
        ids (map :id components)
        dup-ids (->> ids frequencies (filter (fn [[_ c]] (> c 1))) (map first) vec)]
    (when (seq dup-ids)
      (binding [*out* *err*]
        (println (str "Duplicate component ids in Components/index.edn: " (str/join ", " dup-ids)))
        (System/exit 1)))
    {:version (:version (:index input))
     :components
     (reduce (fn [acc component]
               (let [path (:path component)
                     path-file (io/file path)]
                 (when-not (.exists path-file)
                   (binding [*out* *err*]
                     (println (str "Component path missing for id " (:id component) ": " path))
                     (System/exit 1)))
                 (assoc acc
                        (:id component)
                        {:id (:id component)
                         :path path
                         :interfaces (:interfaces component)
                         :status (:status component)})))
             {}
             components)}))

(impl DefaultLauncher LauncherOps write-component-registry-for WriteComponentRegistryInput :string
  [this input]
  (let [out-path (or (:registryPath input)
                     (str (:outputsPath input) "/component_registry.json"))
        out-file (io/file out-path)]
    (io/make-parents out-file)
    (spit out-file (json/generate-string (:registry input) {:pretty true}))
    out-path))

(def default-launcher (->DefaultLauncher))

(defn* validate-config [:=> [:cat types/JailConfig] :any] [config]
  (validate-config-for default-launcher config))

(defn* provision-input [:=> [:cat ProvisionInput] :any] [input]
  (provision-input-for default-launcher input))

(defn* keywordize-keys [:=> [:cat KeywordizeKeysInput] :map] [input]
  (keywordize-keys-for default-launcher input))

(defn* find-jail-config [:=> [:cat FindJailConfigInput] [:maybe :map]] [input]
  (find-jail-config-for default-launcher input))

(defn* load-component-index [:=> [:cat LoadComponentIndexInput] ComponentIndex] [input]
  (load-component-index-for default-launcher input))

(defn* resolve-component-index [:=> [:cat ResolveComponentIndexInput] :map] [input]
  (resolve-component-index-for default-launcher input))

(defn* write-component-registry! [:=> [:cat WriteComponentRegistryInput] :string] [input]
  (write-component-registry-for default-launcher input))

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
            inputs-path (get config :inputsPath "Inputs")
            inputs-root (io/file inputs-path)
            input-manifest (get config :inputManifest {})
            outputs-path (get config :outputsPath "Outputs")
            policy-path (get config :policyPath nil)
            component-index-path (get config :componentIndexPath "Components/index.edn")
            component-registry-path (get config :componentRegistryPath nil)
            mentci-mode (System/getenv "MENTCI_MODE")
            is-impure (or (= mentci-mode "ADMIN")
                          (.exists (io/file "/usr/local"))
                          (System/getenv "NIX_SHELL_PRESERVE_PROMPT"))
            mode (if is-impure "IMPURE" "PURE")
            ro-indicator (if is-impure "RW (Admin)" "RO (Pure)")]
        (println (format "Running in %s mode (%s)" mode ro-indicator))
        (println (format "Outputs root: %s" outputs-path))
        (when policy-path
          (println (format "Jail policy file (read-only): %s" policy-path)))
        (let [component-index (load-component-index {:indexPath component-index-path})
              component-registry (resolve-component-index {:index component-index})
              written-path (write-component-registry! {:registry component-registry
                                                       :outputsPath outputs-path
                                                       :registryPath component-registry-path})]
          (println (format "Component registry loaded from: %s" component-index-path))
          (println (format "Component registry exported to: %s" written-path)))
        ;; Load Whitelist
        (let [whitelist-path (io/file "Core/agent-inputs.edn")
              whitelist (if (.exists whitelist-path)
                          (-> (slurp whitelist-path) edn/read-string :whitelist)
                          #{})]
          (println (format "Mounting Inputs from whitelist: %s" whitelist))
          (println "Launching Nix Jail...")
          (doseq [[item-name item-spec] input-manifest]
            (let [name-str (clojure.core/name item-name)
                  source-path (:sourcePath item-spec)
                  src-path (:srcPath item-spec)
                  input-type (:inputType item-spec "unknown")]
              (if (contains? whitelist name-str)
                (do
                  (println (str "Mounting " name-str " [" input-type "]..."))
                  (provision-input {:name name-str
                                    :inputType input-type
                                    :sourcePath source-path
                                    :srcPath src-path
                                    :inputsRoot (.getPath inputs-root)
                                    :isImpure is-impure}))
                (println (str "Skipping " name-str " (Not in whitelist)"))))))
        (println "Jail Launcher Complete.")
        (let [context-file (io/file "Library/guides/Level5-Ai-Coding.md")]
          (when (.exists context-file)
            (println "\n--- Level 5 Programming Context ---\n")
            (println (slurp context-file))
            (println "\n-----------------------------------\n")))
        (println "Welcome to the Mentci-AI Jail.")))))

(main {})
