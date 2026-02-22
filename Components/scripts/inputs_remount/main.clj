#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[cheshire.core :as json])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

(enable!)

(def RemountArgs
  [:map
   [:attrsPath :string]
   [:inputsPath [:maybe :string]]
   [:whitelistPath :string]])

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def FailInput
  [:map
   [:message :string]])

(def KeywordizeKeysInput
  [:map
   [:data :map]])

(def FindJailConfigInput
  [:map
   [:data :any]])

(def ReadConfigInput
  [:map
   [:attrsPath :string]])

(def WhitelistInput
  [:map
   [:whitelistPath :string]])

(def DeletePathInput
  [:map
   [:path :string]])

(def RemountInput
  [:map
   [:name :string]
   [:sourcePath :string]
   [:targetPath :string]])

(def StripWriteInput
  [:map
   [:path :string]])

(def Input
  [:map
   [:args [:vector :string]]])

(def owner-write java.nio.file.attribute.PosixFilePermission/OWNER_WRITE)
(def group-write java.nio.file.attribute.PosixFilePermission/GROUP_WRITE)
(def others-write java.nio.file.attribute.PosixFilePermission/OTHERS_WRITE)

(defprotocol InputsRemountOps
  (fail-for [this input])
  (parse-args-for [this input])
  (keywordize-keys-for [this input])
  (find-jail-config-for [this input])
  (read-jail-config-for [this input])
  (read-whitelist-for [this input])
  (delete-path-for [this input])
  (remount-input-for [this input])
  (strip-write-permissions-for [this input]))

(defrecord DefaultInputsRemount [])

