#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.java.shell :as sh]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def FailInput
  [:map
   [:message :string]])

(def PromptInput
  [:map
   [:label :string]
   [:default [:maybe :string]]])

(def ConfirmInput
  [:map
   [:label :string]
   [:defaultYes :boolean]])

(def CommandInput
  [:map
   [:argv [:vector :string]]
   [:in [:maybe :string]]])

(def ExistsInput
  [:map
   [:path :string]])

(def EnsureToolInput
  [:map
   [:tool :string]])

(def KeygenInput
  [:map
   [:keyPath :string]
   [:comment :string]
   [:overwrite :boolean]])

(def StoreInput
  [:map
   [:entry :string]
   [:privateKey :string]
   [:publicKey :string]])

(def CleanupInput
  [:map
   [:path :string]])

(defprotocol JailKeySetupOps
  (fail-for [this input])
  (run-command-for [this input])
  (prompt-for [this input])
  (confirm-for [this input])
  (exists-for [this input])
  (ensure-tool-for [this input])
  (ensure-parent-dir-for [this input])
  (generate-keypair-for [this input])
  (store-secret-for [this input])
  (cleanup-file-for [this input])
  (usage-for [this])
  (run-setup-for [this input]))

(defrecord DefaultJailKeySetup [])

(def canonical-gopass-entry "mentci-ai/admin-agent")
(def canonical-public-key-field "public_key")

(defn shell-quote
  [value]
  (str "'" (str/replace (str value) "'" "'\\''") "'"))

(impl DefaultJailKeySetup JailKeySetupOps fail-for FailInput :any
  [this input]
  (let [_ this]
    (binding [*out* *err*]
      (println (:message input)))
    (System/exit 1)))

(impl DefaultJailKeySetup JailKeySetupOps run-command-for CommandInput :map
  [this input]
  (let [_ this]
    (try
      (let [result (apply sh/sh (:argv input))]
        {:exit (:exit result)
         :out (:out result)
         :err (:err result)})
      (catch java.io.IOException e
        {:exit 127
         :out ""
         :err (.getMessage e)}))))

(impl DefaultJailKeySetup JailKeySetupOps prompt-for PromptInput :string
  [this input]
  (let [{:keys [label default]} input
        prompt-text (if (str/blank? (str default))
                      (str label ": ")
                      (str label " [" default "]: "))]
    (print prompt-text)
    (flush)
    (let [raw (or (read-line) "")
          value (str/trim raw)]
      (if (str/blank? value)
        (or default "")
        value))))

