#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def RunCommandInput
  [:map
   [:args [:vector :string]]])

(def FailInput
  [:map
   [:message :string]])

(def ReadTextInput
  [:map
   [:value [:maybe :string]]
   [:path [:maybe :string]]
   [:label :string]])

(def ResolveRevInput
  [:map
   [:rev :string]])

(def BuildMessageInput
  [:map
   [:summary :string]
   [:solarLine :string]
   [:prompt :string]
   [:context :string]
   [:changes [:vector :string]]])

(def DescribeInput
  [:map
   [:rev :string]
   [:message :string]])

(def BookmarkInput
  [:map
   [:bookmark :string]
   [:rev :string]])

(def PushInput
  [:map
   [:bookmark :string]
   [:remote :string]])

(def VerifyPushInput
  [:map
   [:bookmark :string]
   [:remote :string]])

(defprotocol SessionFinalizeOps
  (fail-for [this input])
  (run-command-for [this input])
  (parse-args-for [this input])
  (read-text-for [this input])
  (read-solar-line-for [this])
  (resolve-target-rev-for [this input])
  (build-session-message-for [this input])
  (validate-session-message-for [this input])
  (describe-rev-for [this input])
  (set-bookmark-for [this input])
  (push-bookmark-for [this input])
  (verify-push-for [this input])
  (run-finalize-for [this input]))

(defrecord DefaultSessionFinalize [])

(impl DefaultSessionFinalize SessionFinalizeOps fail-for FailInput :any
  [this input]
  (let [_ this]
    (binding [*out* *err*]
      (println (:message input)))
    (System/exit 1)))

(impl DefaultSessionFinalize SessionFinalizeOps run-command-for RunCommandInput :map
  [this input]
  (let [_ this]
    (try
      (let [result (apply sh (:args input))]
        {:exit (:exit result)
         :out (:out result)
         :err (:err result)})
      (catch java.io.IOException e
        {:exit 127
         :out ""
         :err (.getMessage e)}))))

(impl DefaultSessionFinalize SessionFinalizeOps parse-args-for ParseArgsInput :map
  [this input]
  (loop [remaining (:args input)
         opts {:prompt nil
               :prompt-file nil
               :context nil
               :context-file nil
               :summary nil
               :changes []
               :bookmark "dev"
               :remote "origin"
               :rev "@"
               :no-push? false}]
    (if (empty? remaining)
      opts
      (let [[arg & _] remaining
            value (second remaining)]
        (cond
          (= arg "--prompt")
          (recur (nnext remaining) (assoc opts :prompt value))

          (= arg "--prompt-file")
          (recur (nnext remaining) (assoc opts :prompt-file value))

          (= arg "--context")
          (recur (nnext remaining) (assoc opts :context value))

          (= arg "--context-file")
          (recur (nnext remaining) (assoc opts :context-file value))

          (= arg "--summary")
          (recur (nnext remaining) (assoc opts :summary value))

          (= arg "--change")
          (recur (nnext remaining) (update opts :changes conj value))

          (= arg "--bookmark")
          (recur (nnext remaining) (assoc opts :bookmark value))

          (= arg "--remote")
          (recur (nnext remaining) (assoc opts :remote value))

          (= arg "--rev")
          (recur (nnext remaining) (assoc opts :rev value))

          (= arg "--no-push")
          (recur (next remaining) (assoc opts :no-push? true))

          (= arg "--help")
          (do
            (println "Usage: bb Components/scripts/session_finalize/main.clj \\")
            (println "  --summary <text> --prompt <text>|--prompt-file <path> \\")
            (println "  --context <text>|--context-file <path> --change <text> [--change <text> ...] \\")
            (println "  [--bookmark dev] [--remote origin] [--rev @] [--no-push]")
            (System/exit 0))

          :else
          (fail-for this {:message (str "Unknown argument: " arg)}))))))

