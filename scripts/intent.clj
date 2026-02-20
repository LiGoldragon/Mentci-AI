#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me]
         '[clojure.java.io :as io])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: High-level orchestration of unique intent namespaces using jj-style hashes.

(def GenerateHashInput
  [:map])

(def SanitizeNameInput
  [:map
   [:name :string]])

(def IntentMainInput
  [:map
   [:args [:vector :string]]])

(defn generate-hash []
  (let [input {}]
    (when-not (m/validate GenerateHashInput input)
      (throw (ex-info "Invalid generate-hash input"
                      {:errors (me/humanize (m/explain GenerateHashInput input))}))))
  ;; jj uses short unique hashes. We'll simulate this with a small slice of a random UUID.
  (subs (str (java.util.UUID/randomUUID)) 0 8))

(defn sanitize-name [name]
  (let [input {:name name}]
    (when-not (m/validate SanitizeNameInput input)
      (throw (ex-info "Invalid sanitize-name input"
                      {:errors (me/humanize (m/explain SanitizeNameInput input))}))))
  (-> name
      (str/lower-case)
      (str/replace #"\s+" "-")
      (str/replace #"[^a-z0-9-]" "")))

(defn validate-config [config]
  (when-not (m/validate types/IntentInit config)
    (throw (ex-info "Invalid Intent Initialization"
                    {:errors (me/humanize (m/explain types/IntentInit config))}))))

(defn main []
  (let [args *command-line-args*
        input {:args (vec args)}]
    (when-not (m/validate IntentMainInput input)
      (throw (ex-info "Invalid main input"
                      {:errors (me/humanize (m/explain IntentMainInput input))})))
    (if (empty? args)
      (do (println "Usage: intent.clj <MeaningfulIntentName>")
          (System/exit 1))
      
      (let [intent-name (sanitize-name (first args))
            intent-hash (generate-hash)
            bookmark-name (str intent-hash "-" intent-name)
            config {:rawIntent (first args)
                    :intentName intent-name
                    :intentHash intent-hash
                    :bookmarkName bookmark-name}]
        (validate-config config)
        
        (println (str "Initializing Unique Intent: " bookmark-name))
        
        ;; Create a new change and bookmark it
        (let [res1 (sh "jj" "new" "dev" "-m" (str "intent: " (first args)))]
          (if (not= 0 (:exit res1))
            (do (println "Error creating new change:" (:err res1)) (System/exit (:exit res1)))
            
            (let [res2 (sh "jj" "bookmark" "create" bookmark-name "-r" "@")]
              (if (not= 0 (:exit res2))
                (do (println "Error creating bookmark:" (:err res2)) (System/exit (:exit res2)))
                
                (do (println (str "Bookmark '" bookmark-name "' created and active."))
                    (println bookmark-name)))))))))) ; Output name for shell integration

(when (= *file* (System/getProperty "babashka.file"))
  (main))
