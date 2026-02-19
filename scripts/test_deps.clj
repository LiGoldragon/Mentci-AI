#!/usr/bin/env bb

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Verified environment audit for the Level 5 stack.

(def deps ["nix" "cargo" "rustc" "bb" "clojure" "jj" "jet" "gdb"])

(defn check-dep [name]
  (let [res (sh "which" name)]
    (if (= 0 (:exit res))
      (do (println (format "[OK] %-10s -> %s" name (str/trim (:out res))))
          true)
      (do (println (format "[!!] %-10s NOT FOUND" name))
          false))))

(defn main []
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
