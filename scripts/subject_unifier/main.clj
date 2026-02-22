#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def MainInput
  [:map
   [:args [:vector :string]]])

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def FileNameInput
  [:map
   [:name :string]])

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

(defn* report-subject [:=> [:cat FileNameInput] [:maybe :string]] [input]
  (let [name (:name input)
        matched (re-find #"^\d+_\d+_\d+_\d+_\d+_(answer|draft|question)_(.+)\.md$" name)]
    (when matched
      (nth matched 2))))

(defn* strategy-dir? [:=> [:cat [:map [:file :any]]] :boolean] [input]
  (.isDirectory ^java.io.File (:file input)))

(defn* slug->title [:=> [:cat [:map [:slug :string]]] :string] [input]
  (->> (str/split (:slug input) #"-+")
       (remove str/blank?)
       (map str/capitalize)
       (str/join " ")))

(defn* list-report-subjects [:=> [:cat] :map] []
  (let [dir (io/file "Reports")]
    (if-not (.exists dir)
      {:subjects #{}
       :subject->reports {}}
      (let [pairs (->> (.listFiles dir)
                       (filter #(.isFile ^java.io.File %))
                       (map (fn [f]
                              (let [name (.getName ^java.io.File f)
                                    subject (report-subject {:name name})]
                                (when subject
                                  [subject (str "Reports/" name)]))))
                       (remove nil?))
            subject->reports (reduce (fn [acc [subject path]]
                                       (update acc subject (fnil conj []) path))
                                     {}
                                     pairs)]
        {:subjects (set (keys subject->reports))
         :subject->reports (into {} (map (fn [[k v]] [k (vec (sort v))]) subject->reports))}))))

(defn* list-strategy-subjects [:=> [:cat] [:set :string]] []
  (let [dir (io/file "strategies")]
    (if-not (.exists dir)
      #{}
      (->> (.listFiles dir)
           (filter #(strategy-dir? {:file %}))
           (map #(.getName ^java.io.File %))
           set))))

(defn* topic-path [:=> [:cat [:map [:subject :string]]] :string] [input]
  (str "Reports/topics/" (:subject input) ".md"))

(defn* strategy-path [:=> [:cat [:map [:subject :string]]] :string] [input]
  (str "strategies/" (:subject input)))

(defn* write-topic! [:=> [:cat TopicInput] :any] [input]
  (let [{:keys [subject reportPaths strategyPath]} input
        path (topic-path {:subject subject})
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
            (str "# Strategy: " (slug->title {:slug subject}) "\n\n"
                 "## Objective\n"
                 "Create and maintain a coherent strategy/report pairing for subject `" subject "`.\n\n"
                 "## Scope\n"
                 "- Ensure implementation guidance for this subject is captured.\n"
                 "- Keep `Reports/topics/` and `strategies/` synchronized.\n\n"
                 "## Initial Plan\n"
                 "1. Inventory existing artifacts for the subject.\n"
                 "2. Define gaps and risks.\n"
                 "3. Execute and validate.\n")))
    (io/make-parents report-link-file)
    (spit report-link-file
          (str "# Report Link\n\n"
               "- Subject: `" subject "`\n"
               "- Topic File: `" topicPath "`\n"))))

(defn* ensure-readme-topic-section! [:=> [:cat] :any] []
  (let [path "Reports/README.md"
        marker "## Subject Topics"
        snippet (str marker "\n"
                     "Subject mapping files live in `Reports/topics/`.\n"
                     "Each topic must have a corresponding `strategies/<subject>/` directory.\n")]
    (when (.exists (io/file path))
      (let [content (slurp path)]
        (when-not (str/includes? content marker)
          (spit path (str content "\n\n" snippet)))))))

(defn* -main [:=> [:cat MainInput] :any] [input]
  (let [{:keys [write?]} (parse-args {:args (:args input)})
        report-data (list-report-subjects)
        report-subjects (:subjects report-data)
        strategy-subjects (list-strategy-subjects)
        all-subjects (vec (sort (clojure.set/union report-subjects strategy-subjects)))
        missing-strategies (vec (sort (clojure.set/difference report-subjects strategy-subjects)))
        missing-topics (vec (sort (remove #(.exists (io/file (topic-path {:subject %}))) all-subjects)))]
    (println "Subject unification scan:")
    (println (str "- Report subjects: " (count report-subjects)))
    (println (str "- Strategy subjects: " (count strategy-subjects)))
    (println (str "- Unified subjects: " (count all-subjects)))
    (println (str "- Missing strategies: " (count missing-strategies)))
    (println (str "- Missing topic files: " (count missing-topics)))
    (when (seq missing-strategies)
      (println "- Missing strategy subjects:")
      (doseq [subject missing-strategies]
        (println (str "  - " subject))))
    (if-not write?
      (println "Dry run only. Re-run with --write to apply.")
      (do
        (doseq [subject all-subjects]
          (let [sp (strategy-path {:subject subject})
                tp (topic-path {:subject subject})
                exists? (.exists (io/file sp))]
            (write-topic! {:subject subject
                           :reportPaths (get (:subject->reports report-data) subject [])
                           :strategyPath sp})
            (write-strategy-scaffold! {:subject subject
                                       :strategyPath sp
                                       :topicPath tp
                                       :exists? exists?})))
        (ensure-readme-topic-section!)
        (println "Applied subject unification changes.")))))

(-main {:args (vec *command-line-args*)})
