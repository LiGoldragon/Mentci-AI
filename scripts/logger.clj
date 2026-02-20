#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

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

(def LoggerMainInput
  [:map
   [:args [:vector :string]]])

(defn* get-current-user-id [:=> [:cat GetCurrentUserIdInput] :string] [_]
  (let [system-user (System/getProperty "user.name")]
    (if (= system-user "li")
      default-github-user
      system-user)))

(defn* validate-entry [:=> [:cat types/IntentLog] :any] [entry]
  entry)

(defn* log-prompt [:=> [:cat LogPromptInput] :any] [input]
  (let [{:keys [intentSummary model userId]} input
        log-file (io/file logs-dir (str "user_" userId ".edn"))
        timestamp (.toString (java.time.LocalDateTime/now))
        ecliptic "12.1.28.44 | 5919 AM" ; TODO: Implement dynamic ecliptic calculation
        entry {:timestamp timestamp
               :ecliptic ecliptic
               :userId userId
               :intentSummary intentSummary
               :model model
               :signature nil}]
    (validate-entry entry)
    (when-not (.exists logs-dir)
      (.mkdirs logs-dir))
    (spit log-file (str (pr-str entry) "\n") :append true)
    (println (str "Logged intent (Clojure/Malli): '" intentSummary "' to " log-file))))

(defn* main [:=> [:cat LoggerMainInput] :any] [input]
  (let [args (:args input)]
    (if (empty? args)
      (println "Usage: logger.clj <intent> [--model <model>] [--user <user>]")
      (let [intent (first args)
            options (apply hash-map (map #(str/replace % #"^--" "") (rest args)))
            user (or (get options "user") (get-current-user-id {}))
            model (get options "model" (System/getenv "MENTCI_MODEL"))]
        (log-prompt {:intentSummary intent
                     :model model
                     :userId user})))))

(main {:args (vec *command-line-args*)})
