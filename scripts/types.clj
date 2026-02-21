(ns types
  (:require [malli.core :as m]
            [malli.util :as mu]))

;; -- Mentci-AI Core Schemas (Malli) --

(def IntentLog
  [:map
   [:timestamp :string]
   [:ecliptic :string] ; Format: "Sign.Degree.Minute.Second | YEAR AM"
   [:userId :string]
   [:intentSummary :string]
   [:model :string]
   [:signature [:maybe :string]]])

(def InputManifest
  [:map-of
   :keyword
   [:map
    [:sourcePath :string]
    [:srcPath {:optional true} :string]
    [:inputType :string]]])

(def OutputManifest
  [:map-of
   :keyword
   [:map
    [:workspaceName :string]
    [:workingBookmark :string]
    [:targetBookmark :string]]])

(def JailConfig
  [:map
   [:inputsPath :string]
   [:inputManifest InputManifest]
   [:outputsPath {:optional true} :string]
   [:outputManifest {:optional true} OutputManifest]
   [:policyPath {:optional true} :string]])

(def CommitContext
  [:map
   [:message :string]
   [:bookmark :string]
   [:repoRoot :string]
   [:workspaceRoot :string]])

(def ShellSpec
  [:map
   [:name :string]
   [:packages [:vector :string]]
   [:shellHook :string]
   [:env [:map-of :keyword :any]]])

(def AgentLauncherConfig
  [:map
   [:provider :string]
   [:gopassPrefix :string]
   [:entry :string]
   [:envVar :string]
   [:command [:vector :string]]])

(def IntentInit
  [:map
   [:rawIntent :string]
   [:intentName :string]
   [:intentHash :string]
   [:bookmarkName :string]])

(def JJWorkflowConfig
  [:map
   [:command [:enum "status" "log" "commit"]]
   [:workspaceRoot :string]
   [:targetBookmark :string]
   [:message [:maybe :string]]])

(def DependencyCheckConfig
  [:map
   [:deps [:vector :string]]])
