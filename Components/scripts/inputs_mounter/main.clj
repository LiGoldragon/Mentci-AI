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
(require '[mentci.malli :refer [impl main enable!]])

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

(defprotocol InputsMounterOps
  (fail-for [this input])
  (parse-args-for [this input])
  (keywordize-keys-for [this input])
  (find-jail-config-for [this input])
  (load-whitelist-for [this input])
  (key-name-for [this input])
  (ensure-parent-for [this input])
  (ensure-link-for [this input])
  (run-mounter-for [this input]))

(defrecord DefaultInputsMounter [])

(impl DefaultInputsMounter InputsMounterOps fail-for {:message :string} :any
  [this input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(impl DefaultInputsMounter InputsMounterOps parse-args-for ParseArgsInput :map
  [this input]
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
          (fail-for this {:message (str "Unknown argument: " arg)}))))))

(impl DefaultInputsMounter InputsMounterOps keywordize-keys-for KeywordizeKeysInput :map
  [this input]
  (let [data (:data input)]
    (into {}
          (for [[k v] data]
            [(keyword k)
             (cond
               (map? v) (keywordize-keys-for this {:data v})
               (vector? v) (mapv (fn [i] (if (map? i) (keywordize-keys-for this {:data i}) i)) v)
               :else v)]))))

(impl DefaultInputsMounter InputsMounterOps find-jail-config-for FindJailConfigInput [:maybe :map]
  [this input]
  (let [data (:data input)]
    (cond
      (and (map? data) (get data "inputManifest")) data
      (and (map? data) (get data :inputManifest)) data
      (map? data) (some #(find-jail-config-for this {:data %}) (vals data))
      (string? data) (try (find-jail-config-for this {:data (json/parse-string data)})
                          (catch Exception _ nil))
      (coll? data) (some #(find-jail-config-for this {:data %}) data)
      :else nil)))

(impl DefaultInputsMounter InputsMounterOps load-whitelist-for LoadWhitelistInput [:set :string]
  [this input]
  (let [f (io/file (:path input))]
    (if (.exists f)
      (let [parsed (edn/read-string (slurp f))
            items (:whitelist parsed)]
        (if (set? items) items #{}))
      #{})))

(impl DefaultInputsMounter InputsMounterOps key-name-for {:value :any} :string
  [this input]
  (let [v (:value input)]
    (cond
      (keyword? v) (name v)
      (symbol? v) (name v)
      :else (str v))))

(impl DefaultInputsMounter InputsMounterOps ensure-parent-for {:path :string} :any
  [this input]
  (io/make-parents (str (:path input) "/.keep")))

(impl DefaultInputsMounter InputsMounterOps ensure-link-for EnsureLinkInput :map
  [this input]
  (let [{:keys [targetPath sourcePath write? replace?]} input
        target (io/file targetPath)
        source (io/file sourcePath)
        exists? (.exists target)
        symlink? (and exists? (java.nio.file.Files/isSymbolicLink (.toPath target)))]
    (when-not (.exists source)
      (fail-for this {:message (str "Source path missing: " sourcePath)}))
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
                (fail-for this {:message (str "Failed to remove existing path: " targetPath "\n" err)})))
            (io/delete-file target true)))
        (if-not write?
          {:action "would-link" :target targetPath :source sourcePath}
          (do
            (case (:mode (meta input))
              "rw" (let [{:keys [exit err]} (sh "rsync" "-aL" "--delete" (str sourcePath "/") (str targetPath "/"))]
                     (when-not (zero? exit)
                       (fail-for this {:message (str "Failed to copy input to " targetPath "\n" err)}))
                     {:action "copied-rw" :target targetPath :source sourcePath})
              (do
                (java.nio.file.Files/createSymbolicLink
                  (.toPath target)
                  (.toPath source)
                  (into-array java.nio.file.attribute.FileAttribute []))
                {:action "linked-ro" :target targetPath :source sourcePath}))))))))

(impl DefaultInputsMounter InputsMounterOps run-mounter-for Input :any
  [this input]
  (let [{:keys [attrs inputsRoot mode replace? write? whitelistPath]} (parse-args-for this {:args (:args input)})
        _ (when-not (#{"ro" "rw"} mode)
            (fail-for this {:message (str "Invalid --mode: " mode " (expected ro|rw)")}))
        attrs-file (io/file attrs)]
    (when-not (.exists attrs-file)
      (fail-for this {:message (str "Missing attrs json file: " attrs)}))
    (let [raw (json/parse-string (slurp attrs-file))
          config-raw (find-jail-config-for this {:data raw})
          _ (when-not config-raw
              (fail-for this {:message (str "Could not find jailConfig in " attrs)}))
          config (keywordize-keys-for this {:data config-raw})
          whitelist (load-whitelist-for this {:path whitelistPath})
          manifest (:inputManifest config)
          _ (ensure-parent-for this {:path (str inputsRoot "/.keep")})
          results (->> manifest
                       (sort-by key)
                       (reduce (fn [acc [name spec]]
                                 (let [name-str (key-name-for this {:value name})]
                                   (if (contains? whitelist name-str)
                                     (let [source (or (:srcPath spec) (:sourcePath spec))
                                           target (str inputsRoot "/" name-str)
                                           result (ensure-link-for (with-meta {:targetPath target
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

(def default-inputs-mounter (->DefaultInputsMounter))

(main Input
  [input]
  (run-mounter-for default-inputs-mounter input))

(-main {:args (vec *command-line-args*)})
