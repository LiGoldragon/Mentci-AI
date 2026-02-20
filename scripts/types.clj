(ns types
  (:require [malli.core :as m]
            [malli.util :as mu]))

;; -- Mentci-AI Core Schemas (Malli) --

(def IntentLog
  [:map
   [:sema/type [:= "IntentLog"]]
   [:timestamp :string]
   [:ecliptic :string] ; Format: "Sign.Degree.Minute.Second | YEAR AM"
   [:userId :string]
   [:intentSummary :string]
   [:model :string]
   [:signature [:maybe :string]]])

(def InputManifest
  [:map-of :keyword [:map-of :keyword :string]])

(def JailConfig
  [:map
   [:sema/type [:= "JailConfig"]]
   [:inputsPath :string]
   [:inputManifest InputManifest]])

(def CommitContext
  [:map
   [:sema/type [:= "CommitContext"]]
   [:message :string]
   [:bookmark :string]
   [:repoRoot :string]
   [:workspaceRoot :string]])

(def ShellSpec
  [:map
   [:sema/type [:= "ShellSpec"]]
   [:name :string]
   [:packages [:vector :string]]
   [:shellHook :string]
   [:env [:map-of :keyword :any]]])

(def AgentLauncherConfig
  [:map
   [:sema/type [:= "AgentLauncherConfig"]]
   [:provider :string]
   [:gopassPrefix :string]
   [:entry :string]
   [:envVar :string]
   [:command [:vector :string]]])

(def IntentInit
  [:map
   [:sema/type [:= "IntentInit"]]
   [:rawIntent :string]
   [:intentName :string]
   [:intentHash :string]
   [:bookmarkName :string]])

(def JJWorkflowConfig
  [:map
   [:sema/type [:= "JJWorkflowConfig"]]
   [:command [:enum "status" "log" "commit"]]
   [:workspaceRoot :string]
   [:targetBookmark :string]
   [:message [:maybe :string]]])

(def DependencyCheckConfig
  [:map
   [:sema/type [:= "DependencyCheckConfig"]]
   [:deps [:vector :string]]])
