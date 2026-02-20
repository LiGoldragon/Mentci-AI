#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def ScriptValidationConfig
  [:map
   [:scriptsDir :string]
   [:allowlist [:set :string]]])

(def ScriptFailInput
  [:map
   [:message :string]])

(def ScriptParseArgsInput
  [:map
   [:args [:vector :string]]])

(def ScriptValidateConfigInput
  [:map
   [:config ScriptValidationConfig]])

(def ScriptFileCheckInput
  [:map
   [:file :any]])

(def ScriptScanFilesInput
  [:map
   [:root :string]])

(def ScriptContainsSubstringInput
  [:map
   [:content :string]
   [:substring :string]])

(def ScriptValidateCljInput
  [:map
   [:path :string]
   [:content :string]
   [:allowlist [:set :string]]])

(def ScriptMainInput
  [:map
   [:args [:vector :string]]])

(defn* fail [:=> [:cat ScriptFailInput] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* parse-args [:=> [:cat ScriptParseArgsInput] :map] [input]
  (let [args (:args input)]
    (loop [remaining args
           opts {}]
      (if (empty? remaining)
        opts
        (let [[arg & _more] remaining]
          (cond
            (= arg "--scripts-dir")
            (recur (nnext remaining) (assoc opts :scripts-dir (second remaining)))

            :else
            (fail {:message (str "Unknown argument: " arg)})))))))

(defn* validate-config [:=> [:cat ScriptValidateConfigInput] :any] [input]
  (:config input))

(defn* clj-file? [:=> [:cat ScriptFileCheckInput] :boolean] [input]
  (str/ends-with? (.getName (:file input)) ".clj"))

(defn* py-file? [:=> [:cat ScriptFileCheckInput] :boolean] [input]
  (str/ends-with? (.getName (:file input)) ".py"))

(defn* scan-files [:=> [:cat ScriptScanFilesInput] [:vector :any]] [input]
  (->> (file-seq (io/file (:root input)))
       (filter #(.isFile %))
       vec))

(defn* contains-substring? [:=> [:cat ScriptContainsSubstringInput] :boolean] [input]
  (let [{:keys [content substring]} input]
    (not (nil? (str/index-of content substring)))))

(defn* validate-clj [:=> [:cat ScriptValidateCljInput] :any] [input]
  (let [{:keys [path content allowlist]} input]
    (when-not (contains? allowlist path)
      (when-not (contains-substring? {:content content :substring "defn*"})
        (fail {:message (str "Missing defn* usage in " path)}))
      (when-not (contains-substring? {:content content :substring "mentci.malli"})
        (fail {:message (str "Missing mentci.malli require in " path)})))))

(defn* -main [:=> [:cat ScriptMainInput] :any] [input]
  (let [{:keys [scripts-dir]} (parse-args {:args (:args input)})
        scripts-dir (or scripts-dir "scripts")
        config {:scriptsDir scripts-dir
                :allowlist #{"scripts/types.clj"
                             "scripts/malli.clj"}}
        _ (validate-config {:config config})
        files (scan-files {:root scripts-dir})
        py-files (filter #(py-file? {:file %}) files)]
    (when (seq py-files)
      (doseq [file py-files]
        (fail {:message (str "Python script found in scripts/: " (.getPath file))})))
    (doseq [file (filter #(clj-file? {:file %}) files)]
      (let [path (.getPath file)
            content (slurp file)]
        (validate-clj {:path path :content content :allowlist (:allowlist config)})))
    (println (str "Script validation passed for " scripts-dir))))

(-main {:args (vec *command-line-args*)})
