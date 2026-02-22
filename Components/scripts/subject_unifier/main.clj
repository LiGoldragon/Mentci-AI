#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.set :as set])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

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
   [:subject :string]
   [:tier {:optional true} :string]])

(def TopicInput
  [:map
   [:subject :string]
   [:tier {:optional true} :string]
   [:reportPaths [:vector :string]]
   [:strategyPath :string]])

(def StrategyScaffoldInput
  [:map
   [:subject :string]
   [:strategyPath :string]
   [:topicPath :string]
   [:exists? :boolean]])

(defprotocol SubjectUnifierOps
  (parse-args-for [this input])
  (safe-slug-for [this input])
  (canonical-segment-for [this input])
  (canonical-subject-for [this input])
  (report-file-for [this input])
  (report-subject-from-filename-for [this input])
  (strategy-path-for [this input])
  (report-topic-path-for [this input])
  (strategy-dir-for [this input])
  (migrate-legacy-reports-for [this])
  (list-report-subjects-for [this])
  (rename-strategy-dirs-for [this])
  (list-strategy-subjects-for [this])
  (write-topic-readme-for [this input])
  (write-strategy-scaffold-for [this input])
  (ensure-reports-readme-section-for [this])
  (run-unifier-for [this input]))

(defrecord DefaultSubjectUnifier [])

(def TierNames ["high" "medium" "low"])

(defn subject-tier-for [subject]
  (or (some (fn [tier]
              (when (or (.exists (io/file (str "Development/" tier "/" subject)))
                        (.exists (io/file (str "Research/" tier "/" subject))))
                tier))
            TierNames)
      "high"))

