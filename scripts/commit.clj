#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Direct orchestration of VCS commands with cleaner error handling.

(enable!)

(def CommitMainInput
  [:map
   [:args [:vector :string]]
   [:targetBookmark :string]
   [:repoRoot :string]
   [:workspaceRoot :string]])

(defn* run-commit [:=> [:cat CommitMainInput] :any] [input]
  (let [{:keys [args targetBookmark repoRoot workspaceRoot]} input]
    (if (or (not repoRoot) (not workspaceRoot))
      (do (println "Error: MENTCI_REPO_ROOT or MENTCI_WORKSPACE not set.")
          (System/exit 1))
      
      (let [args args
            target-bookmark targetBookmark
            repo-root repoRoot
            workspace-root workspaceRoot]
        (if (empty? args)
          (do (println "Usage: mentci-commit <message>")
              (System/exit 1))
          
          (let [message (first args)
                _ (println (str "Shipping manifestation from workspace: " message))]
            
            ;; 1. Update description
            (let [res1 (sh "jj" "describe" "-m" message "-R" workspace-root)]
              (if (not= 0 (:exit res1))
                (do (println "Error during jj describe:" (:err res1))
                    (System/exit (:exit res1)))
                
                ;; 2. Advance bookmark
                (let [res2 (sh "jj" "bookmark" "set" target-bookmark "-r" "@" "-R" workspace-root)]
                  (if (not= 0 (:exit res2))
                    (do (println "Error during jj bookmark set:" (:err res2))
                        (System/exit (:exit res2)))
                    (println (str "Successfully committed and advanced bookmark '" target-bookmark "' from workspace."))))))))))))

(defn* -main [:=> [:cat [:* :string]] :any] [& args]
  (let [target-bookmark (or (System/getenv "MENTCI_COMMIT_TARGET") "dev")
        repo-root (System/getenv "MENTCI_REPO_ROOT")
        workspace-root (System/getenv "MENTCI_WORKSPACE")
        input {:args (vec args)
               :targetBookmark target-bookmark
               :repoRoot (or repo-root "")
               :workspaceRoot (or workspace-root "")}]
    (run-commit input)))
