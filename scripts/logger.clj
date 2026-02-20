#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Typing: Malli (metosin/malli)
;; Rationale: Native EDN support, fast startup, and data-driven schema validation.

(def logs-dir (io/file "Logs"))
(def default-github-user "LiGoldragon")

(def GetCurrentUserIdInput
  [:map
   [:sema/type [:= "GetCurrentUserIdInput"]]])

(def LogPromptInput
  [:map
   [:sema/type [:= "LogPromptInput"]]
   [:intentSummary :string]
   [:model :string]
   [:userId :string]])

(defn get-current-user-id []
  (let [input {:sema/type "GetCurrentUserIdInput"}
        _ (when-not (m/validate GetCurrentUserIdInput input)
            (throw (ex-info "Invalid get-current-user-id input"
                            {:errors (me/humanize (m/explain GetCurrentUserIdInput input))})))
        system-user (System/getProperty "user.name")]
    (if (= system-user "li")
      default-github-user
      system-user)))

(defn validate-entry [entry]
  (when-not (m/validate types/IntentLog entry)
    (throw (ex-info "Invalid Log Entry"
                    {:errors (me/humanize (m/explain types/IntentLog entry))}))))

(defn log-prompt [intent-summary & {:keys [model user-id] 
                                   :or {model "gemini-3-flash-preview"}}]
  (let [user (or user-id (get-current-user-id))
        input {:sema/type "LogPromptInput"
               :intentSummary intent-summary
               :model model
               :userId user}
        _ (when-not (m/validate LogPromptInput input)
            (throw (ex-info "Invalid log-prompt input"
                            {:errors (me/humanize (m/explain LogPromptInput input))})))
        log-file (io/file logs-dir (str "user_" user ".edn"))
        timestamp (.toString (java.time.LocalDateTime/now))
        ecliptic "12.1.28.44 | 5919 AM" ; TODO: Implement dynamic ecliptic calculation
        entry {:sema/type "IntentLog"
               :timestamp timestamp
               :ecliptic ecliptic
               :userId user
               :intentSummary intent-summary
               :model model
               :signature nil}]
    (validate-entry entry)
    (when-not (.exists logs-dir)
      (.mkdirs logs-dir))
    (spit log-file (str (pr-str entry) "\n") :append true)
    (println (str "Logged intent (Clojure/Malli): '" intent-summary "' to " log-file))))

(let [args *command-line-args*]
  (if (empty? args)
    (println "Usage: logger.clj <intent> [--model <model>] [--user <user>]")
    (let [intent (first args)
          options (apply hash-map (map #(str/replace % #"^--" "") (rest args)))]
      (log-prompt intent 
                  :model (get options "model" (System/getenv "MENTCI_MODEL"))
                  :user-id (get options "user")))))
