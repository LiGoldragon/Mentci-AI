#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))

(def ScriptValidationConfig
  [:map
   [:sema/type [:= "ScriptValidationConfig"]]
   [:scriptsDir :string]
   [:allowlist [:set :string]]])

(defn fail [msg]
  (binding [*out* *err*]
    (println msg))
  (System/exit 1))

(defn parse-args [args]
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
  (when-not (m/validate ScriptValidationConfig config)
    (throw (ex-info "Invalid Script Validation Configuration"
                    {:errors (me/humanize (m/explain ScriptValidationConfig config))}))))

(defn clj-file? [file]
  (str/ends-with? (.getName file) ".clj"))

(defn py-file? [file]
  (str/ends-with? (.getName file) ".py"))

(defn scan-files [root]
  (->> (file-seq (io/file root))
       (filter #(.isFile %))))

(defn contains-substring? [content substring]
  (not (nil? (str/index-of content substring))))

(defn validate-clj [path content allowlist]
  (when-not (contains? allowlist path)
    (when-not (contains-substring? content "malli.core")
      (fail (str "Missing Malli dependency in " path)))
    (when-not (contains-substring? content "m/validate")
      (fail (str "Missing m/validate usage in " path)))))

(defn -main [& args]
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
