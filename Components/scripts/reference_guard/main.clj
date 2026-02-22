#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* main enable!]])

(enable!)

(def ReferenceGuardConfig
  [:map
   [:roots [:vector :string]]
   [:allowlist [:set :string]]])

(def Input
  [:map
   [:args [:vector :string]]])

(def CollectFilesInput
  [:map
   [:root :string]])

(def FileCheckInput
  [:map
   [:path :string]
   [:content :string]
   [:allowlist [:set :string]]])

(defn* file-extension-allowed? [:=> [:cat [:map [:path :string]]] :boolean] [input]
  (boolean (re-find #"\.(md|edn|clj|nix|toml|json|yaml|yml)$" (:path input))))

(defn* collect-files [:=> [:cat CollectFilesInput] [:vector :any]] [input]
  (let [root (io/file (:root input))]
    (if (.exists root)
      (if (.isFile root)
        (if (file-extension-allowed? {:path (.getPath root)}) [root] [])
        (->> (file-seq root)
             (filter #(.isFile %))
             (filter #(file-extension-allowed? {:path (.getPath %)}))
             vec))
      [])))

(defn* rel-path [:=> [:cat [:map [:file :any]]] :string] [input]
  (let [cwd (.normalize (.toPath (io/file ".")))
        path (.normalize (.toPath ^java.io.File (:file input)))]
    (str (.relativize cwd path))))

(defn* check-file [:=> [:cat FileCheckInput] [:vector :string]] [input]
  (let [{:keys [path content allowlist]} input
        path-lc (str/lower-case path)]
    (if (contains? allowlist path)
      []
      (vec
       (remove nil?
               [(when (re-find #"Library/architecture/AGENTS\.md" content)
                  (str path-lc ": forbidden reference to Library/architecture/AGENTS.md (use Core/AGENTS.md)"))
                (when (re-find #"Library/architecture/HIGH_LEVEL_GOALS\.md" content)
                  (str path-lc ": forbidden reference to Library/architecture/HIGH_LEVEL_GOALS.md (use Core/HIGH_LEVEL_GOALS.md)"))
                (when (re-find #"Library/guides/RESTART_CONTEXT\.md" content)
                  (str path-lc ": forbidden reference to Library/guides/RESTART_CONTEXT.md (use Library/RESTART_CONTEXT.md)"))
                (when (or (re-find #"`inputs/(?!outputs\b)[^`\n]+`" content)
                          (re-find #"\"inputs/(?!outputs\b)[^\"\n]+\"" content))
                  (str path-lc ": lowercase inputs/ path detected"))])))))

(main Input
  [_]
  (let [config {:roots ["AGENTS.md"
                        "Core"
                        "Library"
                        "Components"
                        "Reports"
                        "Strategies"]
                :allowlist #{"Strategies/Agent-Authority-Alignment/STRATEGY.md"
                             "Strategies/Agent-Authority-Alignment/INTEGRATION.md"}}
        files (mapcat #(collect-files {:root %}) (:roots config))
        errors (->> files
                    (mapcat (fn [file]
                              (let [path (rel-path {:file file})
                                    content (slurp file)]
                                (check-file {:path path
                                             :content content
                                             :allowlist (:allowlist config)}))))
                    vec)]
    (if (seq errors)
      (do
        (binding [*out* *err*]
          (println "Reference guard failed:")
          (doseq [error errors]
            (println (str "- " error))))
        (System/exit 1))
      (println "Reference guard passed."))))

(-main {:args (vec *command-line-args*)})
