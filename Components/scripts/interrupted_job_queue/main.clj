#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

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

(impl DefaultClassifier QueueClassifier normalize-for [:=> [:cat :any NormalizeInput] :string]
  [this input]
  (let [_ this]
    (-> (:prompt input)
        str/lower-case
        (str/replace #"[^a-z0-9]+" " ")
        str/trim)))

(impl DefaultClassifier QueueClassifier subject-for [:=> [:cat :any ClassifyInput] :string]
  [this input]
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

(impl DefaultClassifier QueueClassifier class-for [:=> [:cat :any ClassifyInput] :string]
  [this input]
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

(impl DefaultClassifier QueueClassifier priority-for [:=> [:cat :any ClassifyInput] :int]
  [this input]
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

(impl DefaultClassifier QueueClassifier acceptance-for [:=> [:cat :any ClassifyInput] :string]
  [this input]
  (let [class (class-for this {:prompt (:prompt input)})]
    (case class
      "direct-implementation" "Code/docs changes applied, validated, committed, and pushed."
      "strategy-only" "Strategy file updated with executable plan and tracked in Reports."
      "report-only" "Report artifact created/updated and indexed in subject README."
      "requires-confirmation" "Prompt intent confirmed with user, then classified and executed.")))

(defprotocol InterruptedJobQueueOps
  (fail-for [this input])
  (parse-args-for [this input])
  (extract-prompts-for [this input])
  (canonical-subject-for [this input])
  (execution-class-for [this input])
  (priority-tier-for [this input])
  (acceptance-criteria-for [this input])
  (build-jobs-for [this input])
  (dedup-jobs-for [this input])
  (render-queue-for [this input])
  (write-output-for [this input])
  (run-queue-for [this input]))

(defrecord DefaultInterruptedJobQueue [])

(def default-classifier (->DefaultClassifier))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps fail-for {:message :string} :any
  [this input]
  (let [_ this]
    (binding [*out* *err*]
      (println (:message input)))
    (System/exit 1)))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps parse-args-for ParseArgsInput :map
  [this input]
  (loop [remaining (:args input)
         opts {:source "Research/high/Prompt-Report-System/591912042315_answer_interrupted-jobs-audit.md"
               :output "Research/high/Prompt-Report-System/InterruptedJobQueue.md"
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
          (fail-for this {:message (str "Unknown argument: " arg)}))))))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps extract-prompts-for ExtractInput [:vector :string]
  [this input]
  (let [_ this
        lines (str/split-lines (:content input))]
    (->> lines
         (map str/trim)
         (filter #(re-find #"^\d+\.\s+`.+`$" %))
         (map #(second (re-find #"^\d+\.\s+`(.+)`$" %)))
         (remove str/blank?)
         vec)))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps canonical-subject-for ClassifierContext :string
  [this input]
  (subject-for (:classifier input) {:prompt (:prompt input)}))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps execution-class-for ClassifierContext :string
  [this input]
  (class-for (:classifier input) {:prompt (:prompt input)}))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps priority-tier-for ClassifierContext :int
  [this input]
  (priority-for (:classifier input) {:prompt (:prompt input)}))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps acceptance-criteria-for ClassifierContext :string
  [this input]
  (acceptance-for (:classifier input) {:prompt (:prompt input)}))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps build-jobs-for {:prompts [:vector :string]} [:vector :map]
  [this input]
  (->> (:prompts input)
       (map-indexed (fn [idx prompt]
                      {:jobId (str "IJ-" (format "%03d" (inc idx)))
                       :prompt prompt
                       :subject (canonical-subject-for this {:classifier default-classifier :prompt prompt})
                       :executionClass (execution-class-for this {:classifier default-classifier :prompt prompt})
                       :priorityTier (priority-tier-for this {:classifier default-classifier :prompt prompt})
                       :status "queued"
                       :acceptance (acceptance-criteria-for this {:classifier default-classifier :prompt prompt})
                       :normalizeKey (normalize-for default-classifier {:prompt prompt})}))
       vec))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps dedup-jobs-for DedupInput [:vector :map]
  [this input]
  (->> (:jobs input)
       (reduce (fn [acc job]
                 (if (some #(= (:normalizeKey %) (:normalizeKey job)) acc)
                   acc
                   (conj acc job)))
               [])
       (map-indexed (fn [idx job]
                      (assoc job :jobId (str "IJ-" (format "%03d" (inc idx))))))
       vec))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps render-queue-for RenderInput :string
  [this input]
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

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps write-output-for WriteInput :any
  [this input]
  (let [_ this]
    (io/make-parents (:path input))
    (spit (:path input) (:content input))))

(impl DefaultInterruptedJobQueue InterruptedJobQueueOps run-queue-for Input :any
  [this input]
  (let [{:keys [source output write?]} (parse-args-for this {:args (:args input)})
        source-file (io/file source)]
    (when-not (.exists source-file)
      (fail-for this {:message (str "Source report does not exist: " source)}))
    (let [prompts (extract-prompts-for this {:content (slurp source-file)})
          jobs (dedup-jobs-for this {:jobs (build-jobs-for this {:prompts prompts})})
          rendered (render-queue-for this {:jobs jobs :sourcePath source})]
      (if write?
        (do
          (write-output-for this {:path output :content rendered})
          (println (str "Wrote interrupted job queue: " output))
          (println (str "Recovered prompts: " (count prompts) ", queued jobs: " (count jobs))))
        (do
          (println (str "Dry run: would write " output))
          (println (str "Recovered prompts: " (count prompts) ", queued jobs: " (count jobs))))))))

(def default-interrupted-job-queue (->DefaultInterruptedJobQueue))

(main Input
  [input]
  (run-queue-for default-interrupted-job-queue input))

(-main {:args (vec *command-line-args*)})