(impl DefaultSubjectUnifier SubjectUnifierOps parse-args-for ParseArgsInput :map
  [this input]
  (let [_ this
        args (:args input)]
    {:write? (boolean (some #{"--write"} args))}))

(impl DefaultSubjectUnifier SubjectUnifierOps safe-slug-for CanonicalInput :string
  [this input]
  (let [_ this
        v (-> (:value input)
              str/lower-case
              (str/replace #"[^a-z0-9]+" "-")
              (str/replace #"^-+" "")
              (str/replace #"-+$" ""))]
    (if (str/blank? v) "untitled" v)))

(impl DefaultSubjectUnifier SubjectUnifierOps canonical-segment-for CanonicalInput :string
  [this input]
  (let [_ this
        v (str/lower-case (:value input))]
    (cond
      (= v "stt") "STT"
      (= v "rfs") "RFS"
      (= v "fs") "FS"
      (= v "nixos") "NixOS"
      :else (str/capitalize v))))

(impl DefaultSubjectUnifier SubjectUnifierOps canonical-subject-for CanonicalInput :string
  [this input]
  (let [slug (safe-slug-for this {:value (:value input)})]
    (->> (str/split slug #"-+")
         (remove str/blank?)
         (map #(canonical-segment-for this {:value %}))
         (str/join "-"))))

(impl DefaultSubjectUnifier SubjectUnifierOps report-file-for {:name :string} :boolean
  [this input]
  (let [_ this]
    (boolean (re-find #"^(?:\d{8}|\d{12})_(answer|draft|question|strategy)_.+\.md$" (:name input)))))

(impl DefaultSubjectUnifier SubjectUnifierOps report-subject-from-filename-for CanonicalInput :string
  [this input]
  (let [name (:value input)
        matched (re-find #"^(?:\d{8}|\d{12})_(answer|draft|question|strategy)_(.+)\.md$" name)
        raw (or (nth matched 2 nil) "untitled")]
    (canonical-subject-for this {:value raw})))

(impl DefaultSubjectUnifier SubjectUnifierOps strategy-path-for PathInput :string
  [this input]
  (let [_ this]
    (str "Development/" (or (:tier input) "high") "/" (:subject input))))

(impl DefaultSubjectUnifier SubjectUnifierOps report-topic-path-for PathInput :string
  [this input]
  (let [_ this]
    (str "Research/" (or (:tier input) "high") "/" (:subject input) "/index.edn")))

(impl DefaultSubjectUnifier SubjectUnifierOps strategy-dir-for {:file :any} :boolean
  [this input]
  (.isDirectory ^java.io.File (:file input)))

(impl DefaultSubjectUnifier SubjectUnifierOps migrate-legacy-reports-for [:=> [:cat :any] :any]
  [this]
  (let [reports-root (io/file "Research")]
    (when (.exists reports-root)
      (doseq [file (.listFiles reports-root)]
        (when (.isFile ^java.io.File file)
          (let [name (.getName ^java.io.File file)]
            (when (report-file-for this {:name name})
              (let [subject (report-subject-from-filename-for this {:value name})
                    target-dir (io/file (str "Research/high/" subject))
                    target-file (io/file target-dir name)]
                (.mkdirs target-dir)
                (.renameTo file target-file))))))
      (let [legacy-topics (io/file "Research/topics")]
        (when (.exists legacy-topics)
          (doseq [file (.listFiles legacy-topics)]
            (when (.isFile ^java.io.File file)
              (let [base (.getName ^java.io.File file)
                    subject (canonical-subject-for this {:value (str/replace base #"\.md$" "")})
                    target (io/file (str "Research/high/" subject "/index.edn"))]
                (.mkdirs (.getParentFile target))
                (when-not (.exists target)
                  (.renameTo file target)))))
          (doseq [leftover (.listFiles legacy-topics)]
            (when (.isFile ^java.io.File leftover)
              (.delete leftover)))
          (.delete legacy-topics))))))

(impl DefaultSubjectUnifier SubjectUnifierOps list-report-subjects-for [:=> [:cat :any] :map]
  [this]
  (let [root (io/file "Research")]
    (if-not (.exists root)
      {:subjects #{}
       :subject->reports {}}
      (let [tier-dirs (->> TierNames
                           (map #(io/file (str "Research/" %)))
                           (filter #(.exists ^java.io.File %)))
            tier-subject-dirs (->> tier-dirs
                                   (mapcat (fn [tier-dir]
                                             (let [tier (.getName ^java.io.File tier-dir)]
                                               (->> (or (.listFiles ^java.io.File tier-dir) [])
                                                    (filter #(strategy-dir-for this {:file %}))
                                                    (map (fn [subject-dir]
                                                           {:tier tier
                                                            :dir subject-dir})))))))
            legacy-subject-dirs (->> (or (.listFiles root) [])
                                     (filter #(strategy-dir-for this {:file %}))
                                     (remove #(contains? (set TierNames) (.getName ^java.io.File %)))
                                     (map (fn [subject-dir]
                                            {:tier nil
                                             :dir subject-dir})))
            all-subject-dirs (concat tier-subject-dirs legacy-subject-dirs)
            subject->reports
            (reduce (fn [acc {:keys [tier dir]}]
                      (let [raw-subject (.getName ^java.io.File dir)
                            subject (canonical-subject-for this {:value raw-subject})
                            reports (->> (or (.listFiles ^java.io.File dir) [])
                                         (filter #(.isFile ^java.io.File %))
                                         (map #(.getName ^java.io.File %))
                                         (filter #(report-file-for this {:name %}))
                                         sort
                                         (map #(if tier
                                                 (str "Research/" tier "/" raw-subject "/" %)
                                                 (str "Research/" raw-subject "/" %)))
                                         vec)]
                        (update acc subject (fnil into []) reports)))
                    {}
                    all-subject-dirs)]
        {:subjects (set (keys subject->reports))
         :subject->reports subject->reports}))))

(impl DefaultSubjectUnifier SubjectUnifierOps rename-strategy-dirs-for [:=> [:cat :any] :any]
  [this]
  (let [root (io/file "Development")]
    (when (.exists root)
      (let [tier-dirs (->> TierNames
                           (map #(io/file (str "Development/" %)))
                           (filter #(.exists ^java.io.File %)))
            rename-under! (fn [parent]
                            (doseq [dir (or (.listFiles ^java.io.File parent) [])]
                              (when (strategy-dir-for this {:file dir})
                                (let [old-name (.getName ^java.io.File dir)
                                      new-name (canonical-subject-for this {:value old-name})]
                                  (when-not (= old-name new-name)
                                    (let [target (io/file parent new-name)]
                                      (when-not (.exists target)
                                        (.renameTo dir target))))))))]
        (doseq [tier-dir tier-dirs]
          (rename-under! tier-dir))
        (when (seq (->> (or (.listFiles root) [])
                        (filter #(strategy-dir-for this {:file %}))
                        (remove #(contains? (set TierNames) (.getName ^java.io.File %)))))
          (rename-under! root))))))

(impl DefaultSubjectUnifier SubjectUnifierOps list-strategy-subjects-for [:=> [:cat :any] [:set :string]]
  [this]
  (let [dir (io/file "Development")]
    (if-not (.exists dir)
      #{}
      (let [tier-dirs (->> TierNames
                           (map #(io/file (str "Development/" %)))
                           (filter #(.exists ^java.io.File %)))
            tier-subjects (->> tier-dirs
                               (mapcat (fn [tier-dir]
                                         (->> (or (.listFiles ^java.io.File tier-dir) [])
                                              (filter #(strategy-dir-for this {:file %}))
                                              (map #(.getName ^java.io.File %))))))
            legacy-subjects (->> (or (.listFiles dir) [])
                                 (filter #(strategy-dir-for this {:file %}))
                                 (remove #(contains? (set TierNames) (.getName ^java.io.File %)))
                                 (map #(.getName ^java.io.File %)))]
        (->> (concat tier-subjects legacy-subjects)
             (map #(canonical-subject-for this {:value %}))
             set)))))

(impl DefaultSubjectUnifier SubjectUnifierOps write-topic-readme-for TopicInput :any
  [this input]
  (let [{:keys [subject tier reportPaths strategyPath]} input
        path (report-topic-path-for this {:subject subject
                                          :tier tier})
        entries (if (seq reportPaths)
                  reportPaths
                  [])
        content (str "{:kind :index\n"
                     " :title \"Research Topic\"\n"
                     " :subject \"" subject "\"\n"
                     " :developmentPath \"" strategyPath "\"\n"
                     " :entries ["
                     (str/join " " (map #(str "\"" % "\"") entries))
                     "]}\n")]
    (io/make-parents path)
    (spit path content)))

(impl DefaultSubjectUnifier SubjectUnifierOps write-strategy-scaffold-for StrategyScaffoldInput :any
  [this input]
  (let [_ this
        {:keys [subject strategyPath topicPath exists?]} input
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
                 "- Keep `Research/<priority>/<Subject>/` and `Development/<priority>/<Subject>/` synchronized.\n\n"
                 "## Initial Plan\n"
                 "1. Inventory existing artifacts for the subject.\n"
                 "2. Define gaps and risks.\n"
                 "3. Execute and validate.\n")))
    (io/make-parents report-link-file)
    (spit report-link-file
          (str "# Report Link\n\n"
               "- Subject: `" subject "`\n"
               "- Topic File: `" topicPath "`\n"))))

(impl DefaultSubjectUnifier SubjectUnifierOps ensure-reports-readme-section-for [:=> [:cat :any] :any]
  [this]
  (let [_ this
        path "Research/index.edn"
        snippet "{:kind :index :title \"Research\" :note \"Tiered research topics are organized under Research/<priority>/<Subject>/index.edn\"}\n"]
    (when (.exists (io/file path))
      (spit path snippet))))

(impl DefaultSubjectUnifier SubjectUnifierOps run-unifier-for Input :any
  [this input]
  (let [{:keys [write?]} (parse-args-for this {:args (:args input)})]
    (when write?
      (migrate-legacy-reports-for this)
      (rename-strategy-dirs-for this))
    (let [report-data (list-report-subjects-for this)
          report-subjects (:subjects report-data)
          strategy-subjects (list-strategy-subjects-for this)
          all-subjects (vec (sort (set/union report-subjects strategy-subjects)))
          missing-strategies (vec (sort (set/difference report-subjects strategy-subjects)))
          missing-topics (vec (sort (remove (fn [subject]
                                              (let [tier (subject-tier-for subject)]
                                                (.exists (io/file (report-topic-path-for this {:subject subject
                                                                                                :tier tier})))))
                                            all-subjects)))]
      (println "Research/Development unification scan:")
      (println (str "- Research subjects: " (count report-subjects)))
      (println (str "- Development subjects: " (count strategy-subjects)))
      (println (str "- Unified subjects: " (count all-subjects)))
      (println (str "- Missing development subjects: " (count missing-strategies)))
      (println (str "- Missing topic indexes: " (count missing-topics)))
      (when (seq missing-strategies)
        (println "- Missing development subjects:")
        (doseq [subject missing-strategies]
          (println (str "  - " subject))))
      (if-not write?
        (println "Dry run only. Re-run with --write to apply.")
        (do
          (doseq [subject all-subjects]
            (let [tier (subject-tier-for subject)
                  sp (strategy-path-for this {:subject subject
                                              :tier tier})
                  tp (report-topic-path-for this {:subject subject
                                                  :tier tier})
                  exists? (.exists (io/file sp))]
              (write-topic-readme-for this {:subject subject
                                            :tier tier
                                            :reportPaths (get (:subject->reports report-data) subject [])
                                            :strategyPath sp})
              (write-strategy-scaffold-for this {:subject subject
                                                 :strategyPath sp
                                                 :topicPath tp
                                                 :exists? exists?})))
          (ensure-reports-readme-section-for this)
          (println "Applied subject unification changes."))))))

(def default-subject-unifier (->DefaultSubjectUnifier))

(main Input
  [input]
  (run-unifier-for default-subject-unifier input))

(-main {:args (vec *command-line-args*)})
