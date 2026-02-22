#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :refer [sh]])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def ReportMainInput
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
  [:map])

(def BuildFilenameInput
  [:map
   [:year :string]
   [:sign :string]
   [:degree :string]
   [:minute :string]
   [:second :string]
   [:kind [:enum "answer" "draft" "question"]]
   [:title :string]])

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
   [:prompt :string]
   [:answer :string]])

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

(defn* read-chronos [:=> [:cat ChronosInput] :map] [_]
  (let [result (sh "cargo" "run" "--quiet" "--bin" "chronos" "--" "--format" "numeric" "--precision" "second")
        raw (str/trim (:out result))
        matched (re-find #"^(\d+)\.(\d+)\.(\d+)\.(\d+)\s+\|\s+(\d+)\s+AM$" raw)]
    (when-not (= 0 (:exit result))
      (fail {:message (str "Failed to run chronos:\n" (:err result))}))
    (when-not matched
      (fail {:message (str "Unexpected chronos output: " raw)}))
    (let [[_ sign degree minute second year] matched]
      {:raw raw
       :year year
       :sign (pad2 {:value sign})
       :degree (pad2 {:value degree})
       :minute (pad2 {:value minute})
       :second (pad2 {:value second})})))

(defn* build-filename [:=> [:cat BuildFilenameInput] :string] [input]
  (let [{:keys [year sign degree minute second kind title]} input
        slug (safe-title {:value title})]
    (str "Reports/"
         year "_"
         sign "_"
         degree "_"
         minute "_"
         second "_"
         kind "_"
         slug
         ".md")))

(defn* ensure-unique-path [:=> [:cat EnsureUniquePathInput] :string] [input]
  (let [initial (:path input)]
    (loop [idx 1
           candidate initial]
      (if (.exists (io/file candidate))
        (recur (inc idx) (str/replace initial #"\.md$" (str "_" idx ".md")))
        candidate))))

(defn* write-report! [:=> [:cat WriteReportInput] :any] [input]
  (let [{:keys [path chronosRaw kind changeScope title prompt answer]} input]
    (io/make-parents path)
    (spit path
          (str "# Agent Report\n\n"
               "- Chronography: `" chronosRaw "`\n"
               "- Kind: `" kind "`\n"
               "- Change Scope: `" changeScope "`\n"
               "- Title: `" title "`\n\n"
               "## Prompt\n\n"
               prompt
               "\n\n## Agent Answer\n\n"
               answer
               "\n\n## Reporting Protocol Notes\n\n"
               "- This report is required for `answer`, `draft`, and `question` responses.\n"
               "- Reporting still applies when response scope is `no-files`.\n"))))

(defn* -main [:=> [:cat ReportMainInput] :any] [input]
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
        chrono (read-chronos {})
        filename (build-filename {:year (:year chrono)
                                  :sign (:sign chrono)
                                  :degree (:degree chrono)
                                  :minute (:minute chrono)
                                  :second (:second chrono)
                                  :kind kind
                                  :title (:title opts)})
        path (ensure-unique-path {:path filename})]
    (write-report! {:path path
                    :chronosRaw (:raw chrono)
                    :kind kind
                    :changeScope change-scope
                    :title (:title opts)
                    :prompt prompt
                    :answer answer})
    (println path)))

(-main {:args (vec *command-line-args*)})
