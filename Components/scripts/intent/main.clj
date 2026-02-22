#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: High-level orchestration of unique intent namespaces using jj-style hashes.

(enable!)

(def GenerateHashInput
  [:map])

(def SanitizeNameInput
  [:map
   [:name :string]])

(def IntentMainInput
  [:map
   [:args [:vector :string]]])

(defprotocol IntentOps
  (generate-hash-for [this input])
  (sanitize-name-for [this input])
  (run-intent-for [this input]))

(defrecord DefaultIntent [])

(impl DefaultIntent IntentOps generate-hash-for GenerateHashInput :string
  [this input]
  (subs (str (java.util.UUID/randomUUID)) 0 8))

(impl DefaultIntent IntentOps sanitize-name-for SanitizeNameInput :string
  [this input]
  (let [name (:name input)]
    (-> name
        (str/lower-case)
        (str/replace #"\s+" "-")
        (str/replace #"[^a-z0-9-]" ""))))

(impl DefaultIntent IntentOps run-intent-for IntentMainInput :any
  [this input]
  (let [args (:args input)]
    (if (empty? args)
      (do (println "Usage: intent.clj <MeaningfulIntentName>")
          (System/exit 1))
      
      (let [intent-name (sanitize-name-for this {:name (first args)})
            intent-hash (generate-hash-for this {})
            bookmark-name (str intent-hash "-" intent-name)]
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

(def default-intent (->DefaultIntent))

(main IntentMainInput
  [input]
  (run-intent-for default-intent input))

(when (= *file* (System/getProperty "babashka.file"))
  (-main {:args (vec *command-line-args*)}))
