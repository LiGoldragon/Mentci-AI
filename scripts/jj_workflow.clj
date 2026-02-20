#!/usr/bin/env bb

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Centralized, safe JJ entrypoints with consistent workspace targeting.

(defn die [message]
  (println message)
  (System/exit 1))

(defn workspace-root []
  (or (System/getenv "MENTCI_WORKSPACE")
      (System/getenv "MENTCI_REPO_ROOT")))

(defn run-jj [args]
  (let [root (workspace-root)]
    (when-not root
      (die "Error: MENTCI_WORKSPACE or MENTCI_REPO_ROOT not set."))
    (apply sh "jj" (concat args ["-R" root]))))

(defn run-jj-log []
  (let [res (run-jj ["log" "--no-signing"])]
    (if (zero? (:exit res))
      (print (:out res))
      (let [fallback (run-jj ["log"])]
        (if (zero? (:exit fallback))
          (print (:out fallback))
          (die (str "Error during jj log: " (:err fallback))))))))

(defn run-jj-status []
  (let [res (run-jj ["status"])]
    (if (zero? (:exit res))
      (print (:out res))
      (die (str "Error during jj status: " (:err res))))))

(defn run-jj-commit [message]
  (let [target-bookmark (or (System/getenv "MENTCI_COMMIT_TARGET") "dev")
        res1 (run-jj ["describe" "-m" message])]
    (if (not= 0 (:exit res1))
      (die (str "Error during jj describe: " (:err res1)))
      (let [res2 (run-jj ["bookmark" "set" target-bookmark "-r" "@"])]
        (if (not= 0 (:exit res2))
          (die (str "Error during jj bookmark set: " (:err res2)))
          (println (str "Successfully committed and advanced bookmark '" target-bookmark "'.")))))))

(defn usage []
  (println "Usage: mentci-jj <command> [args]")
  (println "")
  (println "Commands:")
  (println "  status")
  (println "  log")
  (println "  commit <message>"))

(defn -main []
  (let [args *command-line-args*
        cmd (first args)]
    (case cmd
      "status" (run-jj-status)
      "log" (run-jj-log)
      "commit" (if-let [message (second args)]
                 (run-jj-commit (str/join " " (rest args)))
                 (die "Error: commit requires a message."))
      (usage))))

(-main)
