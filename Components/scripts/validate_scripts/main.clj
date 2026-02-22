#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

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

(def ScriptMainContractInput
  [:map
   [:path :string]
   [:content :string]
   [:contractAllowlist [:set :string]]])

(def Input
  [:map
   [:args [:vector :string]]])

(defprotocol ScriptValidatorOps
  (fail-for [this input])
  (parse-args-for [this input])
  (validate-config-for [this input])
  (clj-file-for [this input])
  (py-file-for [this input])
  (scan-files-for [this input])
  (contains-substring-for [this input])
  (validate-clj-for [this input])
  (validate-main-contract-for [this input])
  (run-validator-for [this input]))

(defrecord DefaultScriptValidator [])

(impl DefaultScriptValidator ScriptValidatorOps fail-for ScriptFailInput :any
  [this input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(impl DefaultScriptValidator ScriptValidatorOps parse-args-for ScriptParseArgsInput :map
  [this input]
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
            (fail-for this {:message (str "Unknown argument: " arg)})))))))

(impl DefaultScriptValidator ScriptValidatorOps validate-config-for ScriptValidateConfigInput :any
  [this input]
  (:config input))

(impl DefaultScriptValidator ScriptValidatorOps clj-file-for ScriptFileCheckInput :boolean
  [this input]
  (str/ends-with? (.getName (:file input)) ".clj"))

(impl DefaultScriptValidator ScriptValidatorOps py-file-for ScriptFileCheckInput :boolean
  [this input]
  (str/ends-with? (.getName (:file input)) ".py"))

(impl DefaultScriptValidator ScriptValidatorOps scan-files-for ScriptScanFilesInput [:vector :any]
  [this input]
  (->> (file-seq (io/file (:root input)))
       (filter #(.isFile %))
       vec))

(impl DefaultScriptValidator ScriptValidatorOps contains-substring-for ScriptContainsSubstringInput :boolean
  [this input]
  (let [{:keys [content substring]} input]
    (not (nil? (str/index-of content substring)))))

(impl DefaultScriptValidator ScriptValidatorOps validate-clj-for ScriptValidateCljInput :any
  [this input]
  (let [{:keys [path content allowlist]} input]
    (when-not (contains? allowlist path)
      (when-not (str/ends-with? path "/test.clj")
        (when-not (contains-substring-for this {:content content :substring "mentci.malli"})
          (fail-for this {:message (str "Missing mentci.malli require in " path)}))))))

(impl DefaultScriptValidator ScriptValidatorOps validate-main-contract-for ScriptMainContractInput :any
  [this input]
  (let [{:keys [path content contractAllowlist]} input]
    (when (and (str/ends-with? path "/main.clj")
               (not (contains? contractAllowlist path)))
      (when-not (contains-substring-for this {:content content :substring "(main "})
        (fail-for this {:message (str "Missing main macro usage in " path)}))
      (when-not (contains-substring-for this {:content content :substring "(defprotocol "})
        (fail-for this {:message (str "Missing defprotocol in " path)}))
      (when-not (contains-substring-for this {:content content :substring "(impl "})
        (fail-for this {:message (str "Missing impl usage in " path)})))))

(impl DefaultScriptValidator ScriptValidatorOps run-validator-for Input :any
  [this input]
  (let [{:keys [scripts-dir]} (parse-args-for this {:args (:args input)})
        scripts-dir (or scripts-dir "Components/scripts")
        config {:scriptsDir scripts-dir
                :allowlist #{"Components/scripts/lib/types.clj"
                             "Components/scripts/lib/malli.clj"}
                :contractAllowlist #{}}
        _ (validate-config-for this {:config config})
        files (scan-files-for this {:root scripts-dir})
        py-files (filter #(py-file-for this {:file %}) files)]
    (when (seq py-files)
      (doseq [file py-files]
        (fail-for this {:message (str "Python script found in scripts/: " (.getPath file))})))
    (doseq [file (filter #(clj-file-for this {:file %}) files)]
      (let [path (.getPath file)
            content (slurp file)]
        (validate-clj-for this {:path path :content content :allowlist (:allowlist config)})
        (validate-main-contract-for this {:path path :content content :contractAllowlist (:contractAllowlist config)})))
    (println (str "Script validation passed for " scripts-dir))))

(def default-script-validator (->DefaultScriptValidator))

(main Input
  [input]
  (run-validator-for default-script-validator input))

(-main {:args (vec *command-line-args*)})
