#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

(enable!)

(def SESSION_FILE ".mentci/session_state.json")

(def Input
  [:map
   [:args [:vector :string]]])

(def StateInput
  [:map
   [:state :map]])

(def InitInput
  [:map
   [:prompt :string]
   [:context :string]])

(def IntentInput
  [:map
   [:intent :string]
   [:context :string]])

(def SynthesisInput
  [:map
   [:resultSummary :string]
   [:chronosDate :string]])

(defprotocol SessionMetadataOps
  (load-session-for [this input])
  (save-session-for [this input])
  (init-session-for [this input])
  (add-intent-for [this input])
  (generate-synthesis-for [this input]))

(defrecord DefaultSessionMetadata [])

(impl DefaultSessionMetadata SessionMetadataOps load-session-for :map :map
  [this input]
  (if (.exists (io/file SESSION_FILE))
    (json/parse-string (slurp SESSION_FILE) true)
    {}))

(impl DefaultSessionMetadata SessionMetadataOps save-session-for StateInput :any
  [this input]
  (io/make-parents SESSION_FILE)
  (spit SESSION_FILE (json/generate-string (:state input) {:pretty true})))

(impl DefaultSessionMetadata SessionMetadataOps init-session-for InitInput :any
  [this input]
  (let [state {:raw_prompt (:prompt input)
               :initial_context (:context input)
               :start_rev (str/trim (:out (sh "jj" "log" "-r" "@" "--template" "change_id.short()")))
               :intents []}]
    (save-session-for this {:state state})
    (println "Session initialized.")))

(impl DefaultSessionMetadata SessionMetadataOps add-intent-for IntentInput :any
  [this input]
  (let [state (load-session-for this {})
        updated (update state :intents conj {:intent (:intent input) :context (:context input)})
        raw-prompt (or (:raw_prompt state) "")]
    (save-session-for this {:state updated})
    (println (str "intent: " (:intent input) "\n"
                  "[Prompt: " (subs raw-prompt 0 (min 100 (count raw-prompt))) "...]\n"
                  "[Context: " (:context input) "]"))))

(impl DefaultSessionMetadata SessionMetadataOps generate-synthesis-for SynthesisInput :any
  [this input]
  (let [state (load-session-for this {})]
    (println (str "session: " (:resultSummary input)))
    (println (:chronosDate input))
    (println "\n## Original Prompt")
    (println (:raw_prompt state))
    (println "\n## Agent Context")
    (println (:initial_context state))
    (println "\n## Logical Changes")
    (doseq [i (:intents state)]
      (println (str "- " (:intent i) " (" (:context i) ")")))))

(def default-session-metadata (->DefaultSessionMetadata))

(main Input
  [input]
  (let [args (:args input)
        cmd (first args)]
    (case cmd
      "init" (init-session-for default-session-metadata {:prompt (nth args 1) :context (nth args 2)})
      "add" (add-intent-for default-session-metadata {:intent (nth args 1) :context (nth args 2)})
      "synthesize" (generate-synthesis-for default-session-metadata {:resultSummary (nth args 1) :chronosDate (nth args 2)})
      (println "Usage: session_metadata.clj [init <prompt> <context> | add <intent> <context> | synthesize <result_summary> <chronos_date>]"))))

(-main {:args (vec *command-line-args*)})
