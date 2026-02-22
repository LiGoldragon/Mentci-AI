#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* main enable!]])

(enable!)

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def FindJailConfigInput
  [:map
   [:data :any]])

(def KeywordizeKeysInput
  [:map
   [:data :map]])

(def LoadWhitelistInput
  [:map
   [:path :string]])

(def EnsureLinkInput
  [:map
   [:targetPath :string]
   [:sourcePath :string]
   [:write? :boolean]
   [:replace? :boolean]])

(def Input
  [:map
   [:args [:vector :string]]])

(defn* fail! [:=> [:cat [:map [:message :string]]] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* parse-args [:=> [:cat ParseArgsInput] :map] [input]
  (loop [remaining (:args input)
         opts {:attrs ".attrs.json"
               :inputsRoot "Inputs"
               :mode "ro"
               :replace? false
               :write? false
               :whitelistPath "Core/agent-inputs.edn"}]
    (if (empty? remaining)
      opts
      (let [[arg & _] remaining]
        (cond
          (= arg "--attrs")
          (recur (nnext remaining) (assoc opts :attrs (second remaining)))

          (= arg "--inputs-root")
          (recur (nnext remaining) (assoc opts :inputsRoot (second remaining)))

          (= arg "--mode")
          (recur (nnext remaining) (assoc opts :mode (second remaining)))

          (= arg "--replace")
          (recur (next remaining) (assoc opts :replace? true))

          (= arg "--write")
          (recur (next remaining) (assoc opts :write? true))

          (= arg "--whitelist")
          (recur (nnext remaining) (assoc opts :whitelistPath (second remaining)))

          (= arg "--help")
          (do
            (println "Usage: bb Components/scripts/inputs_mounter/main.clj [--attrs .attrs.json] [--inputs-root Inputs] [--mode ro|rw] [--whitelist Core/agent-inputs.edn] [--replace] [--write]")
            (System/exit 0))

          :else
          (fail! {:message (str "Unknown argument: " arg)}))))))

(defn* keywordize-keys [:=> [:cat KeywordizeKeysInput] :map] [input]
  (let [data (:data input)]
    (into {}
          (for [[k v] data]
            [(keyword k)
             (cond
               (map? v) (keywordize-keys {:data v})
               (vector? v) (mapv (fn [i] (if (map? i) (keywordize-keys {:data i}) i)) v)
               :else v)]))))

(defn* find-jail-config [:=> [:cat FindJailConfigInput] [:maybe :map]] [input]
  (let [data (:data input)]
    (cond
      (and (map? data) (get data "inputManifest")) data
      (and (map? data) (get data :inputManifest)) data
      (map? data) (some #(find-jail-config {:data %}) (vals data))
      (string? data) (try (find-jail-config {:data (json/parse-string data)})
                          (catch Exception _ nil))
      (coll? data) (some #(find-jail-config {:data %}) data)
      :else nil)))

(defn* load-whitelist [:=> [:cat LoadWhitelistInput] [:set :string]] [input]
  (let [f (io/file (:path input))]
    (if (.exists f)
      (let [parsed (edn/read-string (slurp f))
            items (:whitelist parsed)]
        (if (set? items) items #{}))
      #{})))

(defn* key->name [:=> [:cat [:map [:value :any]]] :string] [input]
  (let [v (:value input)]
    (cond
      (keyword? v) (name v)
      (symbol? v) (name v)
      :else (str v))))

(defn* ensure-parent! [:=> [:cat [:map [:path :string]]] :any] [input]
  (io/make-parents (str (:path input) "/.keep")))

(defn* ensure-link! [:=> [:cat EnsureLinkInput] :map] [input]
  (let [{:keys [targetPath sourcePath write? replace?]} input
        target (io/file targetPath)
        source (io/file sourcePath)
        exists? (.exists target)
        symlink? (and exists? (java.nio.file.Files/isSymbolicLink (.toPath target)))]
    (when-not (.exists source)
      (fail! {:message (str "Source path missing: " sourcePath)}))
    (cond
      (and exists? symlink?
           (= (.getCanonicalPath (.getAbsoluteFile target))
              (.getCanonicalPath (.getAbsoluteFile source))))
      {:action "skip-existing-link" :target targetPath :source sourcePath}

      (and exists? (not replace?))
      {:action "blocked-existing-path" :target targetPath :source sourcePath}

      :else
      (do
        (when (and exists? write?)
          (if (.isDirectory target)
            (let [{:keys [exit err]} (sh "rm" "-rf" targetPath)]
              (when-not (zero? exit)
                (fail! {:message (str "Failed to remove existing path: " targetPath "\n" err)})))
            (io/delete-file target true)))
        (if-not write?
          {:action "would-link" :target targetPath :source sourcePath}
          (do
            (case (:mode (meta input))
              "rw" (let [{:keys [exit err]} (sh "rsync" "-aL" "--delete" (str sourcePath "/") (str targetPath "/"))]
                     (when-not (zero? exit)
                       (fail! {:message (str "Failed to copy input to " targetPath "\n" err)}))
                     {:action "copied-rw" :target targetPath :source sourcePath})
              (do
                (java.nio.file.Files/createSymbolicLink
                  (.toPath target)
                  (.toPath source)
                  (into-array java.nio.file.attribute.FileAttribute []))
                {:action "linked-ro" :target targetPath :source sourcePath}))))))))

(main Input
  [input]
  (let [{:keys [attrs inputsRoot mode replace? write? whitelistPath]} (parse-args {:args (:args input)})
        _ (when-not (#{"ro" "rw"} mode)
            (fail! {:message (str "Invalid --mode: " mode " (expected ro|rw)")}))
        attrs-file (io/file attrs)]
    (when-not (.exists attrs-file)
      (fail! {:message (str "Missing attrs json file: " attrs)}))
    (let [raw (json/parse-string (slurp attrs-file))
          config-raw (find-jail-config {:data raw})
          _ (when-not config-raw
              (fail! {:message (str "Could not find jailConfig in " attrs)}))
          config (keywordize-keys {:data config-raw})
          whitelist (load-whitelist {:path whitelistPath})
          manifest (:inputManifest config)
          _ (ensure-parent! {:path (str inputsRoot "/.keep")})
          results (->> manifest
                       (sort-by key)
                       (reduce (fn [acc [name spec]]
                                 (let [name-str (key->name {:value name})]
                                   (if (contains? whitelist name-str)
                                     (let [source (or (:srcPath spec) (:sourcePath spec))
                                           target (str inputsRoot "/" name-str)
                                           result (ensure-link! (with-meta {:targetPath target
                                                                            :sourcePath source
                                                                            :write? write?
                                                                            :replace? replace?}
                                                                  {:mode mode}))]
                                       (conj acc result))
                                     (conj acc {:action "skip-not-whitelisted"
                                                :target (str inputsRoot "/" name-str)
                                                :source nil}))))
                               []))]
      (println (str "Inputs mounter mode: " mode))
      (println (str "Attrs source: " attrs))
      (println (str "Inputs root: " inputsRoot))
      (doseq [r results]
        (println (str "- " (:action r) " :: " (:target r)
                      (when-let [s (:source r)] (str " <- " s)))))
      (println (str "Processed entries: " (count results)))
      (when-not write?
        (println "Dry run only. Re-run with --write to apply.")))))

(-main {:args (vec *command-line-args*)})
