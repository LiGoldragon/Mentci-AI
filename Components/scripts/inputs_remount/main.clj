#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[cheshire.core :as json]
         '[clojure.set :as set])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* main enable!]])

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

(defn* fail [:=> [:cat FailInput] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* parse-args [:=> [:cat ParseArgsInput] RemountArgs] [input]
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
          (fail {:message (str "Unknown argument: " arg)}))))))

(defn* keywordize-keys [:=> [:cat KeywordizeKeysInput] :map] [input]
  (let [data (:data input)]
    (into {} (for [[k v] data]
               [(keyword k)
                (if (map? v)
                  (keywordize-keys {:data v})
                  v)]))))

(defn* find-jail-config [:=> [:cat FindJailConfigInput] [:maybe :map]] [input]
  (let [data (:data input)]
    (cond
      (and (map? data) (get data "inputsPath")) data
      (and (map? data) (get data :inputsPath)) data
      (map? data) (some #(find-jail-config {:data %}) (vals data))
      (string? data) (try (find-jail-config {:data (json/parse-string data)}) (catch Exception _ nil))
      (coll? data) (some #(find-jail-config {:data %}) data)
      :else nil)))

(defn* read-jail-config [:=> [:cat ReadConfigInput] types/JailConfig] [input]
  (let [attrs-file (io/file (:attrsPath input))
        full-config (if (.exists attrs-file)
                      (json/parse-string (slurp attrs-file))
                      (when-let [env-val (System/getenv "jailConfig")]
                        (json/parse-string env-val)))
        config-raw (find-jail-config {:data full-config})]
    (when-not config-raw
      (fail {:message "Configuration not found. Expected .attrs.json or jailConfig env var."}))
    (let [config (keywordize-keys {:data config-raw})]
      config)))

(defn* read-whitelist [:=> [:cat WhitelistInput] [:set :string]] [input]
  (let [whitelist-file (io/file (:whitelistPath input))]
    (if (.exists whitelist-file)
      (let [data (edn/read-string (slurp whitelist-file))]
        (or (:whitelist data) #{}))
      (fail {:message (str "Whitelist file not found: " (:whitelistPath input))}))))

(defn* delete-path! [:=> [:cat DeletePathInput] :any] [input]
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

(defn* remount-input! [:=> [:cat RemountInput] :any] [input]
  (let [{:keys [name sourcePath targetPath]} input
        source-file (io/file sourcePath)
        target-file (io/file targetPath)]
    (when-not (.exists source-file)
      (fail {:message (str "Source path for " name " does not exist: " sourcePath)}))
    (when-let [parent (.getParentFile target-file)]
      (.mkdirs parent))
    (delete-path! {:path targetPath})
    (java.nio.file.Files/createSymbolicLink
      (.toPath target-file)
      (.toPath source-file)
      (into-array java.nio.file.attribute.FileAttribute []))))

(defn* strip-write-permissions! [:=> [:cat StripWriteInput] :map] [input]
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
                    (update acc :skipped inc)))) )
            {:visited 0 :changed 0 :skipped 0}
            (iterator-seq (.iterator stream)))
          (finally
            (.close stream)))))))

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
