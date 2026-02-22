#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :refer [sh]])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def ParseArgsInput
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

(def ChronosInput
  [:map
   [:rawOverride [:maybe :string]]])

(def ParseChronosRawInput
  [:map
   [:raw :string]])

(def ChronosAttemptInput
  [:map
   [:label :string]
   [:argv [:vector :string]]])

(def ChronosFailureInput
  [:map
   [:attempts [:vector :map]]])

(def BuildFilenameInput
  [:map
   [:year :string]
   [:sign :string]
   [:degree :string]
   [:minute :string]
   [:second :string]
   [:kind [:enum "answer" "draft" "question"]]
   [:title :string]
   [:tier :string]
   [:subject :string]])

(def EnsureUniquePathInput
  [:map
   [:path :string]])

(def WriteReportInput
  [:map
   [:path :string]
   [:chronosRaw :string]
   [:kind [:enum "answer" "draft" "question"]]
   [:changeScope [:enum "modified-files" "no-files"]]
   [:title :string]
   [:subject :string]
   [:prompt :string]
   [:answer :string]])

(def EnsureTopicReadmeInput
  [:map
   [:tier :string]
   [:subject :string]])

(defprotocol AnswerReportOps
  (fail-for [this input])
  (parse-args-for [this input])
  (read-text-for [this input])
  (pad2-for [this input])
  (safe-title-for [this input])
  (canonical-segment-for [this input])
  (canonical-subject-for [this input])
  (parse-chronos-raw-for [this input])
  (run-chronos-attempt-for [this input])
  (summarize-chronos-failure-for [this input])
  (read-chronos-for [this input])
  (build-filename-for [this input])
  (ensure-unique-path-for [this input])
  (ensure-topic-readme-for [this input])
  (write-report-for [this input])
  (run-report-for [this input]))

(defrecord DefaultAnswerReport [])

(def TierNames ["high" "medium" "low"])

(defn subject-tier-for [subject]
  (or (some (fn [tier]
              (when (.exists (io/file (str "Development/" tier "/" subject)))
                tier))
            TierNames)
      "high"))

(impl DefaultAnswerReport AnswerReportOps fail-for FailInput :any
  [this input]
  (let [_ this]
    (binding [*out* *err*]
      (println (:message input)))
    (System/exit 1)))

(impl DefaultAnswerReport AnswerReportOps parse-args-for ParseArgsInput :map
  [this input]
  (loop [remaining (:args input)
         opts {:kind "answer"
               :change-scope "modified-files"
               :title "agent-answer"}]
    (if (empty? remaining)
      opts
      (let [[arg & _more] remaining
            value (second remaining)]
        (cond
          (= arg "--prompt")
          (recur (nnext remaining) (assoc opts :prompt value))

          (= arg "--prompt-file")
          (recur (nnext remaining) (assoc opts :prompt-file value))

          (= arg "--answer")
          (recur (nnext remaining) (assoc opts :answer value))

          (= arg "--answer-file")
          (recur (nnext remaining) (assoc opts :answer-file value))

          (= arg "--kind")
          (recur (nnext remaining) (assoc opts :kind value))

          (= arg "--change-scope")
          (recur (nnext remaining) (assoc opts :change-scope value))

          (= arg "--title")
          (recur (nnext remaining) (assoc opts :title value))

          (= arg "--subject")
          (recur (nnext remaining) (assoc opts :subject value))

          (= arg "--chronos-raw")
          (recur (nnext remaining) (assoc opts :chronos-raw value))

          :else
          (fail-for this {:message (str "Unknown argument: " arg)}))))))

(impl DefaultAnswerReport AnswerReportOps read-text-for ReadTextInput :string
  [this input]
  (let [{:keys [value path label]} input]
    (cond
      (and value (not (str/blank? value))) value
      (and path (.exists (io/file path))) (slurp path)
      :else (fail-for this {:message (str "Missing required " label " (--" label " or --" label "-file).")}))))

(impl DefaultAnswerReport AnswerReportOps pad2-for {:value :string} :string
  [this input]
  (format "%02d" (Integer/parseInt (:value input))))

