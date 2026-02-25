#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Verified environment audit for the Level 5 stack.

(enable!)

(def CheckDepInput
  [:map
   [:name :string]])

(def TestDepsMainInput
  [:map])

(def deps ["nix" "cargo" "rustc" "bb" "clojure" "jj" "jet" "gdb"])

(defn* check-dep [:=> [:cat CheckDepInput] :boolean] [input]
  (let [name (:name input)]
  (let [res (sh "which" name)]
    (if (= 0 (:exit res))
      (do (println (format "[OK] %-10s -> %s" name (str/trim (:out res))))
          true)
      (do (println (format "[!!] %-10s NOT FOUND" name))
          false)))))

(defn* main [:=> [:cat TestDepsMainInput] :any] [_]
  (println "Mentci-AI Dependency Audit (Clojure)")
  (println "------------------------------")
  (let [results (map #(check-dep {:name %}) deps)
        all-found? (every? true? results)]
    (println "------------------------------")
    (if all-found?
      (println "All administrative dependencies found in environment.")
      (do (println "Warning: Missing dependencies detected for the current shell.")
          (System/exit 1)))))

(main {})
