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

(defprotocol SessionGuardOps
  (run-command-for [this input])
  (parse-descriptions-for [this input])
  (fail-for [this input])
  (pass-for [this input])
  (classify-prefix-for [this input])
  (decide-for [this input]))

(defrecord DefaultSessionGuard [])

(impl DefaultSessionGuard SessionGuardOps run-command-for
  [:=> [:cat :any CommandInput] :map]
  [this input]
  (let [result (apply sh (:args input))]
    {:exit (:exit result)
     :out (:out result)
     :err (:err result)}))

(impl DefaultSessionGuard SessionGuardOps parse-descriptions-for
  [:=> [:cat :any ParseInput] [:vector :string]]
  [this input]
  (->> (str/split-lines (:raw input))
       (map str/trim)
       (remove str/blank?)
       vec))

(impl DefaultSessionGuard SessionGuardOps fail-for
  [:=> [:cat :any [:map [:message :string]]] :any]
  [this input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(impl DefaultSessionGuard SessionGuardOps pass-for
  [:=> [:cat :any [:map [:message :string]]] :any]
  [this input]
  (println (:message input)))

(impl DefaultSessionGuard SessionGuardOps classify-prefix-for
  [:=> [:cat :any [:map [:value :string]]] :string]
  [this input]
  (let [value (:value input)]
    (cond
      (str/starts-with? value "session:") "session"
      (str/starts-with? value "intent:") "intent"
      :else "other")))

(impl DefaultSessionGuard SessionGuardOps decide-for
  [:=> [:cat :any DecideInput] :map]
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

(def default-session-guard (->DefaultSessionGuard))

(main Input
  [_]
  (let [log-result (run-command-for default-session-guard {:args ["jj" "log" "-r" "::@-" "--no-graph" "-T" "description.first_line() ++ \"\\n\""]})]
    (when-not (= 0 (:exit log-result))
      (fail-for default-session-guard {:message (str "Session guard failed: unable to read jj log.\n" (:err log-result))}))
    (let [descriptions (parse-descriptions-for default-session-guard {:raw (:out log-result)})
          verdict (decide-for default-session-guard {:descriptions descriptions})]
      (if (= :pass (:status verdict))
        (pass-for default-session-guard {:message (:message verdict)})
        (fail-for default-session-guard {:message (:message verdict)})))))

(-main {:args (vec *command-line-args*)})
