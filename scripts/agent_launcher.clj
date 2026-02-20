#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(ns agent-launcher
  (:require [clojure.string :as str]
            [clojure.java.shell :as sh]
            [malli.core :as m]
            [malli.error :as me]
            [clojure.java.io :as io]))

(load-file (str (.getParent (io/file *file*)) "/types.clj"))

(def default-gopass-prefix "mentci/ai")
(def default-command ["opencode"])
(def env-by-provider
  {"deepseek" "DEEPSEEK_API_KEY"
   "openai" "OPENAI_API_KEY"
   "anthropic" "ANTHROPIC_API_KEY"})

(defn fail [msg]
  (binding [*out* *err*]
    (println msg))
  (System/exit 1))

(defn parse-args [args]
  (loop [remaining args
         {:keys [provider] :as opts} {}]
    (if (empty? remaining)
      opts
      (let [[arg & more] remaining]
        (cond
          (= arg "--provider")
          (recur (nnext remaining) (assoc opts :provider (second remaining)))

          (= arg "--gopass-prefix")
          (recur (nnext remaining) (assoc opts :gopass-prefix (second remaining)))

          (= arg "--entry")
          (recur (nnext remaining) (assoc opts :entry (second remaining)))

          (= arg "--env-var")
          (recur (nnext remaining) (assoc opts :env-var (second remaining)))

          (= arg "--")
          (assoc opts :command (vec more))

          :else
          (assoc opts :command (vec remaining)))))))

(defn resolve-entry [prefix provider entry]
  (or entry (str prefix "/" provider "/api-key")))

(defn resolve-env-var [provider env-var]
  (or env-var
      (get env-by-provider provider)
      (fail (str "Unknown provider '" provider "'. Use --env-var or extend env-by-provider."))))

(defn gopass-secret [entry]
  (let [{:keys [exit out err]} (sh/sh "gopass" "show" "--password" entry)]
    (when-not (zero? exit)
      (fail (str "gopass failed for " entry ": " (str/trim (or err out)))))
    (str/trim out)))

(defn validate-config [config]
  (when-not (m/validate types/AgentLauncherConfig config)
    (throw (ex-info "Invalid Agent Launcher Configuration"
                    {:errors (me/humanize (m/explain types/AgentLauncherConfig config))}))))

(defn -main [& args]
  (let [{:keys [provider gopass-prefix entry env-var command]}
        (parse-args args)
        provider (or provider (fail "Missing --provider"))
        prefix (or gopass-prefix (System/getenv "MENTCI_GOPASS_PREFIX") default-gopass-prefix)
        entry (resolve-entry prefix provider entry)
        env-var (resolve-env-var provider env-var)
        cmd (if (seq command) command default-command)
        cmd (if (= (first cmd) "--") (vec (rest cmd)) cmd)
        cmd (if (seq cmd) cmd default-command)
        config {:sema/type "AgentLauncherConfig"
                :provider provider
                :gopassPrefix prefix
                :entry entry
                :envVar env-var
                :command cmd}
        _ (validate-config config)
        api-key (gopass-secret entry)]
    (let [env (assoc (into {} (System/getenv)) env-var api-key)
          {:keys [exit]} (apply sh/sh {:env env} cmd)]
      (System/exit exit))))

(apply -main *command-line-args*)
