#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.java.shell :as sh]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

;; Tool Stack Transparency:
;; Runtime: Babashka
;; Origin: clojure.java.shell / Java ProcessBuilder
;; Rationale: deterministic bootstrap + interactive jailed terminal handoff.

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(defprotocol AdminShellOps
  (run-admin-shell-for [this input]))

(defrecord DefaultAdminShell [])

(def Defaults
  {:outputName "mentci-ai-admin"
   :outputsDir "Outputs"
   :workingBookmark "dev"
   :targetBookmark "jailCommit"
   :cmd nil
   :help false})

(defn print-help []
  (println "Usage: mentci-admin-shell [options]")
  (println "Options:")
  (println "  --output-name <name>         Workspace directory name under Outputs/")
  (println "  --outputs-dir <name>         Outputs root directory (default: Outputs)")
  (println "  --working-bookmark <name>    Source bookmark/revision (default: dev)")
  (println "  --target-bookmark <name>     Target bookmark for mentci-commit (default: jailCommit)")
  (println "  --cmd <command>              Run one command instead of opening interactive shell")
  (println "  -h, --help                   Show this help"))

(defn* parse-args
  [:=> [:cat [:vector :string]] [:map
                                 [:outputName :string]
                                 [:outputsDir :string]
                                 [:workingBookmark :string]
                                 [:targetBookmark :string]
                                 [:cmd [:maybe :string]]
                                 [:help :boolean]]]
  [args]
  (loop [cfg Defaults
         remaining args]
    (if (empty? remaining)
      cfg
      (let [[arg & tail] remaining]
        (case arg
          "-h" (assoc cfg :help true)
          "--help" (assoc cfg :help true)
          "--output-name" (recur (assoc cfg :outputName (or (first tail) "")) (rest tail))
          "--outputs-dir" (recur (assoc cfg :outputsDir (or (first tail) "")) (rest tail))
          "--working-bookmark" (recur (assoc cfg :workingBookmark (or (first tail) "")) (rest tail))
          "--target-bookmark" (recur (assoc cfg :targetBookmark (or (first tail) "")) (rest tail))
          "--cmd" (recur (assoc cfg :cmd (or (first tail) "")) (rest tail))
          (do
            (println (str "Unknown option: " arg))
            (print-help)
            (System/exit 2)))))))

(defn* run-capture
  [:=> [:cat [:vector :string] :string] [:map [:exit :int] [:out :string] [:err :string]]]
  [argv dir]
  (let [result (apply sh/sh (concat argv [:dir dir]))]
    {:exit (:exit result)
     :out (:out result)
     :err (:err result)}))

(defn* run-interactive
  [:=> [:cat [:vector :string] :string] :int]
  [argv dir]
  (let [pb (ProcessBuilder. (into-array String argv))]
    (.directory pb (io/file dir))
    (.inheritIO pb)
    (let [process (.start pb)]
      (.waitFor process))))

(defn* require-jj-root
  [:=> [:cat :string] :string]
  [repo-root]
  (let [jj-dir (io/file repo-root ".jj")]
    (when-not (.exists jj-dir)
      (println (str "Error: not a jj repository root: " repo-root))
      (System/exit 1))
    repo-root))

(defn* non-empty-or-die
  [:=> [:cat :string :string] :string]
  [label value]
  (if (str/blank? value)
    (do
      (println (str "Error: " label " must not be empty."))
      (System/exit 2))
    value))

(impl DefaultAdminShell AdminShellOps run-admin-shell-for Input :any
  [this input]
  (let [cfg (parse-args (:args input))]
    (when (:help cfg)
      (print-help)
      (System/exit 0))
    (let [repo-root (require-jj-root (.getCanonicalPath (io/file ".")))
          output-name (non-empty-or-die "--output-name" (:outputName cfg))
          outputs-dir (non-empty-or-die "--outputs-dir" (:outputsDir cfg))
          working-bookmark (non-empty-or-die "--working-bookmark" (:workingBookmark cfg))
          target-bookmark (non-empty-or-die "--target-bookmark" (:targetBookmark cfg))
          workspace-root (str repo-root "/" outputs-dir "/" output-name)
          bootstrap-argv ["cargo" "run" "--quiet"
                          "--manifest-path" "Components/Cargo.toml"
                          "--bin" "mentci-ai" "--"
                          "job/jails" "bootstrap"
                          "--repo-root" repo-root
                          "--outputs-dir" outputs-dir
                          "--output-name" output-name
                          "--working-bookmark" working-bookmark
                          "--target-bookmark" target-bookmark]
          bootstrap-result (run-capture bootstrap-argv repo-root)]
      (when (not= 0 (:exit bootstrap-result))
        (when-not (str/blank? (:out bootstrap-result))
          (print (:out bootstrap-result)))
        (when-not (str/blank? (:err bootstrap-result))
          (binding [*out* *err*] (print (:err bootstrap-result))))
        (System/exit (:exit bootstrap-result)))
      (println (str "workspaceRoot=" workspace-root))
      (let [shell (or (System/getenv "SHELL") "bash")
            jail-argv (if (str/blank? (:cmd cfg))
                        ["mentci-jail-run" shell "-l"]
                        ["mentci-jail-run" "sh" "-lc" (:cmd cfg)])
            exit-code (run-interactive jail-argv workspace-root)]
        (System/exit exit-code)))))

(def default-admin-shell (->DefaultAdminShell))

(main Input
  [input]
  (run-admin-shell-for default-admin-shell input))

(when (= *file* (System/getProperty "babashka.file"))
  (-main {:args (vec *command-line-args*)}))
