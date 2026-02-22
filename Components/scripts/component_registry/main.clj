#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[cheshire.core :as json])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def ComponentEntry
  [:map
   [:id :string]
   [:path :string]
   [:interfaces [:vector :string]]
   [:status [:enum :active :deprecated :experimental]]])

(def ComponentIndex
  [:map
   [:version :int]
   [:components [:vector ComponentEntry]]])

(def ParseArgsInput
  [:map
   [:args [:vector :string]]])

(def LoadIndexInput
  [:map
   [:indexPath :string]])

(def ResolveInput
  [:map
   [:index ComponentIndex]])

(def LookupInput
  [:map
   [:resolved :map]
   [:componentId :string]])

(def MainInput
  [:map
   [:args [:vector :string]]])

(defn* fail! [:=> [:cat [:map [:message :string]]] :any] [input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(defn* parse-args [:=> [:cat ParseArgsInput] :map] [input]
  (loop [remaining (:args input)
         options {:indexPath "Components/index.edn"
                  :format "edn"
                  :componentId nil}]
    (if (empty? remaining)
      options
      (let [[arg & _] remaining]
        (cond
          (= arg "--index-path")
          (recur (nnext remaining) (assoc options :indexPath (second remaining)))

          (= arg "--format")
          (recur (nnext remaining) (assoc options :format (second remaining)))

          (= arg "--id")
          (recur (nnext remaining) (assoc options :componentId (second remaining)))

          (= arg "--help")
          (do
            (println "Usage: bb Components/scripts/component_registry/main.clj [--index-path <path>] [--format <edn|json>] [--id <component-id>]")
            (System/exit 0))

          :else
          (fail! {:message (str "Unknown argument: " arg)}))))))

(defn* load-index [:=> [:cat LoadIndexInput] ComponentIndex] [input]
  (let [f (io/file (:indexPath input))]
    (when-not (.exists f)
      (fail! {:message (str "Component index file not found: " (:indexPath input))}))
    (let [parsed (edn/read-string (slurp f))]
      parsed)))

(defn* resolve-index [:=> [:cat ResolveInput] :map] [input]
  (let [components (:components (:index input))
        ids (map :id components)
        dup-ids (->> ids frequencies (filter (fn [[_ c]] (> c 1))) (map first) vec)]
    (when (seq dup-ids)
      (fail! {:message (str "Duplicate component ids in index: " (str/join ", " dup-ids))}))
    (let [resolved
          (reduce (fn [acc c]
                    (let [p (:path c)
                          f (io/file p)]
                      (when-not (.exists f)
                        (fail! {:message (str "Component path does not exist: " p)}))
                      (assoc acc (:id c)
                             {:id (:id c)
                              :path p
                              :interfaces (:interfaces c)
                              :status (:status c)})))
                  {}
                  components)]
      {:version (:version (:index input))
       :components resolved})))

(defn* lookup-component [:=> [:cat LookupInput] :map] [input]
  (let [resolved (:resolved input)
        component (get-in resolved [:components (:componentId input)])]
    (when-not component
      (fail! {:message (str "Unknown component id: " (:componentId input))}))
    component))

(defn* emit [:=> [:cat [:map [:value :any] [:format :string]]] :any] [input]
  (let [{:keys [value format]} input]
    (case format
      "json" (println (json/generate-string value {:pretty true}))
      (prn value))))

(defn* -main [:=> [:cat MainInput] :any] [input]
  (let [{:keys [indexPath format componentId]} (parse-args {:args (:args input)})
        index (load-index {:indexPath indexPath})
        resolved (resolve-index {:index index})]
    (if componentId
      (emit {:value (lookup-component {:resolved resolved :componentId componentId})
             :format format})
      (emit {:value resolved :format format}))))

(-main {:args (vec *command-line-args*)})
