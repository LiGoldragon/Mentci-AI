#!/usr/bin/env bb

(require '[clojure.java.shell :refer [sh]]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Direct orchestration of VCS commands with cleaner error handling.

(defn main []
  (let [target-bookmark (or (System/getenv "MENTCI_COMMIT_TARGET") "dev")
        repo-root (System/getenv "MENTCI_REPO_ROOT")
        workspace-root (System/getenv "MENTCI_WORKSPACE")]
    
    (if (or (not repo-root) (not workspace-root))
      (do (println "Error: MENTCI_REPO_ROOT or MENTCI_WORKSPACE not set.")
          (System/exit 1))
      
      (let [args *command-line-args*]
        (if (empty? args)
          (do (println "Usage: mentci-commit <message>")
              (System/exit 1))
          
          (let [message (first args)]
            (println (str "Shipping manifestation from workspace: " message))
            
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

(main)
