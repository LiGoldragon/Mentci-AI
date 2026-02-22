#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :refer [sh]])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def RunCommandInput
  [:map
   [:args [:vector :string]]])

(def OutputInput
  [:map
   [:solar :string]])

(defprotocol SolarPrefixOps
  (run-command-for [this input])
  (fail-for [this input])
  (read-solar-for [this])
  (emit-solar-prefix-for [this input]))

(defrecord DefaultSolarPrefix [])

(impl DefaultSolarPrefix SolarPrefixOps run-command-for RunCommandInput :map
  [this input]
  (let [_ this]
    (try
      (let [result (apply sh (:args input))]
        {:exit (:exit result)
         :out (:out result)
         :err (:err result)})
      (catch java.io.IOException e
        {:exit 127
         :out ""
         :err (.getMessage e)}))))

(impl DefaultSolarPrefix SolarPrefixOps fail-for {:message :string} :any
  [this input]
  (let [_ this]
    (binding [*out* *err*]
      (println (:message input)))
    (System/exit 1)))

(impl DefaultSolarPrefix SolarPrefixOps read-solar-for [:=> [:cat :any] :string]
  [this]
  (let [attempts [["chronos" "--format" "am" "--precision" "second"]
                  ["cargo" "run" "--quiet" "--manifest-path" "Components/Cargo.toml" "--bin" "chronos" "--" "--format" "am" "--precision" "second"]]
        results (map #(run-command-for this {:args %}) attempts)
        success (first (filter #(zero? (:exit %)) results))]
    (when-not success
      (fail-for this {:message (str "Failed to resolve solar prefix.\n"
                                    (str/join "\n"
                                              (map (fn [result cmd]
                                                     (str "- " (str/join " " cmd)
                                                          " (exit " (:exit result) ")"
                                                          (when-not (str/blank? (str (:err result)))
                                                            (str ": " (str/trim (:err result))))))
                                                   results
                                                   attempts)))}))
    (str/trim (:out success))))

(impl DefaultSolarPrefix SolarPrefixOps emit-solar-prefix-for OutputInput :any
  [this input]
  (let [_ this]
    (println (str "solar: " (:solar input)))
    (println "")))

(def default-solar-prefix (->DefaultSolarPrefix))

(main Input
  [_]
  (emit-solar-prefix-for default-solar-prefix {:solar (read-solar-for default-solar-prefix)}))

(-main {:args (vec *command-line-args*)})
