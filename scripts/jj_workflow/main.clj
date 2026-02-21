#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParent (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParent (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

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

(defn* die [:=> [:cat DieInput] :any] [input]
  (println (:message input))
  (System/exit 1))

(defn* parse-args [:=> [:cat ParseArgsInput] :map] [input]
  (let [args (:args input)]
    (if (and (>= (count args) 3) (= (first args) "--runtime"))
      {:runtimePath (second args)
       :command (nth args 2 nil)
       :rest (vec (drop 3 args))}
      {:runtimePath nil
       :command (first args)
       :rest (vec (rest args))})))

(defn* default-runtime-path [:=> [:cat] :string] []
  (let [workspace-runtime (io/file "workspace/.mentci/runtime.json")
        local-runtime (io/file ".mentci/runtime.json")]
    (cond
      (.exists workspace-runtime) (.getPath workspace-runtime)
      (.exists local-runtime) (.getPath local-runtime)
      :else (.getPath workspace-runtime))))

(defn* load-runtime [:=> [:cat RuntimeInput] :map] [input]
  (let [path (:runtimePath input)
        f (io/file path)]
    (when-not (.exists f)
      (die {:message (str "Error: runtime file not found: " path)}))
    (json/parse-string (slurp f) true)))

(defn* run-jj [:=> [:cat RunJJInput] :any] [input]
  (let [root (get (:runtime input) :workspaceRoot)
        args (:args input)]
    (when (str/blank? root)
      (die {:message "Error: runtime missing workspaceRoot."}))
    (apply sh "jj" (concat args ["-R" root]))))

(defn* load-allowed-push-bookmarks [:=> [:cat RuntimeInput] [:set :string]] [input]
  (let [runtime (load-runtime input)
        policy-path (get runtime :policyPath)]
    (if (str/blank? policy-path)
      #{}
      (let [policy-file (io/file policy-path)]
        (when-not (.exists policy-file)
          (die {:message (str "Error: policyPath points to missing file: " policy-path)}))
        (let [policy (json/parse-string (slurp policy-file) true)]
          (set (map str (get policy :allowedPushBookmarks []))))))))

(defn* load-allowed-git-hosts [:=> [:cat RuntimeInput] [:set :string]] [input]
  (let [runtime (load-runtime input)
        policy-path (get runtime :policyPath)]
    (if (str/blank? policy-path)
      #{}
      (let [policy-file (io/file policy-path)]
        (when-not (.exists policy-file)
          (die {:message (str "Error: policyPath points to missing file: " policy-path)}))
        (let [policy (json/parse-string (slurp policy-file) true)]
          (set (map str (get-in policy [:networkPolicy :allowedGitHosts] []))))))))

(defn* assert-target-allowed [:=> [:cat AllowedTargetInput] :any] [input]
  (let [{:keys [targetBookmark allowedPushBookmarks]} input]
    (when (and (seq allowedPushBookmarks)
               (not (contains? allowedPushBookmarks targetBookmark)))
      (die {:message (str "Error: target bookmark '" targetBookmark "' is not allowed by jail policy. Allowed: " (vec allowedPushBookmarks))}))))

(defn* assert-host-allowed [:=> [:cat AllowedHostInput] :any] [input]
  (let [{:keys [host allowedHosts]} input]
    (when (and (seq allowedHosts)
               (not (contains? allowedHosts host)))
      (die {:message (str "Error: git host '" host "' is not allowed by jail policy. Allowed: " (vec allowedHosts))}))))

(defn* extract-host [:=> [:cat [:map [:remoteUrl :string]]] :string] [input]
  (let [remote-url (:remoteUrl input)]
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

(defn* usage [:=> [:cat] :any] []
  (println "Usage: mentci-jj [--runtime <path>] <command> [args]")
  (println "")
  (println "Commands:")
  (println "  status")
  (println "  log")
  (println "  commit <message>")
  (println "  push [remote] [bookmark]"))

(defn* -main [:=> [:cat [:* :string]] :any] [& args]
  (let [{:keys [runtimePath command rest]} (parse-args {:args (vec args)})
        runtime-path (or runtimePath (default-runtime-path))
        runtime (load-runtime {:runtimePath runtime-path})
        working-bookmark (get runtime :workingBookmark "dev")
        target-bookmark (get runtime :targetBookmark "jailCommit")
        policy-in {:runtimePath runtime-path}
        allowed-push-bookmarks (load-allowed-push-bookmarks policy-in)]
    (case command
      "status"
      (let [res (run-jj {:runtime runtime :args ["status"]})]
        (if (zero? (:exit res)) (print (:out res)) (die {:message (str "Error during jj status: " (:err res))})))

      "log"
      (let [res (run-jj {:runtime runtime :args ["log" "--no-signing"]})]
        (if (zero? (:exit res))
          (print (:out res))
          (let [fallback (run-jj {:runtime runtime :args ["log"]})]
            (if (zero? (:exit fallback))
              (print (:out fallback))
              (die {:message (str "Error during jj log: " (:err fallback))})))))

      "commit"
      (let [message (str/join " " rest)]
        (when (str/blank? message)
          (die {:message "Error: commit requires a message."}))
        (when (= working-bookmark target-bookmark)
          (die {:message (str "Error: target bookmark '" target-bookmark "' must differ from working bookmark '" working-bookmark "'.")}))
        (assert-target-allowed {:targetBookmark target-bookmark
                                :allowedPushBookmarks allowed-push-bookmarks})
        (let [res1 (run-jj {:runtime runtime :args ["describe" "-m" message]})]
          (if (not= 0 (:exit res1))
            (die {:message (str "Error during jj describe: " (:err res1))})
            (let [res2 (run-jj {:runtime runtime :args ["bookmark" "set" target-bookmark "-r" "@"]})]
              (if (not= 0 (:exit res2))
                (die {:message (str "Error during jj bookmark set: " (:err res2))})
                (println (str "Successfully committed and advanced bookmark '" target-bookmark "'.")))))))

      "push"
      (let [remote (or (first rest) "origin")
            bookmark (or (second rest) target-bookmark)
            git-res (sh "git" "-C" (get runtime :workspaceRoot "") "remote" "get-url" remote)]
        (when (not= 0 (:exit git-res))
          (die {:message (str "Error reading git remote URL: " (:err git-res))}))
        (let [remote-url (str/trim (:out git-res))
              host (extract-host {:remoteUrl remote-url})
              allowed-hosts (load-allowed-git-hosts policy-in)]
          (assert-host-allowed {:host host :allowedHosts allowed-hosts})
          (let [res (run-jj {:runtime runtime :args ["git" "push" "--bookmark" bookmark "--remote" remote]})]
            (if (zero? (:exit res))
              (print (:out res))
              (die {:message (str "Error during jj git push: " (:err res))})))))

      (usage))))

(-main (vec *command-line-args*))
