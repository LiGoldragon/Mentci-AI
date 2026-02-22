#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def MainInput
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

(defn* run-command [:=> [:cat CommandInput] :map] [input]
  (let [result (apply sh (:args input))]
    {:exit (:exit result)
     :out (:out result)
     :err (:err result)}))

(defn* parse-descriptions [:=> [:cat ParseInput] [:vector :string]] [input]
  (->> (str/split-lines (:raw input))
       (map str/trim)
       (remove str/blank?)
       vec))

(defn* fail! [:=> [:cat [:map [:message :string]]] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* pass! [:=> [:cat [:map [:message :string]]] :any] [input]
  (println (:message input)))

(defn* classify-prefix [:=> [:cat [:map [:value :string]]] :string] [input]
  (let [value (:value input)]
    (cond
      (str/starts-with? value "session:") "session"
      (str/starts-with? value "intent:") "intent"
      :else "other")))

(defn* decide [:=> [:cat DecideInput] :map] [input]
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
              kind (classify-prefix {:value line})]
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

(defn* -main [:=> [:cat MainInput] :any] [_]
  (let [log-result (run-command {:args ["jj" "log" "-r" "::@-" "--no-graph" "-T" "description.first_line() ++ \"\\n\""]})]
    (when-not (= 0 (:exit log-result))
      (fail! {:message (str "Session guard failed: unable to read jj log.\n" (:err log-result))}))
    (let [descriptions (parse-descriptions {:raw (:out log-result)})
          verdict (decide {:descriptions descriptions})]
      (if (= :pass (:status verdict))
        (pass! {:message (:message verdict)})
        (fail! {:message (:message verdict)})))))

(-main {:args (vec *command-line-args*)})
