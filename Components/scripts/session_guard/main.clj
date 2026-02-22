#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def CommandInput
  [:map
   [:args [:vector :string]]])

(def ParseInput
  [:map
   [:raw :string]])

(def DecideInput
  [:map
   [:descriptions [:vector :string]]])

(def ValidateSessionMessageInput
  [:map
   [:description :string]])

(defprotocol SessionGuardOps
  (run-command-for [this input])
  (parse-descriptions-for [this input])
  (read-head-description-for [this input])
  (fail-for [this input])
  (pass-for [this input])
  (classify-prefix-for [this input])
  (decide-for [this input])
  (validate-session-message-for [this input])
  (validate-push-for [this input]))

(defrecord DefaultSessionGuard [])

(impl DefaultSessionGuard SessionGuardOps run-command-for CommandInput :map
  [this input]
  (let [result (apply sh (:args input))]
    {:exit (:exit result)
     :out (:out result)
     :err (:err result)}))

(impl DefaultSessionGuard SessionGuardOps parse-descriptions-for ParseInput [:vector :string]
  [this input]
  (->> (str/split-lines (:raw input))
       (map str/trim)
       (remove str/blank?)
       vec))

(impl DefaultSessionGuard SessionGuardOps read-head-description-for :map :string
  [this input]
  (let [result (run-command-for this {:args ["git" "log" "-n" "1" "--pretty=%B"]})]
    (when-not (= 0 (:exit result))
      (fail-for this {:message (str "Session guard failed: unable to read HEAD commit description.\n"
                                    (:err result))}))
    (str (:out result))))

(impl DefaultSessionGuard SessionGuardOps fail-for
  {:message :string} :any
  [this input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(impl DefaultSessionGuard SessionGuardOps pass-for
  {:message :string} :any
  [this input]
  (println (:message input)))

(impl DefaultSessionGuard SessionGuardOps classify-prefix-for
  {:value :string} :string
  [this input]
  (let [value (:value input)]
    (cond
      (str/starts-with? value "session:") "session"
      (str/starts-with? value "intent:") "intent"
      :else "other")))

(impl DefaultSessionGuard SessionGuardOps decide-for DecideInput :map
  [this input]
  (let [descriptions (:descriptions input)]
    (loop [remaining descriptions
           intent-count 0]
      (if (empty? remaining)
        (if (pos? intent-count)
          {:status :fail
           :message (str "Session guard failed: found " intent-count
                         " trailing intent commits without a session aggregating commit.\n"
                         "Create a final commit with: jj commit -m \"session: <result summary>\"")}
          {:status :pass
           :message "Session guard passed: no trailing intent commits detected."})
        (let [line (first remaining)
              kind (classify-prefix-for this {:value line})]
          (cond
            (= kind "session")
            (if (pos? intent-count)
              {:status :fail
               :message (str "Session guard failed: found " intent-count
                             " trailing intent commits above the latest session commit.\n"
                             "Create a final commit with: jj commit -m \"session: <result summary>\"")}
              {:status :pass
               :message "Session guard passed: latest session aggregation commit is present."})

            (= kind "intent")
            (recur (rest remaining) (inc intent-count))

            :else
            (recur (rest remaining) intent-count)))))))

(impl DefaultSessionGuard SessionGuardOps validate-session-message-for ValidateSessionMessageInput :map
  [this input]
  (let [description (:description input)
        lines (->> (str/split-lines description)
                   (map str/trim)
                   (remove str/blank?)
                   vec)
        head-line (or (first lines) "")
        required-sections ["## Original Prompt"
                           "## Agent Context"
                           "## Logical Changes"]
        missing (->> required-sections
                     (remove #(str/includes? description %))
                     vec)]
    (cond
      (not (str/starts-with? head-line "session:"))
      {:status :fail
       :message "Session guard failed: HEAD commit is not a session commit."}

      (seq missing)
      {:status :fail
       :message (str "Session guard failed: session commit missing required sections: "
                     (str/join ", " missing))}

      :else
      {:status :pass
       :message "Session guard passed: HEAD session commit contains required context sections."})))

(impl DefaultSessionGuard SessionGuardOps validate-push-for :map :map
  [this input]
  (let [local (run-command-for this {:args ["git" "rev-parse" "HEAD"]})
        remote (run-command-for this {:args ["git" "ls-remote" "--heads" "origin" "dev"]})]
    (when-not (= 0 (:exit local))
      (fail-for this {:message (str "Session guard failed: unable to resolve local HEAD.\n" (:err local))}))
    (when-not (= 0 (:exit remote))
      (fail-for this {:message (str "Session guard failed: unable to resolve origin/dev.\n" (:err remote))}))
    (let [local-hash (str/trim (:out local))
          remote-line (str/trim (:out remote))
          remote-hash (first (str/split remote-line #"\s+"))]
      (if (= local-hash remote-hash)
        {:status :pass
         :message "Session guard passed: HEAD is pushed to origin/dev."}
        {:status :fail
         :message (str "Session guard failed: HEAD is not pushed to origin/dev.\n"
                       "local HEAD: " local-hash "\n"
                       "origin/dev: " (or remote-hash "<missing>"))}))))

(def default-session-guard (->DefaultSessionGuard))

(main Input
  [_]
  (let [log-result (run-command-for default-session-guard {:args ["jj" "log" "-r" "::@-" "--no-graph" "-T" "description.first_line() ++ \"\\n\""]})]
    (when-not (= 0 (:exit log-result))
      (fail-for default-session-guard {:message (str "Session guard failed: unable to read jj log.\n" (:err log-result))}))
    (let [descriptions (parse-descriptions-for default-session-guard {:raw (:out log-result)})
          verdict (decide-for default-session-guard {:descriptions descriptions})]
      (if (not= :pass (:status verdict))
        (fail-for default-session-guard {:message (:message verdict)})
        (let [desc-verdict (validate-session-message-for default-session-guard
                                                         {:description (read-head-description-for default-session-guard {})})]
          (if (not= :pass (:status desc-verdict))
            (fail-for default-session-guard {:message (:message desc-verdict)})
            (let [push-verdict (validate-push-for default-session-guard {})]
              (if (= :pass (:status push-verdict))
                (pass-for default-session-guard {:message (str (:message verdict)
                                                               "\n" (:message desc-verdict)
                                                               "\n" (:message push-verdict))})
                (fail-for default-session-guard {:message (:message push-verdict)})))))))))

(-main {:args (vec *command-line-args*)})
