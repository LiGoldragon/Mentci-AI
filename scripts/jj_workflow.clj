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

(def DieInput
  [:map
   [:message :string]])

(def WorkspaceRootInput
  [:map])

(def RunJJInput
  [:map
   [:args [:vector :string]]
   [:workspaceRoot :string]])

(def RunJJLogInput
  [:map])

(def RunJJStatusInput
  [:map])

(def RunJJCommitInput
  [:map
   [:message :string]])

(def UsageInput
  [:map])

(def JJMainInput
  [:map
   [:args [:vector :string]]
   [:command :string]])

(defn die [message]
  (let [input {:message message}]
    (when-not (m/validate DieInput input)
      (throw (ex-info "Invalid die input"
                      {:errors (me/humanize (m/explain DieInput input))}))))
  (println message)
  (System/exit 1))

(defn workspace-root []
  (let [input {}]
    (when-not (m/validate WorkspaceRootInput input)
      (throw (ex-info "Invalid workspace-root input"
                      {:errors (me/humanize (m/explain WorkspaceRootInput input))}))))
  (or (System/getenv "MENTCI_WORKSPACE")
      (System/getenv "MENTCI_REPO_ROOT")))

(defn validate-config [config]
  (when-not (m/validate types/JJWorkflowConfig config)
    (throw (ex-info "Invalid JJ Workflow Configuration"
                    {:errors (me/humanize (m/explain types/JJWorkflowConfig config))}))))

(defn run-jj [args]
  (let [root (workspace-root)]
    (let [input {:args (vec args)
                 :workspaceRoot (or root "")}]
      (when-not (m/validate RunJJInput input)
        (throw (ex-info "Invalid run-jj input"
                        {:errors (me/humanize (m/explain RunJJInput input))}))))
    (when-not root
      (die "Error: MENTCI_WORKSPACE or MENTCI_REPO_ROOT not set."))
    (apply sh "jj" (concat args ["-R" root]))))

(defn run-jj-log []
  (let [input {}]
    (when-not (m/validate RunJJLogInput input)
      (throw (ex-info "Invalid run-jj-log input"
                      {:errors (me/humanize (m/explain RunJJLogInput input))}))))
  (let [res (run-jj ["log" "--no-signing"])]
    (if (zero? (:exit res))
      (print (:out res))
      (let [fallback (run-jj ["log"])]
        (if (zero? (:exit fallback))
          (print (:out fallback))
          (die (str "Error during jj log: " (:err fallback))))))))

(defn run-jj-status []
  (let [input {}]
    (when-not (m/validate RunJJStatusInput input)
      (throw (ex-info "Invalid run-jj-status input"
                      {:errors (me/humanize (m/explain RunJJStatusInput input))}))))
  (let [res (run-jj ["status"])]
    (if (zero? (:exit res))
      (print (:out res))
      (die (str "Error during jj status: " (:err res))))))

(defn run-jj-commit [message]
  (let [input {:message message}]
    (when-not (m/validate RunJJCommitInput input)
      (throw (ex-info "Invalid run-jj-commit input"
                      {:errors (me/humanize (m/explain RunJJCommitInput input))}))))
  (let [target-bookmark (or (System/getenv "MENTCI_COMMIT_TARGET") "dev")
        res1 (run-jj ["describe" "-m" message])]
    (if (not= 0 (:exit res1))
      (die (str "Error during jj describe: " (:err res1)))
      (let [res2 (run-jj ["bookmark" "set" target-bookmark "-r" "@"])]
        (if (not= 0 (:exit res2))
          (die (str "Error during jj bookmark set: " (:err res2)))
          (println (str "Successfully committed and advanced bookmark '" target-bookmark "'.")))))))

(defn usage []
  (let [input {}]
    (when-not (m/validate UsageInput input)
      (throw (ex-info "Invalid usage input"
                      {:errors (me/humanize (m/explain UsageInput input))}))))
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
        config {:command (or cmd "")
                :workspaceRoot (or root "")
                :targetBookmark target-bookmark
                :message message}
        main-input {:args (vec args)
                    :command (or cmd "")}]
    (when-not (m/validate JJMainInput main-input)
      (throw (ex-info "Invalid -main input"
                      {:errors (me/humanize (m/explain JJMainInput main-input))})))
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
