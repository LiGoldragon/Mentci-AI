#!/usr/bin/env bb

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

;; Tool Stack Transparency:
;; Runtime: Babashka (Clojure)
;; Rationale: Fast, reliable file manipulation for artifact purging.

(def ARTIFACT_PATTERN #"(?s)> \*\*Canonical Aski framing:\*\* Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data \(homoiconicity\)\. Authority: `(?:docs/architecture|core)/ASKI_POSITIONING\.md`\.

?")

(def WHITELIST #{"core/ASKI_POSITIONING.md" "core/AGENTS.md" "core/programs/RESTART_CONTEXT.md"})

(defn sweep-file [file]
  (let [path (.getPath file)
        content (slurp file)]
    (if (and (str/ends-with? path ".md")
             (not (contains? WHITELIST (str/replace path #"^\./" "")))
             (re-find ARTIFACT_PATTERN content))
      (do
        (println (str "Purging artifact from: " path))
        (spit file (str/replace content ARTIFACT_PATTERN "")))
      nil)))

(defn sweep-dir [dir]
  (doseq [file (file-seq (io/file dir))]
    (when (.isFile file)
      (sweep-file file))))

(println "Starting Artifact Sweep...")
(sweep-dir ".")
(println "Sweep Complete.")
