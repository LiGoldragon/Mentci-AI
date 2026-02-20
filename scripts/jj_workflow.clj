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
;; Rationale: Centralized, safe JJ entrypoints with consistent workspace targeting.

(defn die [message]
  (println message)
  (System/exit 1))

(defn workspace-root []
  (or (System/getenv "MENTCI_WORKSPACE")
      (System/getenv "MENTCI_REPO_ROOT")))

(defn validate-config [config]
  (when-not (m/validate types/JJWorkflowConfig config)
    (throw (ex-info "Invalid JJ Workflow Configuration"
                    {:errors (me/humanize (m/explain types/JJWorkflowConfig config))}))))

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
        cmd (first args)
        root (workspace-root)
        target-bookmark (or (System/getenv "MENTCI_COMMIT_TARGET") "dev")
        message (when (= cmd "commit") (str/join " " (rest args)))
        config {:sema/type "JJWorkflowConfig"
                :command (or cmd "")
                :workspaceRoot (or root "")
                :targetBookmark target-bookmark
                :message message}]
    (when (= cmd "commit")
      (when (str/blank? message)
        (die "Error: commit requires a message.")))
    (validate-config config)
    (case cmd
      "status" (run-jj-status)
      "log" (run-jj-log)
      "commit" (run-jj-commit message)
      (usage))))

(-main)
