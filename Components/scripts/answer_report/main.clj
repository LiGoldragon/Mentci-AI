#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :refer [sh]])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

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
   [:subject :string]])

(defprotocol AnswerReportOps
  (run-report-for [this input]))

(defrecord DefaultAnswerReport [])

(defn* fail [:=> [:cat FailInput] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* parse-args [:=> [:cat ParseArgsInput] :map] [input]
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
          (fail {:message (str "Unknown argument: " arg)}))))))

(defn* read-text [:=> [:cat ReadTextInput] :string] [input]
  (let [{:keys [value path label]} input]
    (cond
      (and value (not (str/blank? value))) value
      (and path (.exists (io/file path))) (slurp path)
      :else (fail {:message (str "Missing required " label " (--" label " or --" label "-file).")}))))

(defn* pad2 [:=> [:cat [:map [:value :string]]] :string] [input]
  (format "%02d" (Integer/parseInt (:value input))))

(defn* safe-title [:=> [:cat [:map [:value :string]]] :string] [input]
  (let [v (-> (:value input)
              str/lower-case
              (str/replace #"[^a-z0-9]+" "-")
              (str/replace #"^-+" "")
              (str/replace #"-+$" ""))]
    (if (str/blank? v) "agent-answer" v)))

(defn* canonical-segment [:=> [:cat [:map [:value :string]]] :string] [input]
  (let [v (str/lower-case (:value input))]
    (cond
      (= v "stt") "STT"
      (= v "rfs") "RFS"
      (= v "fs") "FS"
      (= v "nixos") "NixOS"
      :else (str/capitalize v))))

(defn* canonical-subject [:=> [:cat [:map [:value :string]]] :string] [input]
  (let [slug (safe-title {:value (:value input)})]
    (->> (str/split slug #"-+")
         (remove str/blank?)
         (map #(canonical-segment {:value %}))
         (str/join "-"))))

(defn* parse-chronos-raw [:=> [:cat ParseChronosRawInput] :map] [input]
  (let [raw (str/trim (:raw input))
        matched (re-find #"^(\d+)\.(\d+)\.(\d+)\.(\d+)\s+\|\s+(\d+)\s+AM$" raw)]
    (when-not matched
      (fail {:message (str "Unexpected chronos output: " raw)}))
    (let [[_ sign degree minute second year] matched]
      {:raw raw
       :year year
       :sign (pad2 {:value sign})
       :degree (pad2 {:value degree})
       :minute (pad2 {:value minute})
       :second (pad2 {:value second})})))

(defn* run-chronos-attempt [:=> [:cat ChronosAttemptInput] :map] [input]
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
       :err (.getMessage e)})))

(defn* summarize-chronos-failure [:=> [:cat ChronosFailureInput] :string] [input]
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

(defn* read-chronos [:=> [:cat ChronosInput] :map] [input]
  (if (and (:rawOverride input) (not (str/blank? (:rawOverride input))))
    (parse-chronos-raw {:raw (:rawOverride input)})
    (let [components-manifest? (.exists (io/file "Components/Cargo.toml"))
          library-manifest? (.exists (io/file "Library/chronos/Cargo.toml"))
          attempts (cond-> [{:label "chronos-bin"
                             :argv ["chronos" "--format" "numeric" "--precision" "second"]}]
                     components-manifest?
                     (conj {:label "cargo-components-manifest"
                            :argv ["cargo" "run" "--quiet"
                                   "--manifest-path" "Components/Cargo.toml"
                                   "--bin" "chronos" "--"
                                   "--format" "numeric" "--precision" "second"]})

                     library-manifest?
                     (conj {:label "cargo-library-chronos-manifest"
                            :argv ["cargo" "run" "--quiet"
                                   "--manifest-path" "Library/chronos/Cargo.toml"
                                   "--bin" "chronos" "--"
                                   "--format" "numeric" "--precision" "second"]}))
          results (mapv run-chronos-attempt attempts)
          success (first (filter #(= 0 (:exit %)) results))]
      (if success
        (parse-chronos-raw {:raw (:out success)})
        (fail {:message (summarize-chronos-failure {:attempts results})})))))

(defn* build-filename [:=> [:cat BuildFilenameInput] :string] [input]
  (let [{:keys [year sign degree minute second kind title subject]} input
        slug (safe-title {:value title})]
    (str "Reports/"
         subject
         "/"
         year sign degree minute second "_" kind "_" slug ".md")))

(defn* ensure-unique-path [:=> [:cat EnsureUniquePathInput] :string] [input]
  (let [initial (:path input)]
    (loop [idx 1
           candidate initial]
      (if (.exists (io/file candidate))
        (recur (inc idx) (str/replace initial #"\.md$" (str "_" idx ".md")))
        candidate))))

(defn* ensure-topic-readme! [:=> [:cat EnsureTopicReadmeInput] :any] [input]
  (let [subject (:subject input)
        readme (str "Reports/" subject "/README.md")]
    (when-not (.exists (io/file readme))
      (io/make-parents readme)
      (spit readme
            (str "# Subject Topic\n\n"
                 "- Subject: `" subject "`\n"
                 "- Strategy Path: `Strategies/" subject "`\n\n"
                 "## Report Entries\n\n"
                 "- _Topic index. Report entries are stored in this directory._\n")))))

(defn* write-report! [:=> [:cat WriteReportInput] :any] [input]
  (let [{:keys [path chronosRaw kind changeScope title subject prompt answer]} input]
    (io/make-parents path)
    (ensure-topic-readme! {:subject subject})
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
  (let [opts (parse-args {:args (:args input)})
        prompt (read-text {:value (:prompt opts)
                           :path (:prompt-file opts)
                           :label "prompt"})
        answer (read-text {:value (:answer opts)
                           :path (:answer-file opts)
                           :label "answer"})
        kind (:kind opts)
        change-scope (:change-scope opts)
        _ (when-not (#{"answer" "draft" "question"} kind)
            (fail {:message (str "Invalid --kind: " kind)}))
        _ (when-not (#{"modified-files" "no-files"} change-scope)
            (fail {:message (str "Invalid --change-scope: " change-scope)}))
        subject (canonical-subject {:value (or (:subject opts) (:title opts))})
        chrono (read-chronos {:rawOverride (:chronos-raw opts)})
        filename (build-filename {:year (:year chrono)
                                  :sign (:sign chrono)
                                  :degree (:degree chrono)
                                  :minute (:minute chrono)
                                  :second (:second chrono)
                                  :kind kind
                                  :title (:title opts)
                                  :subject subject})
        path (ensure-unique-path {:path filename})]
    (write-report! {:path path
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
