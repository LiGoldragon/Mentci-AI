#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.string :as str]
         '[clojure.java.shell :as sh]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

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

(def RunInput
  [:map
   [:provider :string]
   [:gopass-prefix [:maybe :string]]
   [:entry [:maybe :string]]
   [:env-var [:maybe :string]]
   [:command [:maybe [:vector :string]]]])

(def Input
  [:map
   [:args [:vector :string]]])

(defprotocol AgentLauncherOps
  (fail-for [this input])
  (parse-args-for [this input])
  (resolve-entry-for [this input])
  (resolve-env-var-for [this input])
  (gopass-secret-for [this input])
  (run-agent-for [this input]))

(defrecord DefaultAgentLauncher [])

(impl DefaultAgentLauncher AgentLauncherOps fail-for FailInput :any
  [this input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(impl DefaultAgentLauncher AgentLauncherOps parse-args-for ParseArgsInput :map
  [this input]
  (let [args (:args input)]
    (loop [remaining args
           opts {}]
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

(impl DefaultAgentLauncher AgentLauncherOps resolve-entry-for ResolveEntryInput :string
  [this input]
  (let [{:keys [prefix provider entry]} input]
    (or entry (str prefix "/" provider "/api-key"))))

(impl DefaultAgentLauncher AgentLauncherOps resolve-env-var-for ResolveEnvVarInput :string
  [this input]
  (let [{:keys [provider envVar]} input]
    (or envVar
        (get env-by-provider provider)
        (fail-for this {:message (str "Unknown provider '" provider "'. Use --env-var or extend env-by-provider.")}))))

(impl DefaultAgentLauncher AgentLauncherOps gopass-secret-for GopassSecretInput :string
  [this input]
  (let [entry (:entry input)
        {:keys [exit out err]} (sh/sh "gopass" "show" "--password" entry)]
    (when-not (zero? exit)
      (fail-for this {:message (str "gopass failed for " entry ": " (str/trim (or err out)))}))
    (str/trim out)))

(impl DefaultAgentLauncher AgentLauncherOps run-agent-for RunInput :any
  [this input]
  (let [{:keys [provider gopass-prefix entry env-var command]} input
        prefix (or gopass-prefix (System/getenv "MENTCI_GOPASS_PREFIX") default-gopass-prefix)
        entry-path (resolve-entry-for this {:prefix prefix :provider provider :entry entry})
        env-name (resolve-env-var-for this {:provider provider :envVar env-var})
        cmd0 (if (seq command) command default-command)
        cmd1 (if (= (first cmd0) "--") (vec (rest cmd0)) cmd0)
        cmd (if (seq cmd1) cmd1 default-command)
        api-key (gopass-secret-for this {:entry entry-path})
        env (assoc (into {} (System/getenv)) env-name api-key)
        {:keys [exit]} (apply sh/sh {:env env} cmd)]
    (System/exit exit)))

(def default-agent-launcher (->DefaultAgentLauncher))

(main Input
  [input]
  (let [{:keys [provider gopass-prefix entry env-var command]}
        (parse-args-for default-agent-launcher {:args (:args input)})
        provider (or provider (fail-for default-agent-launcher {:message "Missing --provider"}))]
    (run-agent-for default-agent-launcher {:provider provider
                                           :gopass-prefix gopass-prefix
                                           :entry entry
                                           :env-var env-var
                                           :command command})))

(-main {:args (vec *command-line-args*)})