(impl DefaultInputsRemount InputsRemountOps fail-for FailInput :any
  [this input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(impl DefaultInputsRemount InputsRemountOps parse-args-for ParseArgsInput RemountArgs
  [this input]
  (loop [remaining (:args input)
         options {:attrsPath ".attrs.json"
                  :inputsPath nil
                  :whitelistPath "Core/agent-inputs.edn"}]
    (if (empty? remaining)
      options
      (let [[arg & _more] remaining]
        (cond
          (= arg "--attrs-path")
          (recur (nnext remaining) (assoc options :attrsPath (second remaining)))

          (= arg "--inputs-path")
          (recur (nnext remaining) (assoc options :inputsPath (second remaining)))

          (= arg "--whitelist-path")
          (recur (nnext remaining) (assoc options :whitelistPath (second remaining)))

          (= arg "--help")
          (do
            (println "Usage: bb Components/scripts/inputs_remount/main.clj [--attrs-path <path>] [--inputs-path <path>] [--whitelist-path <path>]")
            (System/exit 0))

          :else
          (fail-for this {:message (str "Unknown argument: " arg)}))))))

(impl DefaultInputsRemount InputsRemountOps keywordize-keys-for KeywordizeKeysInput :map
  [this input]
  (let [data (:data input)]
    (into {}
          (for [[k v] data]
            [(keyword k)
             (if (map? v)
               (keywordize-keys-for this {:data v})
               v)]))))

(impl DefaultInputsRemount InputsRemountOps find-jail-config-for FindJailConfigInput [:maybe :map]
  [this input]
  (let [data (:data input)]
    (cond
      (and (map? data) (get data "inputsPath")) data
      (and (map? data) (get data :inputsPath)) data
      (map? data) (some #(find-jail-config-for this {:data %}) (vals data))
      (string? data) (try (find-jail-config-for this {:data (json/parse-string data)}) (catch Exception _ nil))
      (coll? data) (some #(find-jail-config-for this {:data %}) data)
      :else nil)))

(impl DefaultInputsRemount InputsRemountOps read-jail-config-for ReadConfigInput types/JailConfig
  [this input]
  (let [attrs-file (io/file (:attrsPath input))
        full-config (if (.exists attrs-file)
                      (json/parse-string (slurp attrs-file))
                      (when-let [env-val (System/getenv "jailConfig")]
                        (json/parse-string env-val)))
        config-raw (find-jail-config-for this {:data full-config})]
    (when-not config-raw
      (fail-for this {:message "Configuration not found. Expected .attrs.json or jailConfig env var."}))
    (keywordize-keys-for this {:data config-raw})))

(impl DefaultInputsRemount InputsRemountOps read-whitelist-for WhitelistInput [:set :string]
  [this input]
  (let [whitelist-file (io/file (:whitelistPath input))]
    (if (.exists whitelist-file)
      (let [data (edn/read-string (slurp whitelist-file))]
        (or (:whitelist data) #{}))
      (fail-for this {:message (str "Whitelist file not found: " (:whitelistPath input))}))))

(impl DefaultInputsRemount InputsRemountOps delete-path-for DeletePathInput :any
  [this input]
  (let [path-str (:path input)
        path (.toPath (io/file path-str))]
    (when (java.nio.file.Files/exists path (into-array java.nio.file.LinkOption []))
      (if (java.nio.file.Files/isSymbolicLink path)
        (java.nio.file.Files/delete path)
        (let [stream (java.nio.file.Files/walk path (into-array java.nio.file.FileVisitOption []))]
          (try
            (doseq [p (reverse (vec (iterator-seq (.iterator stream))))]
              (java.nio.file.Files/deleteIfExists p))
            (finally
              (.close stream))))))))

(impl DefaultInputsRemount InputsRemountOps remount-input-for RemountInput :any
  [this input]
  (let [{:keys [name sourcePath targetPath]} input
        source-file (io/file sourcePath)
        target-file (io/file targetPath)]
    (when-not (.exists source-file)
      (fail-for this {:message (str "Source path for " name " does not exist: " sourcePath)}))
    (when-let [parent (.getParentFile target-file)]
      (.mkdirs parent))
    (delete-path-for this {:path targetPath})
    (java.nio.file.Files/createSymbolicLink
      (.toPath target-file)
      (.toPath source-file)
      (into-array java.nio.file.attribute.FileAttribute []))))

(impl DefaultInputsRemount InputsRemountOps strip-write-permissions-for StripWriteInput :map
  [this input]
  (let [root (.toPath (io/file (:path input)))]
    (if-not (java.nio.file.Files/exists root (into-array java.nio.file.LinkOption []))
      {:visited 0 :changed 0 :skipped 0}
      (let [stream (java.nio.file.Files/walk root (into-array java.nio.file.FileVisitOption []))]
        (try
          (reduce
            (fn [acc path]
              (if (java.nio.file.Files/isSymbolicLink path)
                (update acc :skipped inc)
                (try
                  (let [perms (java.nio.file.Files/getPosixFilePermissions
                                path
                                (into-array java.nio.file.LinkOption []))
                        new-perms (doto (java.util.HashSet. perms)
                                    (.remove owner-write)
                                    (.remove group-write)
                                    (.remove others-write))]
                    (if (= perms new-perms)
                      (update acc :visited inc)
                      (do
                        (java.nio.file.Files/setPosixFilePermissions path new-perms)
                        (-> acc
                            (update :visited inc)
                            (update :changed inc)))))
                  (catch UnsupportedOperationException _
                    (update acc :skipped inc))
                  (catch Exception _
                    (update acc :skipped inc)))))
            {:visited 0 :changed 0 :skipped 0}
            (iterator-seq (.iterator stream)))
          (finally
            (.close stream)))))))

(def default-inputs-remount (->DefaultInputsRemount))

;; Compatibility wrappers for existing tests/callers.
(defn* fail [:=> [:cat FailInput] :any] [input]
  (fail-for default-inputs-remount input))

(defn* parse-args [:=> [:cat ParseArgsInput] RemountArgs] [input]
  (parse-args-for default-inputs-remount input))

(defn* keywordize-keys [:=> [:cat KeywordizeKeysInput] :map] [input]
  (keywordize-keys-for default-inputs-remount input))

(defn* find-jail-config [:=> [:cat FindJailConfigInput] [:maybe :map]] [input]
  (find-jail-config-for default-inputs-remount input))

(defn* read-jail-config [:=> [:cat ReadConfigInput] types/JailConfig] [input]
  (read-jail-config-for default-inputs-remount input))

(defn* read-whitelist [:=> [:cat WhitelistInput] [:set :string]] [input]
  (read-whitelist-for default-inputs-remount input))

(defn* delete-path! [:=> [:cat DeletePathInput] :any] [input]
  (delete-path-for default-inputs-remount input))

(defn* remount-input! [:=> [:cat RemountInput] :any] [input]
  (remount-input-for default-inputs-remount input))

(defn* strip-write-permissions! [:=> [:cat StripWriteInput] :map] [input]
  (strip-write-permissions-for default-inputs-remount input))

(main Input
  [input]
  (let [args (parse-args {:args (:args input)})
        config (read-jail-config {:attrsPath (:attrsPath args)})
        whitelist (read-whitelist {:whitelistPath (:whitelistPath args)})
        inputs-path (or (:inputsPath args) (:inputsPath config) "Inputs")
        input-manifest (:inputManifest config)
        inputs-root (io/file inputs-path)]
    (.mkdirs inputs-root)
    (println (str "Refreshing Inputs mount at: " (.getPath inputs-root)))
    (println (str "Using whitelist: " whitelist))
    (doseq [[item-name item-spec] input-manifest]
      (let [name-str (name item-name)]
        (when (contains? whitelist name-str)
          (let [source-path (or (:srcPath item-spec) (:sourcePath item-spec))
                target-path (.getPath (io/file inputs-root name-str))]
            (println (str "Re-mounting " name-str " -> " source-path))
            (remount-input! {:name name-str
                             :sourcePath source-path
                             :targetPath target-path})
            (let [report (strip-write-permissions! {:path source-path})]
              (println (format "RO enforced for %s source tree (visited=%d changed=%d skipped=%d)"
                               name-str
                               (:visited report)
                               (:changed report)
                               (:skipped report))))))))
    (println "Inputs refresh complete.")))

(when (= *file* (System/getProperty "babashka.file"))
  (-main {:args (vec *command-line-args*)}))
