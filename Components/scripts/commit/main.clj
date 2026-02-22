#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Direct orchestration of VCS commands with cleaner error handling.

(enable!)

(def CommitMainInput
  [:map
   [:message :string]
   [:runtimePath :string]
   [:workingBookmark :string]
   [:targetBookmark :string]
   [:repoRoot :string]
   [:workspaceRoot :string]
   [:policyPath [:maybe :string]]])

(def ParseCommitArgsInput
  [:map
   [:args [:vector :string]]])

(def LoadRuntimeInput
  [:map
   [:runtimePath :string]])

(def LoadPolicyInput
  [:map
   [:policyPath [:maybe :string]]])

(def AllowedTargetInput
  [:map
   [:targetBookmark :string]
   [:allowedPushBookmarks [:set :string]]])

(def Input
  [:map
   [:args [:vector :string]]])

(defprotocol CommitOps
  (run-cli-for [this input]))

(defrecord DefaultCommit [])

(defn* parse-commit-args [:=> [:cat ParseCommitArgsInput] :map] [input]
  (let [args (:args input)]
    (if (and (>= (count args) 2) (= (first args) "--runtime"))
      {:runtimePath (second args)
       :message (str/join " " (drop 2 args))}
      {:runtimePath nil
       :message (str/join " " args)})))

(defn* default-runtime-path [:=> [:cat] :string] []
  (let [workspace-runtime (io/file "workspace/.mentci/runtime.json")
        local-runtime (io/file ".mentci/runtime.json")]
    (cond
      (.exists workspace-runtime) (.getPath workspace-runtime)
      (.exists local-runtime) (.getPath local-runtime)
      :else (.getPath workspace-runtime))))

(defn* load-runtime [:=> [:cat LoadRuntimeInput] :map] [input]
  (let [runtime-path (:runtimePath input)
        runtime-file (io/file runtime-path)]
    (when-not (.exists runtime-file)
      (println (str "Error: runtime file not found: " runtime-path))
      (System/exit 1))
    (json/parse-string (slurp runtime-file) true)))

(defn* load-allowed-push-bookmarks [:=> [:cat LoadPolicyInput] [:set :string]] [input]
  (let [policy-path (:policyPath input)]
    (if (str/blank? policy-path)
      #{}
      (let [policy-file (io/file policy-path)]
        (if-not (.exists policy-file)
          (do
            (println (str "Error: policyPath points to missing file: " policy-path))
            (System/exit 1))
          (let [policy (json/parse-string (slurp policy-file) true)
                bookmarks (get policy :allowedPushBookmarks [])]
            (set (map str bookmarks))))))))

(defn* assert-target-allowed [:=> [:cat AllowedTargetInput] :any] [input]
  (let [{:keys [targetBookmark allowedPushBookmarks]} input]
    (when (and (seq allowedPushBookmarks)
               (not (contains? allowedPushBookmarks targetBookmark)))
      (println (str "Error: target bookmark '" targetBookmark "' is not allowed by jail policy. Allowed: " (vec allowedPushBookmarks)))
      (System/exit 1))))

(defn* run-commit [:=> [:cat CommitMainInput] :any] [input]
  (let [{:keys [message workingBookmark targetBookmark repoRoot workspaceRoot policyPath]} input]
    (if (or (str/blank? repoRoot) (str/blank? workspaceRoot))
      (do (println "Error: runtime missing repoRoot or workspaceRoot.")
          (System/exit 1))
      (let [working-bookmark workingBookmark
            target-bookmark targetBookmark
            workspace-root workspaceRoot
            allowed-push-bookmarks (load-allowed-push-bookmarks {:policyPath policyPath})]
        (when (= working-bookmark target-bookmark)
          (println (str "Error: target bookmark '" target-bookmark "' must differ from working bookmark '" working-bookmark "'."))
          (System/exit 1))
        (assert-target-allowed {:targetBookmark target-bookmark
                                :allowedPushBookmarks allowed-push-bookmarks})
        (if (str/blank? message)
          (do (println "Usage: mentci-commit [--runtime <path>] <message>")
              (System/exit 1))
          (let [_ (println (str "Shipping manifestation from workspace: " message))
                res1 (sh "jj" "describe" "-m" message "-R" workspace-root)]
            (if (not= 0 (:exit res1))
              (do (println "Error during jj describe:" (:err res1))
                  (System/exit (:exit res1)))
              (let [res2 (sh "jj" "bookmark" "set" target-bookmark "-r" "@" "-R" workspace-root)]
                (if (not= 0 (:exit res2))
                  (do (println "Error during jj bookmark set:" (:err res2))
                      (System/exit (:exit res2)))
                  (println (str "Successfully committed and advanced bookmark '" target-bookmark "' from workspace.")))))))))))

(defn* run-cli [:=> [:cat Input] :any] [input]
  (let [args (:args input)
        {:keys [runtimePath message]} (parse-commit-args {:args args})
        runtime-path (or runtimePath (default-runtime-path))
        runtime (load-runtime {:runtimePath runtime-path})
        payload {:message message
                 :runtimePath runtime-path
                 :workingBookmark (get runtime :workingBookmark "dev")
                 :targetBookmark (get runtime :targetBookmark "jailCommit")
                 :repoRoot (get runtime :repoRoot "")
                 :workspaceRoot (get runtime :workspaceRoot "")
                 :policyPath (get runtime :policyPath nil)}]
    (run-commit payload)))

(impl DefaultCommit CommitOps run-cli-for
  [:=> [:cat :any Input] :any]
  [this input]
  (run-cli input))

(def default-commit (->DefaultCommit))

(main Input
  [input]
  (run-cli-for default-commit input))

(when (= *file* (System/getProperty "babashka.file"))
  (-main {:args (vec *command-line-args*)}))
