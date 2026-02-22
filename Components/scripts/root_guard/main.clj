#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.set :as set]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def Entry
  [:map
   [:name :string]
   [:dir? :boolean]])

(def MainInput
  [:map
   [:args [:vector :string]]])

(def allowed-domain-dirs
  #{"Inputs" "Components" "Outputs" "Reports" "Strategies" "Core" "Library"})

(def allowed-runtime-dirs
  #{".git" ".jj" ".direnv" "target"})

(def allowed-top-files
  #{"flake.nix" "flake.lock" ".gitignore" ".envrc" "AGENTS.md" "README.md"
    ".attrs.json" ".opencode.edn"})

(defn* list-root-entries [:=> [:cat] [:vector Entry]] []
  (let [root (io/file ".")]
    (->> (.listFiles root)
         (map (fn [f]
                {:name (.getName ^java.io.File f)
                 :dir? (.isDirectory ^java.io.File f)}))
         (sort-by :name)
         vec)))

(defn* runtime-dir? [:=> [:cat [:map [:name :string]]] :boolean] [input]
  (let [name (:name input)]
    (or (contains? allowed-runtime-dirs name)
        (.startsWith name ".jj_"))))

(defn* classify-violations [:=> [:cat [:map [:entries [:vector Entry]]]] [:vector :string]] [input]
  (let [entries (:entries input)]
    (->> entries
         (mapcat (fn [{:keys [name dir?]}]
                   (cond
                     (and dir? (contains? allowed-domain-dirs name))
                     []

                     (and dir? (runtime-dir? {:name name}))
                     []

                     (and (not dir?) (contains? allowed-top-files name))
                     []

                     dir?
                     [(str "unexpected top-level directory: " name)]

                     :else
                     [(str "unexpected top-level file: " name)])))
         vec)))

(defn* fail! [:=> [:cat [:map [:errors [:vector :string]]]] :any] [input]
  (binding [*out* *err*]
    (println "Root guard failed:")
    (doseq [error (:errors input)]
      (println (str "- " error))))
  (System/exit 1))

(defn* pass! [:=> [:cat] :any] []
  (println "Root guard passed: top-level entries match the FS contract."))

(defn* -main [:=> [:cat MainInput] :any] [_]
  (let [entries (list-root-entries)
        errors (classify-violations {:entries entries})]
    (if (seq errors)
      (fail! {:errors errors})
      (pass!))))

(-main {:args (vec *command-line-args*)})
