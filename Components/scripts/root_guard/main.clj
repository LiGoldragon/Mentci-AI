#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.set :as set]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

(enable!)

(def Entry
  [:map
   [:name :string]
   [:dir? :boolean]])

(def Input
  [:map
   [:args [:vector :string]]])

(def allowed-domain-dirs
  #{"Sources" "Components" "Outputs" "Research" "Development" "Core" "Library"})

(def allowed-runtime-dirs
  #{".git" ".jj" ".direnv" "target" ".mentci"})

(def allowed-top-files
  #{"flake.nix" "flake.lock" ".gitignore" ".envrc" "AGENTS.md" "README.md"
    ".attrs.json" ".opencode.edn"})

(defprotocol RootGuardOps
  (list-root-entries-for [this input])
  (runtime-dir-for [this input])
  (classify-violations-for [this input])
  (fail-for [this input])
  (pass-for [this input]))

(defrecord DefaultRootGuard [])

(impl DefaultRootGuard RootGuardOps list-root-entries-for :map [:vector Entry]
  [this input]
  (let [root (io/file ".")]
    (->> (.listFiles root)
         (map (fn [f]
                {:name (.getName ^java.io.File f)
                 :dir? (.isDirectory ^java.io.File f)}))
         (sort-by :name)
         vec)))

(impl DefaultRootGuard RootGuardOps runtime-dir-for
  {:name :string} :boolean
  [this input]
  (let [name (:name input)]
    (or (contains? allowed-runtime-dirs name)
        (.startsWith name ".jj_"))))

(impl DefaultRootGuard RootGuardOps classify-violations-for
  {:entries [:vector Entry]} [:vector :string]
  [this input]
  (let [entries (:entries input)]
    (->> entries
         (mapcat (fn [{:keys [name dir?]}]
                   (cond
                     (and dir? (contains? allowed-domain-dirs name))
                     []

                     (and dir? (runtime-dir-for this {:name name}))
                     []

                     (and (not dir?) (contains? allowed-top-files name))
                     []

                     dir?
                     [(str "unexpected top-level directory: " name)]

                     :else
                     [(str "unexpected top-level file: " name)])))
         vec)))

(impl DefaultRootGuard RootGuardOps fail-for
  {:errors [:vector :string]} :any
  [this input]
  (binding [*out* *err*]
    (println "Root guard failed:")
    (doseq [error (:errors input)]
      (println (str "- " error))))
  (System/exit 1))

(impl DefaultRootGuard RootGuardOps pass-for :map :any
  [this input]
  (println "Root guard passed: top-level entries match the FS contract."))

(def default-root-guard (->DefaultRootGuard))

(main Input
  [input]
  (let [entries (list-root-entries-for default-root-guard {})
        errors (classify-violations-for default-root-guard {:entries entries})]
    (if (seq errors)
      (fail-for default-root-guard {:errors errors})
      (pass-for default-root-guard {}))))

(-main {:args (vec *command-line-args*)})
