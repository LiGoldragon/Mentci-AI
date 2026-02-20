#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(ns agent-launcher
  (:require [clojure.string :as str]
            [clojure.java.shell :as sh]
            [clojure.java.io :as io]))

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(def default-gopass-prefix "mentci/ai")
(def default-command ["opencode"])
(def env-by-provider
  {"deepseek" "DEEPSEEK_API_KEY"
   "openai" "OPENAI_API_KEY"
   "anthropic" "ANTHROPIC_API_KEY"})

(enable!)

(def FailInput
  [:map
   [:message :string]])

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def ResolveEntryInput
  [:map
   [:prefix :string]
   [:provider :string]
   [:entry [:maybe :string]]])

(def ResolveEnvVarInput
  [:map
   [:provider :string]
   [:envVar [:maybe :string]]])

(def GopassSecretInput
  [:map
   [:entry :string]])

(def LauncherMainInput
  [:map
   [:args [:vector :string]]])

(defn* fail [:=> [:cat FailInput] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* parse-args [:=> [:cat ParseArgsInput] :map] [input]
  (let [args (:args input)]
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
            (assoc opts :command (vec remaining))))))))

(defn* resolve-entry [:=> [:cat ResolveEntryInput] :string] [input]
  (let [{:keys [prefix provider entry]} input]
    (or entry (str prefix "/" provider "/api-key"))))

(defn* resolve-env-var [:=> [:cat ResolveEnvVarInput] :string] [input]
  (let [{:keys [provider envVar]} input]
    (or envVar
        (get env-by-provider provider)
        (fail {:message (str "Unknown provider '" provider "'. Use --env-var or extend env-by-provider.")}))))

(defn* gopass-secret [:=> [:cat GopassSecretInput] :string] [input]
  (let [entry (:entry input)
        {:keys [exit out err]} (sh/sh "gopass" "show" "--password" entry)]
    (when-not (zero? exit)
      (fail {:message (str "gopass failed for " entry ": " (str/trim (or err out)))}))
    (str/trim out)))

(defn* -main [:=> [:cat LauncherMainInput] :any] [input]
  (let [{:keys [provider gopass-prefix entry env-var command]}
        (parse-args {:args (:args input)})
        provider (or provider (fail {:message "Missing --provider"}))
        prefix (or gopass-prefix (System/getenv "MENTCI_GOPASS_PREFIX") default-gopass-prefix)
        entry (resolve-entry {:prefix prefix :provider provider :entry entry})
        env-var (resolve-env-var {:provider provider :envVar env-var})
        cmd (if (seq command) command default-command)
        cmd (if (= (first cmd) "--") (vec (rest cmd)) cmd)
        cmd (if (seq cmd) cmd default-command)
        api-key (gopass-secret {:entry entry})]
    (let [env (assoc (into {} (System/getenv)) env-var api-key)
          {:keys [exit]} (apply sh/sh {:env env} cmd)]
      (System/exit exit))))

(-main {:args (vec *command-line-args*)})
