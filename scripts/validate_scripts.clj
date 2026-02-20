#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))

(def ScriptFailInput
  [:map
   [:sema/type [:= "ScriptFailInput"]]
   [:message :string]])

(def ScriptParseArgsInput
  [:map
   [:sema/type [:= "ScriptParseArgsInput"]]
   [:args [:vector :string]]])

(def ScriptValidateConfigInput
  [:map
   [:sema/type [:= "ScriptValidateConfigInput"]]
   [:config ScriptValidationConfig]])

(def ScriptFileCheckInput
  [:map
   [:sema/type [:= "ScriptFileCheckInput"]]
   [:file :any]])

(def ScriptScanFilesInput
  [:map
   [:sema/type [:= "ScriptScanFilesInput"]]
   [:root :string]])

(def ScriptContainsSubstringInput
  [:map
   [:sema/type [:= "ScriptContainsSubstringInput"]]
   [:content :string]
   [:substring :string]])

(def ScriptValidateCljInput
  [:map
   [:sema/type [:= "ScriptValidateCljInput"]]
   [:path :string]
   [:content :string]
   [:allowlist [:set :string]]])

(def ScriptMainInput
  [:map
   [:sema/type [:= "ScriptMainInput"]]
   [:args [:vector :string]]])

(def ScriptValidationConfig
  [:map
   [:sema/type [:= "ScriptValidationConfig"]]
   [:scriptsDir :string]
   [:allowlist [:set :string]]])

(defn fail [msg]
  (let [input {:sema/type "ScriptFailInput"
               :message msg}]
    (when-not (m/validate ScriptFailInput input)
      (throw (ex-info "Invalid fail input"
                      {:errors (me/humanize (m/explain ScriptFailInput input))}))))
  (binding [*out* *err*]
    (println msg))
  (System/exit 1))

(defn parse-args [args]
  (let [input {:sema/type "ScriptParseArgsInput"
               :args (vec args)}]
    (when-not (m/validate ScriptParseArgsInput input)
      (throw (ex-info "Invalid parse-args input"
                      {:errors (me/humanize (m/explain ScriptParseArgsInput input))}))))
  (loop [remaining args
         opts {}]
    (if (empty? remaining)
      opts
      (let [[arg & more] remaining]
        (cond
          (= arg "--scripts-dir")
          (recur (nnext remaining) (assoc opts :scripts-dir (second remaining)))

          :else
          (fail (str "Unknown argument: " arg)))))))

(defn validate-config [config]
  (let [input {:sema/type "ScriptValidateConfigInput"
               :config config}]
    (when-not (m/validate ScriptValidateConfigInput input)
      (throw (ex-info "Invalid validate-config input"
                      {:errors (me/humanize (m/explain ScriptValidateConfigInput input))}))))
  (when-not (m/validate ScriptValidationConfig config)
    (throw (ex-info "Invalid Script Validation Configuration"
                    {:errors (me/humanize (m/explain ScriptValidationConfig config))}))))

(defn clj-file? [file]
  (let [input {:sema/type "ScriptFileCheckInput"
               :file file}]
    (when-not (m/validate ScriptFileCheckInput input)
      (throw (ex-info "Invalid clj-file? input"
                      {:errors (me/humanize (m/explain ScriptFileCheckInput input))}))))
  (str/ends-with? (.getName file) ".clj"))

(defn py-file? [file]
  (let [input {:sema/type "ScriptFileCheckInput"
               :file file}]
    (when-not (m/validate ScriptFileCheckInput input)
      (throw (ex-info "Invalid py-file? input"
                      {:errors (me/humanize (m/explain ScriptFileCheckInput input))}))))
  (str/ends-with? (.getName file) ".py"))

(defn scan-files [root]
  (let [input {:sema/type "ScriptScanFilesInput"
               :root root}]
    (when-not (m/validate ScriptScanFilesInput input)
      (throw (ex-info "Invalid scan-files input"
                      {:errors (me/humanize (m/explain ScriptScanFilesInput input))}))))
  (->> (file-seq (io/file root))
       (filter #(.isFile %))))

(defn contains-substring? [content substring]
  (let [input {:sema/type "ScriptContainsSubstringInput"
               :content content
               :substring substring}]
    (when-not (m/validate ScriptContainsSubstringInput input)
      (throw (ex-info "Invalid contains-substring? input"
                      {:errors (me/humanize (m/explain ScriptContainsSubstringInput input))}))))
  (not (nil? (str/index-of content substring))))

(defn validate-clj [path content allowlist]
  (let [input {:sema/type "ScriptValidateCljInput"
               :path path
               :content content
               :allowlist allowlist}]
    (when-not (m/validate ScriptValidateCljInput input)
      (throw (ex-info "Invalid validate-clj input"
                      {:errors (me/humanize (m/explain ScriptValidateCljInput input))}))))
  (when-not (contains? allowlist path)
    (when-not (contains-substring? content "malli.core")
      (fail (str "Missing Malli dependency in " path)))
    (when-not (contains-substring? content "m/validate")
      (fail (str "Missing m/validate usage in " path)))))

(defn -main [& args]
  (let [input {:sema/type "ScriptMainInput"
               :args (vec args)}]
    (when-not (m/validate ScriptMainInput input)
      (throw (ex-info "Invalid -main input"
                      {:errors (me/humanize (m/explain ScriptMainInput input))}))))
  (let [{:keys [scripts-dir]} (parse-args args)
        scripts-dir (or scripts-dir "scripts")
        config {:sema/type "ScriptValidationConfig"
                :scriptsDir scripts-dir
                :allowlist #{}}
        _ (validate-config config)
        files (scan-files scripts-dir)
        py-files (filter py-file? files)]
    (when (seq py-files)
      (doseq [file py-files]
        (fail (str "Python script found in scripts/: " (.getPath file)))))
    (doseq [file (filter clj-file? files)]
      (let [path (.getPath file)
            content (slurp file)]
        (validate-clj path content (:allowlist config))))
    (println (str "Script validation passed for " scripts-dir))))

(apply -main *command-line-args*)
