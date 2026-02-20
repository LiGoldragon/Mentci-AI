#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Centralized, safe JJ entrypoints with consistent workspace targeting.

(enable!)

(def DieInput
  [:map
   [:message :string]])

(def WorkspaceRootInput
  [:map])

(def RunJJInput
  [:map
   [:args [:vector :string]]])

(def RunJJLogInput
  [:map])

(def RunJJStatusInput
  [:map])

(def RunJJCommitInput
  [:map
   [:workingBookmark :string]
   [:targetBookmark :string]
   [:message :string]])

(def LoadPolicyInput
  [:map
   [:policyPath [:maybe :string]]])

(def AllowedTargetInput
  [:map
   [:targetBookmark :string]
   [:allowedPushBookmarks [:set :string]]])

(def UsageInput
  [:map])

(def JJMainInput
  [:map
   [:args [:vector :string]]
   [:command :string]])

(defn* die [:=> [:cat DieInput] :any] [input]
  (let [message (:message input)]
    (println message)
    (System/exit 1)))

(defn* workspace-root [:=> [:cat WorkspaceRootInput] [:maybe :string]] [_]
  (or (System/getenv "MENTCI_WORKSPACE")
      (System/getenv "MENTCI_REPO_ROOT")))

(defn* run-jj [:=> [:cat RunJJInput] :any] [input]
  (let [root (workspace-root)
        args (:args input)]
    (when-not root
      (die {:message "Error: MENTCI_WORKSPACE or MENTCI_REPO_ROOT not set."}))
    (apply sh "jj" (concat args ["-R" root]))))

(defn* run-jj-log [:=> [:cat RunJJLogInput] :any] [_]
  (let [res (run-jj {:args ["log" "--no-signing"]})]
    (if (zero? (:exit res))
      (print (:out res))
      (let [fallback (run-jj {:args ["log"]})]
        (if (zero? (:exit fallback))
          (print (:out fallback))
          (die {:message (str "Error during jj log: " (:err fallback))})))))))

(defn* run-jj-status [:=> [:cat RunJJStatusInput] :any] [_]
  (let [res (run-jj {:args ["status"]})]
    (if (zero? (:exit res))
      (print (:out res))
      (die {:message (str "Error during jj status: " (:err res))}))))

(defn* load-allowed-push-bookmarks [:=> [:cat LoadPolicyInput] [:set :string]] [input]
  (let [policy-path (:policyPath input)]
    (if (str/blank? policy-path)
      #{}
      (let [policy-file (io/file policy-path)]
        (when-not (.exists policy-file)
          (die {:message (str "Error: MENTCI_JAIL_POLICY points to missing file: " policy-path)}))
        (let [policy (json/parse-string (slurp policy-file) true)
              bookmarks (get policy :allowedPushBookmarks [])]
          (set (map str bookmarks)))))))

(defn* assert-target-allowed [:=> [:cat AllowedTargetInput] :any] [input]
  (let [{:keys [targetBookmark allowedPushBookmarks]} input]
    (when (and (seq allowedPushBookmarks)
               (not (contains? allowedPushBookmarks targetBookmark)))
      (die {:message (str "Error: target bookmark '" targetBookmark "' is not allowed by jail policy. Allowed: " (vec allowedPushBookmarks))}))))

(defn* run-jj-commit [:=> [:cat RunJJCommitInput] :any] [input]
  (let [message (:message input)
        working-bookmark (:workingBookmark input)
        target-bookmark (:targetBookmark input)
        allowed-push-bookmarks (load-allowed-push-bookmarks {:policyPath (System/getenv "MENTCI_JAIL_POLICY")})
        res1 (run-jj {:args ["describe" "-m" message]})]
    (when (= working-bookmark target-bookmark)
      (die {:message (str "Error: target bookmark '" target-bookmark "' must differ from working bookmark '" working-bookmark "'.")}))
    (assert-target-allowed {:targetBookmark target-bookmark
                            :allowedPushBookmarks allowed-push-bookmarks})
    (if (not= 0 (:exit res1))
      (die {:message (str "Error during jj describe: " (:err res1))})
      (let [res2 (run-jj {:args ["bookmark" "set" target-bookmark "-r" "@"]})]
        (if (not= 0 (:exit res2))
          (die {:message (str "Error during jj bookmark set: " (:err res2))})
          (println (str "Successfully committed and advanced bookmark '" target-bookmark "'.")))))))

(defn* usage [:=> [:cat UsageInput] :any] [_]
  (println "Usage: mentci-jj <command> [args]")
  (println "")
  (println "Commands:")
  (println "  status")
  (println "  log")
  (println "  commit <message>"))

(defn* -main [:=> [:cat JJMainInput] :any] [input]
  (let [args (:args input)
        cmd (first args)
        working-bookmark (or (System/getenv "MENTCI_WORKING_BOOKMARK") "dev")
        target-bookmark (or (System/getenv "MENTCI_COMMIT_TARGET") "jailCommit")
        message (when (= cmd "commit") (str/join " " (rest args)))]
    (when (= cmd "commit")
      (when (str/blank? message)
        (die {:message "Error: commit requires a message."})))
    (case cmd
      "status" (run-jj-status {})
      "log" (run-jj-log {})
      "commit" (run-jj-commit {:message message
                               :workingBookmark working-bookmark
                               :targetBookmark target-bookmark})
      (usage {}))))

(-main {:args (vec *command-line-args*)
        :command (or (first *command-line-args*) "")})
