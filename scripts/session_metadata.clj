#!/usr/bin/env bb

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[cheshire.core :as json])

;; Tool Stack Transparency:
;; Runtime: Babashka (Clojure)
;; Rationale: Fast CLI orchestration for session metadata management.

(def SESSION_FILE ".mentci/session_state.json")

(defn load-session []
  (if (.exists (io/file SESSION_FILE))
    (json/parse-string (slurp SESSION_FILE) true)
    {}))

(defn save-session [state]
  (io/make-parents SESSION_FILE)
  (spit SESSION_FILE (json/generate-string state {:pretty true})))

(defn init-session [prompt context]
  (let [state {:raw_prompt prompt
               :initial_context context
               :start_rev (str/trim (:out (clojure.java.shell/sh "jj" "log" "-r" "@" "--template" "change_id.short()")))
               :intents []}]
    (save-session state)
    (println "Session initialized.")))

(defn add-intent [intent context]
  (let [state (load-session)]
    (save-session (update state :intents conj {:intent intent :context context}))
    (println (str "intent: " intent "
[Prompt: " (subs (:raw_prompt state) 0 (min 100 (count (:raw_prompt state)))) "...]
[Context: " context "]"))))

(defn generate-synthesis [result_summary chronos_date]
  (let [state (load-session)]
    (println (str "session: " result_summary))
    (println chronos_date)
    (println "
## Original Prompt")
    (println (:raw_prompt state))
    (println "
## Agent Context")
    (println (:initial_context state))
    (println "
## Logical Changes")
    (doseq [i (:intents state)]
      (println (str "- " (:intent i) " (" (:context i) ")")))))

(case (first *command-line-args*)
  "init" (init-session (nth *command-line-args* 1) (nth *command-line-args* 2))
  "add" (add-intent (nth *command-line-args* 1) (nth *command-line-args* 2))
  "synthesize" (generate-synthesis (nth *command-line-args* 1) (nth *command-line-args* 2))
  (println "Usage: session_metadata.clj [init <prompt> <context> | add <intent> <context> | synthesize <result_summary> <chronos_date>]"))
