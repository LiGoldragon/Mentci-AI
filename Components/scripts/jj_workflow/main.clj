#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Centralized, safe JJ entrypoints with runtime-file driven workspace targeting.

(enable!)

(def DieInput [:map [:message :string]])
(def ParseArgsInput [:map [:args [:vector :string]]])
(def RuntimeInput [:map [:runtimePath :string]])
(def RunJJInput [:map [:runtime :map] [:args [:vector :string]]])
(def AllowedTargetInput [:map [:targetBookmark :string] [:allowedPushBookmarks [:set :string]]])
(def AllowedHostInput [:map [:host :string] [:allowedHosts [:set :string]]])
(def ExtractHostInput [:map [:remoteUrl :string]])

(def Input
  [:map
   [:args [:vector :string]]])

(defprotocol JJWorkflowOps
  (die-for [this input])
  (parse-args-for [this input])
  (default-runtime-path-for [this])
  (load-runtime-for [this input])
  (run-jj-for [this input])
  (load-allowed-push-bookmarks-for [this input])
  (load-allowed-git-hosts-for [this input])
  (assert-target-allowed-for [this input])
  (assert-host-allowed-for [this input])
  (extract-host-for [this input])
  (usage-for [this])
  (run-cli-for [this input]))

(defrecord DefaultJJWorkflow [])

(impl DefaultJJWorkflow JJWorkflowOps die-for DieInput :any
  [this input]
  (let [_ this]
    (println (:message input))
    (System/exit 1)))

(impl DefaultJJWorkflow JJWorkflowOps parse-args-for ParseArgsInput :map
  [this input]
  (let [_ this
        args (:args input)]
    (if (and (>= (count args) 3) (= (first args) "--runtime"))
      {:runtimePath (second args)
       :command (nth args 2 nil)
       :rest (vec (drop 3 args))}
      {:runtimePath nil
       :command (first args)
       :rest (vec (rest args))})))

(impl DefaultJJWorkflow JJWorkflowOps default-runtime-path-for [:=> [:cat :any] :string]
  [this]
  (let [workspace-runtime (io/file "workspace/.mentci/runtime.json")
        local-runtime (io/file ".mentci/runtime.json")]
    (cond
      (.exists workspace-runtime) (.getPath workspace-runtime)
      (.exists local-runtime) (.getPath local-runtime)
      :else (.getPath workspace-runtime))))

(impl DefaultJJWorkflow JJWorkflowOps load-runtime-for RuntimeInput :map
  [this input]
  (let [path (:runtimePath input)
        f (io/file path)]
    (when-not (.exists f)
      (die-for this {:message (str "Error: runtime file not found: " path)}))
    (json/parse-string (slurp f) true)))

(impl DefaultJJWorkflow JJWorkflowOps run-jj-for RunJJInput :any
  [this input]
  (let [root (get (:runtime input) :workspaceRoot)
        args (:args input)]
    (when (str/blank? root)
      (die-for this {:message "Error: runtime missing workspaceRoot."}))
    (apply sh "jj" (concat args ["-R" root]))))

(impl DefaultJJWorkflow JJWorkflowOps load-allowed-push-bookmarks-for RuntimeInput [:set :string]
  [this input]
  (let [runtime (load-runtime-for this input)
        policy-path (get runtime :policyPath)]
    (if (str/blank? policy-path)
      #{}
      (let [policy-file (io/file policy-path)]
        (when-not (.exists policy-file)
          (die-for this {:message (str "Error: policyPath points to missing file: " policy-path)}))
        (let [policy (json/parse-string (slurp policy-file) true)]
          (set (map str (get policy :allowedPushBookmarks []))))))))

(impl DefaultJJWorkflow JJWorkflowOps load-allowed-git-hosts-for RuntimeInput [:set :string]
  [this input]
  (let [runtime (load-runtime-for this input)
        policy-path (get runtime :policyPath)]
    (if (str/blank? policy-path)
      #{}
      (let [policy-file (io/file policy-path)]
        (when-not (.exists policy-file)
          (die-for this {:message (str "Error: policyPath points to missing file: " policy-path)}))
        (let [policy (json/parse-string (slurp policy-file) true)]
          (set (map str (get-in policy [:networkPolicy :allowedGitHosts] []))))))))

(impl DefaultJJWorkflow JJWorkflowOps assert-target-allowed-for AllowedTargetInput :any
  [this input]
  (let [{:keys [targetBookmark allowedPushBookmarks]} input]
    (when (and (seq allowedPushBookmarks)
               (not (contains? allowedPushBookmarks targetBookmark)))
      (die-for this {:message (str "Error: target bookmark '" targetBookmark "' is not allowed by jail policy. Allowed: " (vec allowedPushBookmarks))}))))