(impl DefaultJailKeySetup JailKeySetupOps confirm-for ConfirmInput :boolean
  [this input]
  (let [{:keys [label defaultYes]} input
        suffix (if defaultYes "[Y/n]" "[y/N]")
        value (prompt-for this {:label (str label " " suffix) :default nil})]
    (if (str/blank? value)
      defaultYes
      (contains? #{"y" "yes"} (str/lower-case value)))))

(impl DefaultJailKeySetup JailKeySetupOps exists-for ExistsInput :boolean
  [this input]
  (.exists (io/file (:path input))))

(impl DefaultJailKeySetup JailKeySetupOps ensure-tool-for EnsureToolInput :any
  [this input]
  (let [tool (:tool input)
        result (run-command-for this {:argv ["sh" "-lc" (str "command -v " tool " >/dev/null 2>&1")] :in nil})]
    (when-not (zero? (:exit result))
      (fail-for this {:message (str "Required tool not found on PATH: " tool)}))))

(impl DefaultJailKeySetup JailKeySetupOps ensure-parent-dir-for {:path :string} :any
  [this input]
  (let [target (io/file (:path input))
        parent (.getParentFile target)]
    (when parent
      (.mkdirs parent))))

(impl DefaultJailKeySetup JailKeySetupOps generate-keypair-for KeygenInput :any
  [this input]
  (let [{:keys [keyPath comment overwrite]} input
        key-file (io/file keyPath)]
    (when (and (.exists key-file) (not overwrite))
      (fail-for this {:message (str "Key already exists: " keyPath " (overwrite disabled).")}))
    (when (.exists key-file)
      (io/delete-file key-file true))
    (let [pub-path (str keyPath ".pub")
          pub-file (io/file pub-path)]
      (when (.exists pub-file)
        (io/delete-file pub-file true)))
    (ensure-parent-dir-for this {:path keyPath})
    (let [result (run-command-for this {:argv ["ssh-keygen" "-t" "ed25519" "-a" "64" "-N" "" "-f" keyPath "-C" comment]
                                        :in nil})]
      (when-not (zero? (:exit result))
        (fail-for this {:message (str "ssh-keygen failed:\n" (or (:err result) (:out result)))})))))

(impl DefaultJailKeySetup JailKeySetupOps store-secret-for StoreInput :any
  [this input]
  (let [{:keys [entry privateKey publicKey]} input
        tmp-file (java.io.File/createTempFile "mentci-gopass-" ".tmp")
        tmp-path (.getPath tmp-file)]
    (try
      (spit tmp-path
            (str "private_key: |\n"
                 (str/join "\n" (map #(str "  " %) (str/split-lines privateKey)))
                 "\n"
                 canonical-public-key-field ": "
                 (str/trim publicKey)
                 "\nalgorithm: ed25519\n"))
      (let [command (str "gopass insert -m "
                         "-f "
                         (shell-quote entry)
                         " < "
                         (shell-quote tmp-path))
            result (run-command-for this {:argv ["sh" "-lc" command]
                                          :in nil})]
        (when-not (zero? (:exit result))
          (fail-for this {:message (str "Failed to store gopass entry " entry ":\n"
                                        (str/trim (str (:err result) "\n" (:out result))))})))
      (finally
        (when (.exists (io/file tmp-path))
          (io/delete-file tmp-path true))))))

(impl DefaultJailKeySetup JailKeySetupOps cleanup-file-for CleanupInput :any
  [this input]
  (let [path (:path input)]
    (when (exists-for this {:path path})
      (let [shred (run-command-for this {:argv ["shred" "-u" path] :in nil})]
        (when-not (zero? (:exit shred))
          (let [rm (run-command-for this {:argv ["rm" "-f" path] :in nil})]
            (when-not (zero? (:exit rm))
              (fail-for this {:message (str "Failed to remove temporary file: " path)}))))))))

(impl DefaultJailKeySetup JailKeySetupOps usage-for [:=> [:cat :any] :any]
  [this]
  (let [_ this]
    (println "Usage: bb Components/scripts/jail_key_setup/main.clj")
    (println "")
    (println "Interactive setup for the deterministic gopass key entry:")
    (println "- entry: mentci-ai/admin-agent")
    (println "- named field: public_key")))

(impl DefaultJailKeySetup JailKeySetupOps run-setup-for Input :any
  [this input]
  (let [args (:args input)]
    (when (some #{"--help" "-h"} args)
      (usage-for this)
      (System/exit 0))
    (when (seq args)
      (fail-for this {:message "This script is interactive only. Use --help for usage."}))

    (ensure-tool-for this {:tool "ssh-keygen"})
    (ensure-tool-for this {:tool "gopass"})

    (let [default-key-path "/tmp/mentci_admin_ed25519"
          default-comment "mentci-ai-admin-agent"
          key-path (prompt-for this {:label "Temporary key path" :default default-key-path})
          comment (prompt-for this {:label "SSH key comment" :default default-comment})
          overwrite (confirm-for this {:label "Overwrite existing key file if present?" :defaultYes false})
          cleanup (confirm-for this {:label "Delete local temporary key files after import?" :defaultYes true})]
      (generate-keypair-for this {:keyPath key-path :comment comment :overwrite overwrite})
      (let [pub-path (str key-path ".pub")]
        (when-not (exists-for this {:path pub-path})
          (fail-for this {:message (str "Public key file not found: " pub-path)}))
        (store-secret-for this {:entry canonical-gopass-entry
                                :privateKey (slurp key-path)
                                :publicKey (slurp pub-path)}))
      (when cleanup
        (cleanup-file-for this {:path key-path})
        (cleanup-file-for this {:path (str key-path ".pub")}))
      (println "Jail key setup complete.")
      (println (str "Private key entry: " canonical-gopass-entry))
      (println (str "Public key field: " canonical-public-key-field))
      (println "Expected jail runtime target: <workspaceRoot>/.mentci/keys/git_ed25519"))))

(def default-jail-key-setup (->DefaultJailKeySetup))

(main Input
  [input]
  (run-setup-for default-jail-key-setup input))

(-main {:args (vec *command-line-args*)})
