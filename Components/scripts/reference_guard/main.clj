#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

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

(defprotocol ReferenceGuardOps
  (file-extension-allowed-for [this input])
  (collect-files-for [this input])
  (rel-path-for [this input])
  (check-file-for [this input]))

(defrecord DefaultReferenceGuard [])

(impl DefaultReferenceGuard ReferenceGuardOps file-extension-allowed-for
  [:=> [:cat :any [:map [:path :string]]] :boolean]
  [this input]
  (boolean (re-find #"\.(md|edn|clj|nix|toml|json|yaml|yml)$" (:path input))))

(impl DefaultReferenceGuard ReferenceGuardOps collect-files-for
  [:=> [:cat :any CollectFilesInput] [:vector :any]]
  [this input]
  (let [root (io/file (:root input))]
    (if (.exists root)
      (if (.isFile root)
        (if (file-extension-allowed-for this {:path (.getPath root)}) [root] [])
        (->> (file-seq root)
             (filter #(.isFile %))
             (filter #(file-extension-allowed-for this {:path (.getPath %)}))
             vec))
      [])))

(impl DefaultReferenceGuard ReferenceGuardOps rel-path-for
  [:=> [:cat :any [:map [:file :any]]] :string]
  [this input]
  (let [cwd (.normalize (.toPath (io/file ".")))
        path (.normalize (.toPath ^java.io.File (:file input)))]
    (str (.relativize cwd path))))

(impl DefaultReferenceGuard ReferenceGuardOps check-file-for
  [:=> [:cat :any FileCheckInput] [:vector :string]]
  [this input]
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

(def default-reference-guard (->DefaultReferenceGuard))

(main Input
  [_]
  (let [config {:roots ["AGENTS.md"
                        "Core"
                        "Library"
                        "Components"
                        "Reports"
                        "Strategies"]
                :allowlist #{"Strategies/Agent-Authority-Alignment/STRATEGY.md"
                             "Strategies/Agent-Authority-Alignment/INTEGRATION.md"
                             "Components/scripts/reference_guard/main.clj"}}
        files (mapcat #(collect-files-for default-reference-guard {:root %}) (:roots config))
        errors (->> files
                    (mapcat (fn [file]
                              (let [path (rel-path-for default-reference-guard {:file file})
                                    content (slurp file)]
                                (check-file-for default-reference-guard {:path path
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
