#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[cheshire.core :as json]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParent (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParent (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Rationale: Pure Clojure implementation of Nix mkShell environment logic.
;; Handles both structured attrs and traditional env vars.

(enable!)

(def ShellMainInput
  [:map])

(defn* validate-spec [:=> [:cat types/ShellSpec] :any] [spec]
  spec)

(defn* main [:=> [:cat ShellMainInput] :any] [_]
  (let [attrs-file (io/file ".attrs.json")
        ;; Support both structured attrs and traditional env vars
        spec-json (if (.exists attrs-file)
                    (get (json/parse-string (slurp attrs-file)) "spec")
                    (System/getenv "spec"))
        spec (json/parse-string spec-json true)
        out (System/getenv "out")
        out-bin (io/file out "bin")
        setup-file (io/file out "setup")]
    (if-not out
      (do (binding [*out* *err*] (println "Error: $out not set."))
          (System/exit 1))
      (do
        (validate-spec spec)
        (.mkdirs out-bin)
        (.mkdirs (io/file out))
        (let [shell-name (:name spec)
              packages (:packages spec)
              shell-hook (:shellHook spec)
              env (:env spec)
              ;; Construct PATH
              bin-paths (map #(str % "/bin") packages)
              path-str (str/join ":" bin-paths)
              ;; Generate setup script
              setup-content
              (str "# Mentci-AI Environment Setup (Clojure-Generated)\n"
                   "export PATH=\"" path-str ":$PATH\"\n"
                   (str/join "\n" (for [[k v] env] (str "export " (clojure.core/name k) "=\"" v "\"")))
                   "\n"
                   "# Run User Hook\n"
                   shell-hook
                   "\n")]
          (spit setup-file setup-content)
          (println (str "Generated shell setup for " shell-name " at " setup-file)))))))

(main {})
