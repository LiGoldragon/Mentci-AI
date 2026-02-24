#!/usr/bin/env bb

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[babashka.process :refer [sh process]])

(defn log [msg]
  (println (str "[pi-sync] " msg)))

(defn get-installed-packages []
  (let [res (sh "pi" "list")
        out (:out res)]
    (if (str/includes? out "No packages installed")
      #{}
      (->> (str/split-lines out)
           (map str/trim)
           (filter #(and (not (str/blank? %))
                         (not (str/starts-with? % "Packages:"))))
           ;; Extract package name from "name (source)" format if present
           (map #(first (str/split % #" ")))
           set))))

(defn install-package [pkg]
  (log (str "Installing " pkg "..."))
  (let [res (sh "pi" "install" "-l" pkg)]
    (if (zero? (:exit res))
      (log (str "Successfully installed " pkg))
      (log (str "Failed to install " pkg ": " (:err res))))))

(defn sync-settings [settings]
  (let [settings-path ".pi/settings.json"
        current-settings (if (.exists (io/file settings-path))
                           (try
                             (json/parse-string (slurp settings-path) true)
                             (catch Exception _ {}))
                           {})
        ;; Deep merge settings
        new-settings (merge current-settings settings)]
    (when (not= current-settings new-settings)
      (log "Updating .pi/settings.json...")
      (io/make-parents settings-path)
      (spit settings-path (json/generate-string new-settings {:pretty true})))))

(defn -main []
  (let [config-path ".pi/extensions.edn"
        default-config {:packages
                        ["npm:@aliou/pi-linkup"
                         "npm:@juanibiapina/pi-plan"
                         "npm:@oh-my-pi/subagents"
                         "npm:@oh-my-pi/lsp"
                         "npm:mcporter"
                         "npm:@oh-my-pi/omp-stats"]
                        :settings
                        {:theme "dark"
                         :defaultProvider "google-gemini-cli"
                         :defaultModel "gemini-2.5-flash"
                         :compaction {:enabled true :reserveTokens 16384 :keepRecentTokens 20000}}}]
    
    (when-not (.exists (io/file ".pi"))
      (.mkdir (io/file ".pi")))

    (let [config (if (.exists (io/file config-path))
                   (do
                     (log (str "Loading config from " config-path))
                     (edn/read-string (slurp config-path)))
                   (do
                     (log (str "No config found at " config-path ". Using defaults."))
                     (spit config-path (with-out-str (clojure.pprint/pprint default-config)))
                     default-config))
          packages (:packages config)
          installed (get-installed-packages)]
      
      (doseq [pkg packages]
        (let [pkg-name (last (str/split pkg #":"))]
          (if-not (contains? installed pkg-name)
            (install-package pkg)
            (log (str pkg " is already installed.")))))
      
      (when-let [settings (:settings config)]
        ;; Note: cheshire is not a default bb pod, but we can use bb's built-in json
        ;; Wait, bb has clojure.data.json or babashka.json?
        ;; BB has internal json support via 'cheshire.core' or similar? 
        ;; Actually BB has 'cheshire.core' built-in.
        (sync-settings settings)))))

(try
  ;; Require cheshire inside to avoid failure if not available (should be in BB)
  (require '[cheshire.core :as json])
  (-main)
  (catch Exception e
    (log (str "Error: " (.getMessage e)))
    (System/exit 1)))