(impl DefaultJJWorkflow JJWorkflowOps assert-host-allowed-for AllowedHostInput :any
  [this input]
  (let [{:keys [host allowedHosts]} input]
    (when (and (seq allowedHosts)
               (not (contains? allowedHosts host)))
      (die-for this {:message (str "Error: git host '" host "' is not allowed by jail policy. Allowed: " (vec allowedHosts))}))))

(impl DefaultJJWorkflow JJWorkflowOps extract-host-for ExtractHostInput :string
  [this input]
  (let [_ this
        remote-url (:remoteUrl input)]
    (cond
      (str/starts-with? remote-url "ssh://")
      (let [rest (subs remote-url 6)
            rest (if (str/includes? rest "@") (second (str/split rest #"@" 2)) rest)
            host-port (first (str/split rest #"/" 2))
            host (first (str/split host-port #":" 2))]
        host)

      (re-find #"^[^@]+@[^:]+:.+$" remote-url)
      (let [after-at (second (str/split remote-url #"@" 2))]
        (first (str/split after-at #":" 2)))

      :else "")))

(impl DefaultJJWorkflow JJWorkflowOps usage-for [:=> [:cat :any] :any]
  [this]
  (let [_ this]
    (println "Usage: mentci-jj [--runtime <path>] <command> [args]")
    (println "")
    (println "Commands:")
    (println "  status")
    (println "  log")
    (println "  commit <message>")
    (println "  push [remote] [bookmark]")))

(impl DefaultJJWorkflow JJWorkflowOps run-cli-for Input :any
  [this input]
  (let [args (:args input)
        {:keys [runtimePath command rest]} (parse-args-for this {:args args})
        runtime-path (or runtimePath (default-runtime-path-for this))
        runtime (load-runtime-for this {:runtimePath runtime-path})
        working-bookmark (get runtime :workingBookmark "dev")
        target-bookmark (get runtime :targetBookmark "jailCommit")
        policy-in {:runtimePath runtime-path}
        allowed-push-bookmarks (load-allowed-push-bookmarks-for this policy-in)]
    (case command
      "status"
      (let [res (run-jj-for this {:runtime runtime :args ["status"]})]
        (if (zero? (:exit res)) (print (:out res)) (die-for this {:message (str "Error during jj status: " (:err res))})))

      "log"
      (let [res (run-jj-for this {:runtime runtime :args ["log" "--no-signing"]})]
        (if (zero? (:exit res))
          (print (:out res))
          (let [fallback (run-jj-for this {:runtime runtime :args ["log"]})]
            (if (zero? (:exit fallback))
              (print (:out fallback))
              (die-for this {:message (str "Error during jj log: " (:err fallback))})))))

      "commit"
      (let [message (str/join " " rest)]
        (when (str/blank? message)
          (die-for this {:message "Error: commit requires a message."}))
        (when (= working-bookmark target-bookmark)
          (die-for this {:message (str "Error: target bookmark '" target-bookmark "' must differ from working bookmark '" working-bookmark "'.")}))
        (assert-target-allowed-for this {:targetBookmark target-bookmark
                                         :allowedPushBookmarks allowed-push-bookmarks})
        (let [res1 (run-jj-for this {:runtime runtime :args ["describe" "-m" message]})]
          (if (not= 0 (:exit res1))
            (die-for this {:message (str "Error during jj describe: " (:err res1))})
            (let [res2 (run-jj-for this {:runtime runtime :args ["bookmark" "set" target-bookmark "-r" "@"]})]
              (if (not= 0 (:exit res2))
                (die-for this {:message (str "Error during jj bookmark set: " (:err res2))})
                (println (str "Successfully committed and advanced bookmark '" target-bookmark "'.")))))))

      "push"
      (let [remote (or (first rest) "origin")
            bookmark (or (second rest) target-bookmark)
            git-res (sh "git" "-C" (get runtime :workspaceRoot "") "remote" "get-url" remote)]
        (when (not= 0 (:exit git-res))
          (die-for this {:message (str "Error reading git remote URL: " (:err git-res))}))
        (let [remote-url (str/trim (:out git-res))
              host (extract-host-for this {:remoteUrl remote-url})
              allowed-hosts (load-allowed-git-hosts-for this policy-in)]
          (assert-host-allowed-for this {:host host :allowedHosts allowed-hosts})
          (let [res (run-jj-for this {:runtime runtime :args ["git" "push" "--bookmark" bookmark "--remote" remote]})]
            (if (zero? (:exit res))
              (print (:out res))
              (die-for this {:message (str "Error during jj git push: " (:err res))})))))

      (usage-for this))))

(def default-jj-workflow (->DefaultJJWorkflow))

(main Input
  [input]
  (run-cli-for default-jj-workflow input))

(when (= *file* (System/getProperty "babashka.file"))
  (-main {:args (vec *command-line-args*)}))
