#!/usr/bin/env bb

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Native EDN support and fast startup for CLI logging.

(def logs-dir (io/file "Logs"))
(def default-github-user "LiGoldragon")

(defn get-current-user-id []
  (let [system-user (System/getProperty "user.name")]
    (if (= system-user "li")
      default-github-user
      system-user)))

(defn log-prompt [intent-summary & {:keys [model user-id] 
                                   :or {model "gemini-3-flash-preview"}}]
  (let [user (or user-id (get-current-user-id))
        log-file (io/file logs-dir (str "user_" user ".edn"))
        timestamp (.toString (java.time.LocalDateTime/now))
        entry {:timestamp timestamp
               :userId user
               :intentSummary intent-summary
               :model model
               :signature nil}]
    (when-not (.exists logs-dir)
      (.mkdirs logs-dir))
    (spit log-file (str (pr-str entry) "\n") :append true)
    (println (str "Logged intent (Clojure): '" intent-summary "' to " log-file))))

(let [args *command-line-args*]
  (if (empty? args)
    (println "Usage: logger.clj <intent> [--model <model>] [--user <user>]")
    (let [intent (first args)
          options (apply hash-map (map #(str/replace % #"^--" "") (rest args)))]
      (log-prompt intent 
                  :model (get options "model" (System/getenv "MENTCI_MODEL"))
                  :user-id (get options "user")))))
