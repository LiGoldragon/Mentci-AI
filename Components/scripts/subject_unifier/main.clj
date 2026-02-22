#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.set :as set])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def CanonicalInput
  [:map
   [:value :string]])

(def PathInput
  [:map
   [:subject :string]])

(def TopicInput
  [:map
   [:subject :string]
   [:reportPaths [:vector :string]]
   [:strategyPath :string]])

(def StrategyScaffoldInput
  [:map
   [:subject :string]
   [:strategyPath :string]
   [:topicPath :string]
   [:exists? :boolean]])

(defn* parse-args [:=> [:cat ParseArgsInput] :map] [input]
  (let [args (:args input)]
    {:write? (boolean (some #{"--write"} args))}))

(defn* safe-slug [:=> [:cat CanonicalInput] :string] [input]
  (let [v (-> (:value input)
              str/lower-case
              (str/replace #"[^a-z0-9]+" "-")
              (str/replace #"^-+" "")
              (str/replace #"-+$" ""))]
    (if (str/blank? v) "untitled" v)))

(defn* canonical-segment [:=> [:cat CanonicalInput] :string] [input]
  (let [v (str/lower-case (:value input))]
    (cond
      (= v "stt") "STT"
      (= v "rfs") "RFS"
      (= v "fs") "FS"
      (= v "nixos") "NixOS"
      :else (str/capitalize v))))

(defn* canonical-subject [:=> [:cat CanonicalInput] :string] [input]
  (let [slug (safe-slug {:value (:value input)})]
    (->> (str/split slug #"-+")
         (remove str/blank?)
         (map #(canonical-segment {:value %}))
         (str/join "-"))))

(defn* report-file? [:=> [:cat [:map [:name :string]]] :boolean] [input]
  (boolean (re-find #"^\d+_\d+_\d+_\d+_\d+_(answer|draft|question)_.+\.md$" (:name input))))

(defn* report-subject-from-filename [:=> [:cat CanonicalInput] :string] [input]
  (let [name (:value input)
        matched (re-find #"^\d+_\d+_\d+_\d+_\d+_(answer|draft|question)_(.+)\.md$" name)
        raw (or (nth matched 2 nil) "untitled")]
    (canonical-subject {:value raw})))

(defn* strategy-path [:=> [:cat PathInput] :string] [input]
  (str "Strategies/" (:subject input)))

(defn* report-topic-path [:=> [:cat PathInput] :string] [input]
  (str "Reports/" (:subject input) "/README.md"))

(defn* strategy-dir? [:=> [:cat [:map [:file :any]]] :boolean] [input]
  (.isDirectory ^java.io.File (:file input)))

(defn* migrate-legacy-reports! [:=> [:cat] :any] []
  (let [reports-root (io/file "Reports")]
    (when (.exists reports-root)
      ;; Move flat chronographic report files into Reports/<Subject>/
      (doseq [file (.listFiles reports-root)]
        (when (.isFile ^java.io.File file)
          (let [name (.getName ^java.io.File file)]
            (when (report-file? {:name name})
              (let [subject (report-subject-from-filename {:value name})
                    target-dir (io/file (str "Reports/" subject))
                    target-file (io/file target-dir name)]
                (.mkdirs target-dir)
                (.renameTo file target-file))))))
      ;; Migrate old Reports/*.md into per-topic README.md
      (let [legacy-topics (io/file "Reports/topics")]
        (when (.exists legacy-topics)
          (doseq [file (.listFiles legacy-topics)]
            (when (.isFile ^java.io.File file)
              (let [base (.getName ^java.io.File file)
                    subject (canonical-subject {:value (str/replace base #"\.md$" "")})
                    target (io/file (str "Reports/" subject "/README.md"))]
                (.mkdirs (.getParentFile target))
                (when-not (.exists target)
                  (.renameTo file target)))))
          (doseq [leftover (.listFiles legacy-topics)]
            (when (.isFile ^java.io.File leftover)
              (.delete leftover)))
          (.delete legacy-topics))))))

(defn* list-report-subjects [:=> [:cat] :map] []
  (let [root (io/file "Reports")]
    (if-not (.exists root)
      {:subjects #{}
       :subject->reports {}}
      (let [subject->reports
            (->> (.listFiles root)
                 (filter #(strategy-dir? {:file %}))
                 (reduce (fn [acc dir]
                           (let [raw-subject (.getName ^java.io.File dir)
                                 subject (canonical-subject {:value raw-subject})
                                 reports (->> (.listFiles ^java.io.File dir)
                                              (filter #(.isFile ^java.io.File %))
                                              (map #(.getName ^java.io.File %))
                                              (filter #(report-file? {:name %}))
                                              sort
                                              (map #(str "Reports/" raw-subject "/" %))
                                              vec)]
                             (assoc acc subject reports)))
                         {}))]
        {:subjects (set (keys subject->reports))
         :subject->reports subject->reports}))))

(defn* rename-strategy-dirs! [:=> [:cat] :any] []
  (let [root (io/file "strategies")]
    (when (.exists root)
      (doseq [dir (.listFiles root)]
        (when (strategy-dir? {:file dir})
          (let [old-name (.getName ^java.io.File dir)
                new-name (canonical-subject {:value old-name})]
            (when-not (= old-name new-name)
              (let [target (io/file root new-name)]
                (when-not (.exists target)
                  (.renameTo dir target))))))))))

(defn* list-strategy-subjects [:=> [:cat] [:set :string]] []
  (let [dir (io/file "strategies")]
    (if-not (.exists dir)
      #{}
      (->> (.listFiles dir)
           (filter #(strategy-dir? {:file %}))
           (map #(.getName ^java.io.File %))
           (map #(canonical-subject {:value %}))
           set))))

(defn* write-topic-readme! [:=> [:cat TopicInput] :any] [input]
  (let [{:keys [subject reportPaths strategyPath]} input
        path (report-topic-path {:subject subject})
        content (str "# Subject Topic\n\n"
                     "- Subject: `" subject "`\n"
                     "- Strategy Path: `" strategyPath "`\n\n"
                     "## Report Entries\n\n"
                     (if (seq reportPaths)
                       (str/join "\n" (map #(str "- `" % "`") reportPaths))
                       "- _No report entries yet._")
                     "\n")]
    (io/make-parents path)
    (spit path content)))

(defn* write-strategy-scaffold! [:=> [:cat StrategyScaffoldInput] :any] [input]
  (let [{:keys [subject strategyPath topicPath exists?]} input
        strategy-file (str strategyPath "/STRATEGY.md")
        report-link-file (str strategyPath "/REPORT.md")]
    (when-not exists?
      (io/make-parents strategy-file)
      (spit strategy-file
            (str "# Strategy: " (str/replace subject #"-" " ") "\n\n"
                 "## Objective\n"
                 "Create and maintain a coherent strategy/report pairing for subject `" subject "`.\n\n"
                 "## Scope\n"
                 "- Ensure implementation guidance for this subject is captured.\n"
                 "- Keep `Reports/<Subject>/` and `Strategies/<Subject>/` synchronized.\n\n"
                 "## Initial Plan\n"
                 "1. Inventory existing artifacts for the subject.\n"
                 "2. Define gaps and risks.\n"
                 "3. Execute and validate.\n")))
    (io/make-parents report-link-file)
    (spit report-link-file
          (str "# Report Link\n\n"
               "- Subject: `" subject "`\n"
               "- Topic File: `" topicPath "`\n"))))

(defn* ensure-reports-readme-section! [:=> [:cat] :any] []
  (let [path "Reports/README.md"
        marker "## Subject Topics"
        snippet (str marker "\n"
                     "Topics are subdirectory names under `Reports/` (for example `Reports/Prompt-Report-System/`).\n"
                     "Each topic must have a corresponding `Strategies/<Subject>/` directory.\n")]
    (when (.exists (io/file path))
      (let [content (slurp path)
            updated (if (str/includes? content marker)
                      (str/replace content #"(?s)## Subject Topics.*$" snippet)
                      (str content "\n\n" snippet))]
        (spit path updated)))))

(main Input
  [input]
  (let [{:keys [write?]} (parse-args {:args (:args input)})]
    (when write?
      (migrate-legacy-reports!)
      (rename-strategy-dirs!))
    (let [report-data (list-report-subjects)
          report-subjects (:subjects report-data)
          strategy-subjects (list-strategy-subjects)
          all-subjects (vec (sort (set/union report-subjects strategy-subjects)))
          missing-strategies (vec (sort (set/difference report-subjects strategy-subjects)))
          missing-topics (vec (sort (remove #(.exists (io/file (report-topic-path {:subject %}))) all-subjects)))]
      (println "Subject unification scan:")
      (println (str "- Report subjects: " (count report-subjects)))
      (println (str "- Strategy subjects: " (count strategy-subjects)))
      (println (str "- Unified subjects: " (count all-subjects)))
      (println (str "- Missing strategies: " (count missing-strategies)))
      (println (str "- Missing topic READMEs: " (count missing-topics)))
      (when (seq missing-strategies)
        (println "- Missing strategy subjects:")
        (doseq [subject missing-strategies]
          (println (str "  - " subject))))
      (if-not write?
        (println "Dry run only. Re-run with --write to apply.")
        (do
          (doseq [subject all-subjects]
            (let [sp (strategy-path {:subject subject})
                  tp (report-topic-path {:subject subject})
                  exists? (.exists (io/file sp))]
              (write-topic-readme! {:subject subject
                                    :reportPaths (get (:subject->reports report-data) subject [])
                                    :strategyPath sp})
              (write-strategy-scaffold! {:subject subject
                                         :strategyPath sp
                                         :topicPath tp
                                         :exists? exists?})))
          (ensure-reports-readme-section!)
          (println "Applied subject unification changes."))))))

(-main {:args (vec *command-line-args*)})