(impl DefaultSessionFinalize SessionFinalizeOps read-text-for ReadTextInput :string
  [this input]
  (let [{:keys [value path label]} input]
    (cond
      (and value (not (str/blank? value))) value
      (and path (.exists (io/file path))) (slurp path)
      :else (fail-for this {:message (str "Missing required " label " (--" label " or --" label "-file).")}))))

(impl DefaultSessionFinalize SessionFinalizeOps read-solar-line-for [:=> [:cat :any] :string]
  [this]
  (let [attempts [["chronos" "--precision" "second"]
                  ["cargo" "run" "--quiet" "--manifest-path" "Components/chronos/Cargo.toml" "--bin" "chronos" "--" "--precision" "second"]]
        results (map #(run-command-for this {:args %}) attempts)
        success (first (filter #(zero? (:exit %)) results))]
    (when-not success
      (fail-for this {:message (str "Failed to resolve solar timestamp for session message.\n"
                                    (str/join "\n"
                                              (map (fn [result cmd]
                                                     (str "- " (str/join " " cmd)
                                                          " (exit " (:exit result) ")"
                                                          (when-not (str/blank? (str (:err result)))
                                                            (str ": " (str/trim (:err result))))))
                                                   results
                                                   attempts)))}))
    (let [raw (str/trim (:out success))
          unicode-match (re-find #"^(.+?)(\d+)°(\d+)'(\d+)\"\s+\|\s+(\d+)\s+AM$" raw)
          am-match (re-find #"^(\d+)\.(\d+)\.(\d+)\.(\d+)\.(\d+)$" raw)]
      (cond
        unicode-match
        (let [[_ sign degree minute second year] unicode-match]
          (str sign "." degree "." minute "." second " " year "AM"))

        am-match
        (let [[_ year sign degree minute second] am-match]
          (str sign "." degree "." minute "." second " " year "AM"))

        :else
        (fail-for this {:message (str "Unexpected chronos output: " raw)})))))

(impl DefaultSessionFinalize SessionFinalizeOps resolve-target-rev-for ResolveRevInput :string
  [this input]
  (let [rev (:rev input)
        result (run-command-for this {:args ["jj" "log" "-r" rev "--no-graph" "-T" "if(empty, \"true\", \"false\") ++ \"|\" ++ description.first_line() ++ \"\\n\""]})]
    (when-not (zero? (:exit result))
      (fail-for this {:message (str "Unable to inspect revision " rev ":\n" (:err result))}))
    (let [[empty? desc] (str/split (str/trim (:out result)) #"\|" 2)
          blank-desc? (str/blank? (or desc ""))]
      (if (or (= "true" empty?) blank-desc?)
        (if (= rev "@")
          "@-"
          (fail-for this {:message (str "Requested revision is empty or has blank description: " rev)}))
        rev))))

(impl DefaultSessionFinalize SessionFinalizeOps build-session-message-for BuildMessageInput :string
  [this input]
  (let [{:keys [summary solarLine prompt context changes]} input
        changes (->> changes (remove str/blank?) vec)]
    (when (str/blank? summary)
      (fail-for this {:message "Missing required --summary."}))
    (when (empty? changes)
      (fail-for this {:message "At least one --change is required."}))
    (str "session: " summary "\n"
         solarLine "\n\n"
         "## Original Prompt\n"
         prompt "\n\n"
         "## Agent Context\n"
         context "\n\n"
         "## Logical Changes\n"
         (str/join "\n" (map #(str "- " %) changes)) "\n")))

(impl DefaultSessionFinalize SessionFinalizeOps validate-session-message-for {:message :string} :any
  [this input]
  (let [message (:message input)
        missing (->> ["## Original Prompt" "## Agent Context" "## Logical Changes"]
                     (remove #(str/includes? message %))
                     vec)]
    (when-not (str/starts-with? message "session:")
      (fail-for this {:message "Final session message must start with `session:`."}))
    (when (seq missing)
      (fail-for this {:message (str "Session message missing required sections: " (str/join ", " missing))}))))

(impl DefaultSessionFinalize SessionFinalizeOps describe-rev-for DescribeInput :any
  [this input]
  (let [result (run-command-for this {:args ["jj" "describe" "-r" (:rev input) "-m" (:message input)]})]
    (when-not (zero? (:exit result))
      (fail-for this {:message (str "Failed to describe revision " (:rev input) ":\n" (:err result))}))))

(impl DefaultSessionFinalize SessionFinalizeOps set-bookmark-for BookmarkInput :any
  [this input]
  (let [result (run-command-for this {:args ["jj" "bookmark" "set" (:bookmark input) "-r" (:rev input) "--allow-backwards"]})]
    (when-not (zero? (:exit result))
      (fail-for this {:message (str "Failed to set bookmark " (:bookmark input) " to " (:rev input) ":\n" (:err result))}))))

(impl DefaultSessionFinalize SessionFinalizeOps push-bookmark-for PushInput :any
  [this input]
  (let [result (run-command-for this {:args ["jj" "git" "push" "--bookmark" (:bookmark input) "--remote" (:remote input)]})]
    (when-not (zero? (:exit result))
      (fail-for this {:message (str "Failed to push bookmark " (:bookmark input) " to " (:remote input) ":\n" (:err result))}))))

(impl DefaultSessionFinalize SessionFinalizeOps verify-push-for VerifyPushInput :any
  [this input]
  (let [{:keys [bookmark remote]} input
        local (run-command-for this {:args ["jj" "log" "-r" (str "bookmarks(\"" bookmark "\")") "--no-graph" "-T" "commit_id ++ \"\\n\""]})
        remote-line (run-command-for this {:args ["git" "ls-remote" "--heads" remote bookmark]})]
    (when-not (zero? (:exit local))
      (fail-for this {:message (str "Failed to resolve local bookmark commit: " bookmark "\n" (:err local))}))
    (when-not (zero? (:exit remote-line))
      (fail-for this {:message (str "Failed to resolve remote bookmark: " remote "/" bookmark "\n" (:err remote-line))}))
    (let [local-hash (str/trim (:out local))
          remote-hash (first (str/split (str/trim (:out remote-line)) #"\s+"))]
      (when (or (str/blank? remote-hash) (not= local-hash remote-hash))
        (fail-for this {:message (str "Push verification failed for " bookmark ".\n"
                                      "local: " local-hash "\n"
                                      "remote: " (or remote-hash "<missing>"))})))))

(impl DefaultSessionFinalize SessionFinalizeOps run-finalize-for Input :any
  [this input]
  (let [{:keys [summary prompt prompt-file context context-file changes bookmark remote rev no-push?]}
        (parse-args-for this {:args (:args input)})
        prompt-text (read-text-for this {:value prompt :path prompt-file :label "prompt"})
        context-text (read-text-for this {:value context :path context-file :label "context"})
        solar-line (read-solar-line-for this)
        message (build-session-message-for this {:summary summary
                                                 :solarLine solar-line
                                                 :prompt prompt-text
                                                 :context context-text
                                                 :changes changes})
        _ (validate-session-message-for this {:message message})
        target-rev (resolve-target-rev-for this {:rev rev})]
    (describe-rev-for this {:rev target-rev :message message})
    (set-bookmark-for this {:bookmark bookmark :rev target-rev})
    (when-not no-push?
      (push-bookmark-for this {:bookmark bookmark :remote remote})
      (verify-push-for this {:bookmark bookmark :remote remote}))
    (println (str "Finalized session commit on rev " target-rev " and bookmark " bookmark
                  (if no-push? " (no push)." ".")))))

(def default-session-finalize (->DefaultSessionFinalize))

(main Input
  [input]
  (run-finalize-for default-session-finalize input))

(-main {:args (vec *command-line-args*)})