(impl DefaultAnswerReport AnswerReportOps safe-title-for {:value :string} :string
  [this input]
  (let [_ this
        v (-> (:value input)
              str/lower-case
              (str/replace #"[^a-z0-9]+" "-")
              (str/replace #"^-+" "")
              (str/replace #"-+$" ""))]
    (if (str/blank? v) "agent-answer" v)))

(impl DefaultAnswerReport AnswerReportOps canonical-segment-for {:value :string} :string
  [this input]
  (let [_ this
        v (str/lower-case (:value input))]
    (cond
      (= v "stt") "STT"
      (= v "rfs") "RFS"
      (= v "fs") "FS"
      (= v "nixos") "NixOS"
      :else (str/capitalize v))))

(impl DefaultAnswerReport AnswerReportOps canonical-subject-for {:value :string} :string
  [this input]
  (let [slug (safe-title-for this {:value (:value input)})]
    (->> (str/split slug #"-+")
         (remove str/blank?)
         (map #(canonical-segment-for this {:value %}))
         (str/join "-"))))

(impl DefaultAnswerReport AnswerReportOps parse-chronos-raw-for ParseChronosRawInput :map
  [this input]
  (let [raw (str/trim (:raw input))
        matched (re-find #"^(\d+)\.(\d+)\.(\d+)\.(\d+)\s+\|\s+(\d+)\s+AM$" raw)]
    (when-not matched
      (fail-for this {:message (str "Unexpected chronos output: " raw)}))
    (let [[_ sign degree minute second year] matched]
      {:raw raw
       :year year
       :sign (pad2-for this {:value sign})
       :degree (pad2-for this {:value degree})
       :minute (pad2-for this {:value minute})
       :second (pad2-for this {:value second})})))

(impl DefaultAnswerReport AnswerReportOps run-chronos-attempt-for ChronosAttemptInput :map
  [this input]
  (let [_ this]
    (try
      (let [result (apply sh (:argv input))]
        {:label (:label input)
         :argv (:argv input)
         :exit (:exit result)
         :out (:out result)
         :err (:err result)})
      (catch java.io.IOException e
        {:label (:label input)
         :argv (:argv input)
         :exit 127
         :out ""
         :err (.getMessage e)}))))

(impl DefaultAnswerReport AnswerReportOps summarize-chronos-failure-for ChronosFailureInput :string
  [this input]
  (let [lines
        (for [attempt (:attempts input)]
          (str "- " (str/join " " (:argv attempt))
               "\n  exit: " (:exit attempt)
               (when-not (str/blank? (str (:err attempt)))
                 (str "\n  stderr: " (str/trim (:err attempt))))
               (when-not (str/blank? (str (:out attempt)))
                 (str "\n  stdout: " (str/trim (:out attempt))))))]
    (str "Failed to run chronos using all resolver paths:\n"
         (str/join "\n" lines))))

(impl DefaultAnswerReport AnswerReportOps read-chronos-for ChronosInput :map
  [this input]
  (if (and (:rawOverride input) (not (str/blank? (:rawOverride input))))
    (parse-chronos-raw-for this {:raw (:rawOverride input)})
    (let [components-chronos-manifest? (.exists (io/file "Components/chronos/Cargo.toml"))
          library-manifest? (.exists (io/file "Library/chronos/Cargo.toml"))
          attempts (cond-> [{:label "chronos-bin"
                             :argv ["chronos" "--format" "numeric" "--precision" "second"]}]
                     components-chronos-manifest?
                     (conj {:label "cargo-components-chronos-manifest"
                            :argv ["cargo" "run" "--quiet"
                                   "--manifest-path" "Components/chronos/Cargo.toml"
                                   "--bin" "chronos" "--"
                                   "--format" "numeric" "--precision" "second"]})

                     library-manifest?
                     (conj {:label "cargo-library-chronos-manifest"
                            :argv ["cargo" "run" "--quiet"
                                   "--manifest-path" "Library/chronos/Cargo.toml"
                                   "--bin" "chronos" "--"
                                   "--format" "numeric" "--precision" "second"]}))
          results (mapv #(run-chronos-attempt-for this %) attempts)
          success (first (filter #(= 0 (:exit %)) results))]
      (if success
        (parse-chronos-raw-for this {:raw (:out success)})
        (fail-for this {:message (summarize-chronos-failure-for this {:attempts results})})))))

(impl DefaultAnswerReport AnswerReportOps build-filename-for BuildFilenameInput :string
  [this input]
  (let [{:keys [year sign degree minute second kind title tier subject]} input
        slug (safe-title-for this {:value title})]
    (str "Research/"
         tier
         "/"
         subject
         "/"
         year sign degree minute second "_" kind "_" slug ".md")))

(impl DefaultAnswerReport AnswerReportOps ensure-unique-path-for EnsureUniquePathInput :string
  [this input]
  (let [initial (:path input)]
    (loop [idx 1
           candidate initial]
      (if (.exists (io/file candidate))
        (recur (inc idx) (str/replace initial #"\.md$" (str "_" idx ".md")))
        candidate))))

(impl DefaultAnswerReport AnswerReportOps ensure-topic-readme-for EnsureTopicReadmeInput :any
  [this input]
  (let [{:keys [tier subject]} input
        readme (str "Research/" tier "/" subject "/index.edn")]
    (when-not (.exists (io/file readme))
      (io/make-parents readme)
      (spit readme
            (str "{:kind :index\n"
                 " :title \"Subject Topic\"\n"
                 " :subject \"" subject "\"\n"
                 " :developmentPath \"Development/" tier "/" subject "\"\n"
                 " :entries []}\n")))))

(impl DefaultAnswerReport AnswerReportOps write-report-for WriteReportInput :any
  [this input]
  (let [{:keys [path chronosRaw kind changeScope title subject prompt answer]} input
        tier (or (second (str/split path #"/")) "high")]
    (io/make-parents path)
    (ensure-topic-readme-for this {:tier tier
                                   :subject subject})
    (spit path
          (str "# Agent Report\n\n"
               "- Chronography: `" chronosRaw "`\n"
               "- Kind: `" kind "`\n"
               "- Change Scope: `" changeScope "`\n"
               "- Subject: `" subject "`\n"
               "- Title: `" title "`\n\n"
               "## Prompt\n\n"
               prompt
               "\n\n## Agent Answer\n\n"
               answer
               "\n\n## Reporting Protocol Notes\n\n"
               "- This report is required for `answer`, `draft`, and `question` responses.\n"
               "- Reporting still applies when response scope is `no-files`.\n"))))

(impl DefaultAnswerReport AnswerReportOps run-report-for Input :any
  [this input]
  (let [opts (parse-args-for this {:args (:args input)})
        prompt (read-text-for this {:value (:prompt opts)
                                    :path (:prompt-file opts)
                                    :label "prompt"})
        answer (read-text-for this {:value (:answer opts)
                                    :path (:answer-file opts)
                                    :label "answer"})
        kind (:kind opts)
        change-scope (:change-scope opts)
        _ (when-not (#{"answer" "draft" "question"} kind)
            (fail-for this {:message (str "Invalid --kind: " kind)}))
        _ (when-not (#{"modified-files" "no-files"} change-scope)
            (fail-for this {:message (str "Invalid --change-scope: " change-scope)}))
        subject (canonical-subject-for this {:value (or (:subject opts) (:title opts))})
        tier (subject-tier-for subject)
        chrono (read-chronos-for this {:rawOverride (:chronos-raw opts)})
        filename (build-filename-for this {:year (:year chrono)
                                           :sign (:sign chrono)
                                           :degree (:degree chrono)
                                           :minute (:minute chrono)
                                           :second (:second chrono)
                                           :kind kind
                                           :title (:title opts)
                                           :tier tier
                                           :subject subject})
        path (ensure-unique-path-for this {:path filename})]
    (write-report-for this {:path path
                            :chronosRaw (:raw chrono)
                            :kind kind
                            :changeScope change-scope
                            :title (:title opts)
                            :subject subject
                            :prompt prompt
                            :answer answer})
    (println path)))

(def default-answer-report (->DefaultAnswerReport))

(main Input
  [input]
  (run-report-for default-answer-report input))

(-main {:args (vec *command-line-args*)})
