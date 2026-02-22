#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :as sh])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Typing: Malli (metosin/malli)
;; Rationale: Native EDN support, fast startup, and data-driven schema validation.

(def logs-dir (io/file "Logs"))
(def default-github-user "LiGoldragon")

(enable!)

(def GetCurrentUserIdInput
  [:map])

(def LogPromptInput
  [:map
   [:intentSummary :string]
   [:model :string]
   [:userId :string]])

(def CurrentEclipticInput
  [:map])

(def LoggerMainInput
  [:map
   [:args [:vector :string]]])

(defprotocol LoggerOps
  (get-current-user-id-for [this input])
  (validate-entry-for [this input])
  (current-ecliptic-for [this input])
  (log-prompt-for [this input])
  (run-logger-for [this input]))

(defrecord DefaultLogger [])

(impl DefaultLogger LoggerOps get-current-user-id-for GetCurrentUserIdInput :string
  [this input]
  (let [system-user (System/getProperty "user.name")]
    (if (= system-user "li")
      default-github-user
      system-user)))

(impl DefaultLogger LoggerOps validate-entry-for types/IntentLog :any
  [this entry]
  entry)

(impl DefaultLogger LoggerOps current-ecliptic-for CurrentEclipticInput :string
  [this input]
  (let [{:keys [exit out]} (sh/sh "chronos" "--format" "numeric")]
    (if (zero? exit)
      (str/trim out)
      "12.1.28.44 | 5919 AM")))

(impl DefaultLogger LoggerOps log-prompt-for LogPromptInput :any
  [this input]
  (let [{:keys [intentSummary model userId]} input
        log-file (io/file logs-dir (str "user_" userId ".edn"))
        timestamp (.toString (java.time.LocalDateTime/now))
        ecliptic (current-ecliptic-for this {})
        entry {:timestamp timestamp
               :ecliptic ecliptic
               :userId userId
               :intentSummary intentSummary
               :model model
               :signature nil}]
    (validate-entry-for this entry)
    (when-not (.exists logs-dir)
      (.mkdirs logs-dir))
    (spit log-file (str (pr-str entry) "\n") :append true)
    (println (str "Logged intent (Clojure/Malli): '" intentSummary "' to " log-file))))

(impl DefaultLogger LoggerOps run-logger-for LoggerMainInput :any
  [this input]
  (let [args (:args input)]
    (if (empty? args)
      (println "Usage: logger.clj <intent> [--model <model>] [--user <user>]")
      (let [intent (first args)
            options (apply hash-map (map #(str/replace % #"^--" "") (rest args)))
            user (or (get options "user") (get-current-user-id-for this {}))
            model (get options "model" (System/getenv "MENTCI_MODEL"))]
        (log-prompt-for this {:intentSummary intent
                              :model model
                              :userId user})))))

(def default-logger (->DefaultLogger))

(main LoggerMainInput
  [input]
  (run-logger-for default-logger input))

(-main {:args (vec *command-line-args*)})
