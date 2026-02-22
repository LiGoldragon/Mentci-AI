#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

(enable!)

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def Input
  [:map
   [:args [:vector :string]]])

(def ExtractInput
  [:map
   [:content :string]])

(def ClassifyInput
  [:map
   [:prompt :string]])

(def NormalizeInput
  [:map
   [:prompt :string]])

(def DedupInput
  [:map
   [:jobs [:vector :map]]])

(def RenderInput
  [:map
   [:jobs [:vector :map]]
   [:sourcePath :string]])

(def WriteInput
  [:map
   [:path :string]
   [:content :string]])

(def ClassifierContext
  [:map
   [:classifier :any]
   [:prompt :string]])

(defprotocol QueueClassifier
  (subject-for [this input])
  (class-for [this input])
  (priority-for [this input])
  (acceptance-for [this input])
  (normalize-for [this input]))

(defrecord DefaultClassifier [])

(defn* fail! [:=> [:cat [:map [:message :string]]] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* parse-args [:=> [:cat ParseArgsInput] :map] [input]
  (loop [remaining (:args input)
         opts {:source "Reports/Prompt-Report-System/20260222_answer_interrupted-jobs-audit.md"
               :output "Reports/Prompt-Report-System/INTERRUPTED_JOB_QUEUE.md"
               :write? false}]
    (if (empty? remaining)
      opts
      (let [[arg & _] remaining]
        (cond
          (= arg "--source-report")
          (recur (nnext remaining) (assoc opts :source (second remaining)))

          (= arg "--output")
          (recur (nnext remaining) (assoc opts :output (second remaining)))

          (= arg "--write")
          (recur (next remaining) (assoc opts :write? true))

          (= arg "--help")
          (do
            (println "Usage: bb Components/scripts/interrupted_job_queue/main.clj [--source-report <path>] [--output <path>] [--write]")
            (System/exit 0))

          :else
          (fail! {:message (str "Unknown argument: " arg)}))))))

(defn* extract-prompts [:=> [:cat ExtractInput] [:vector :string]] [input]
  (let [lines (str/split-lines (:content input))]
    (->> lines
         (map str/trim)
         (filter #(re-find #"^\d+\.\s+`.+`$" %))
         (map #(second (re-find #"^\d+\.\s+`(.+)`$" %)))
         (remove str/blank?)
         vec)))

(defn* normalize-key [:=> [:cat NormalizeInput] :string] [input]
  (-> (:prompt input)
      str/lower-case
      (str/replace #"[^a-z0-9]+" " ")
      str/trim))

(impl DefaultClassifier QueueClassifier normalize-for [:=> [:cat :any NormalizeInput] :string] [this input]
  (let [_ this]
    (normalize-key input)))

(impl DefaultClassifier QueueClassifier subject-for [:=> [:cat :any ClassifyInput] :string] [this input]
  (let [_ this
        p (str/lower-case (:prompt input))]
    (cond
      (or (str/includes? p "stt")
          (str/includes? p "voiceprompt")
          (str/includes? p "audio"))
      "STT-Context-Decoding"

      (or (str/includes? p "commit")
          (str/includes? p "session commit")
          (str/includes? p "push")
          (str/includes? p "tag")
          (str/includes? p "release"))
      "Commit-Protocol-Merge-Fanin"

      (or (str/includes? p "history purge")
          (str/includes? p "workspace/")
          (str/includes? p "$(pwd)")
          (str/includes? p "rewrite")
          (str/includes? p "inputs_backup"))
      "Workspace-Pwd-History-Purge-And-Jj-Config-Migration"

      (or (str/includes? p "top level")
          (str/includes? p "core/")
          (str/includes? p "library/")
          (str/includes? p "components/")
          (str/includes? p "fs "))
      "Top-Level-FS-Spec"

      (or (str/includes? p "strategy")
          (str/includes? p "strategize"))
      "Strategy-Development"

      :else
      "Prompt-Report-System")))

(impl DefaultClassifier QueueClassifier class-for [:=> [:cat :any ClassifyInput] :string] [this input]
  (let [_ this
        p (str/lower-case (:prompt input))]
    (cond
      (or (str/includes? p "implement")
          (str/includes? p "fix")
          (str/includes? p "move")
          (str/includes? p "remove")
          (str/includes? p "run tests"))
      "direct-implementation"

      (or (str/includes? p "strategy")
          (str/includes? p "strategize"))
      "strategy-only"

      (str/includes? p "report")
      "report-only"

      :else
      "requires-confirmation")))

(impl DefaultClassifier QueueClassifier priority-for [:=> [:cat :any ClassifyInput] :int] [this input]
  (let [_ this
        p (str/lower-case (:prompt input))]
    (cond
      (or (str/includes? p "session")
          (str/includes? p "commit")
          (str/includes? p "push")
          (str/includes? p "release")
          (str/includes? p "protocol"))
      1

      (or (str/includes? p "purge")
          (str/includes? p "history")
          (str/includes? p "rewrite")
          (str/includes? p "recover"))
      2

      :else
      3)))

(impl DefaultClassifier QueueClassifier acceptance-for [:=> [:cat :any ClassifyInput] :string] [this input]
  (let [class (class-for this {:prompt (:prompt input)})]
    (case class
      "direct-implementation" "Code/docs changes applied, validated, committed, and pushed."
      "strategy-only" "Strategy file updated with executable plan and tracked in Reports."
      "report-only" "Report artifact created/updated and indexed in subject README."
      "requires-confirmation" "Prompt intent confirmed with user, then classified and executed.")))

(defn* canonical-subject [:=> [:cat ClassifierContext] :string] [input]
  (subject-for (:classifier input) {:prompt (:prompt input)}))

(defn* execution-class [:=> [:cat ClassifierContext] :string] [input]
  (class-for (:classifier input) {:prompt (:prompt input)}))

(defn* priority-tier [:=> [:cat ClassifierContext] :int] [input]
  (priority-for (:classifier input) {:prompt (:prompt input)}))

(defn* acceptance-criteria [:=> [:cat ClassifierContext] :string] [input]
  (acceptance-for (:classifier input) {:prompt (:prompt input)}))

(def default-classifier (->DefaultClassifier))

(defn* build-jobs [:=> [:cat [:map [:prompts [:vector :string]]]] [:vector :map]] [input]
  (->> (:prompts input)
       (map-indexed (fn [idx prompt]
                      {:jobId (str "IJ-" (format "%03d" (inc idx)))
                       :prompt prompt
                       :subject (canonical-subject {:classifier default-classifier :prompt prompt})
                       :executionClass (execution-class {:classifier default-classifier :prompt prompt})
                       :priorityTier (priority-tier {:classifier default-classifier :prompt prompt})
                       :status "queued"
                       :acceptance (acceptance-criteria {:classifier default-classifier :prompt prompt})
                       :normalizeKey (normalize-for default-classifier {:prompt prompt})}))
       vec))

(defn* dedup-jobs [:=> [:cat DedupInput] [:vector :map]] [input]
  (->> (:jobs input)
       (reduce (fn [acc job]
                 (if (some #(= (:normalizeKey %) (:normalizeKey job)) acc)
                   acc
                   (conj acc job)))
               [])
       (map-indexed (fn [idx job]
                      (assoc job :jobId (str "IJ-" (format "%03d" (inc idx))))))
       vec))

(defn* render-queue [:=> [:cat RenderInput] :string] [input]
  (let [jobs (->> (:jobs input)
                  (sort-by (juxt :priorityTier :jobId))
                  vec)
        rows (->> jobs
                  (map (fn [job]
                         (str "| " (:jobId job)
                              " | " (:priorityTier job)
                              " | " (:status job)
                              " | " (:subject job)
                              " | " (:executionClass job)
                              " | " (str/replace (:prompt job) "|" "\\|")
                              " | " (:acceptance job) " |")))
                  (str/join "\n"))]
    (str "# Interrupted Job Queue\n\n"
         "- Source Report: `" (:sourcePath input) "`\n"
         "- Status Legend: `queued | in_progress | done | blocked`\n"
         "- Priority: `1=protocol integrity`, `2=history integrity`, `3=feature/other`\n"
         "- Last Refresh: generated by `bb Components/scripts/interrupted_job_queue/main.clj`\n\n"
         "## Queue\n\n"
         "| Job ID | P | Status | Subject | Class | Recovered Prompt | Acceptance Criteria |\n"
         "| --- | --- | --- | --- | --- | --- | --- |\n"
         rows
         "\n")))

(defn* write-output! [:=> [:cat WriteInput] :any] [input]
  (io/make-parents (:path input))
  (spit (:path input) (:content input)))

(main Input
  [input]
  (let [{:keys [source output write?]} (parse-args {:args (:args input)})
        source-file (io/file source)]
    (when-not (.exists source-file)
      (fail! {:message (str "Source report does not exist: " source)}))
    (let [prompts (extract-prompts {:content (slurp source-file)})
          jobs (dedup-jobs {:jobs (build-jobs {:prompts prompts})})
          rendered (render-queue {:jobs jobs :sourcePath source})]
      (if write?
        (do
          (write-output! {:path output :content rendered})
          (println (str "Wrote interrupted job queue: " output))
          (println (str "Recovered prompts: " (count prompts) ", queued jobs: " (count jobs))))
        (do
          (println (str "Dry run: would write " output))
          (println (str "Recovered prompts: " (count prompts) ", queued jobs: " (count jobs))))))))

(-main {:args (vec *command-line-args*)})
