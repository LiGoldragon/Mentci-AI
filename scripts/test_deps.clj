#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me]
         '[clojure.java.io :as io])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Verified environment audit for the Level 5 stack.

(def CheckDepInput
  [:map
   [:name :string]])

(def TestDepsMainInput
  [:map])

(def deps ["nix" "cargo" "rustc" "bb" "clojure" "jj" "jet" "gdb"])

(defn validate-config [config]
  (when-not (m/validate types/DependencyCheckConfig config)
    (throw (ex-info "Invalid Dependency Check Configuration"
                    {:errors (me/humanize (m/explain types/DependencyCheckConfig config))}))))

(defn check-dep [name]
  (let [input {:name name}]
    (when-not (m/validate CheckDepInput input)
      (throw (ex-info "Invalid check-dep input"
                      {:errors (me/humanize (m/explain CheckDepInput input))}))))
  (let [res (sh "which" name)]
    (if (= 0 (:exit res))
      (do (println (format "[OK] %-10s -> %s" name (str/trim (:out res))))
          true)
      (do (println (format "[!!] %-10s NOT FOUND" name))
          false))))

(defn main []
  (let [input {}]
    (when-not (m/validate TestDepsMainInput input)
      (throw (ex-info "Invalid main input"
                      {:errors (me/humanize (m/explain TestDepsMainInput input))}))))
  (let [config {:deps deps}]
    (validate-config config))
  (println "Mentci-AI Dependency Audit (Clojure)")
  (println "------------------------------")
  (let [results (map check-dep deps)
        all-found? (every? true? results)]
    (println "------------------------------")
    (if all-found?
      (println "All administrative dependencies found in environment.")
      (do (println "Warning: Missing dependencies detected for the current shell.")
          (System/exit 1)))))

(main)
