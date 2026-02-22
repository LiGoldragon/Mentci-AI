#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[cheshire.core :as json])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

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

(def Input
  [:map
   [:args [:vector :string]]])

(defprotocol ComponentRegistryOps
  (fail-for [this input])
  (parse-args-for [this input])
  (load-index-for [this input])
  (resolve-index-for [this input])
  (lookup-component-for [this input])
  (emit-for [this input])
  (run-registry-for [this input]))

(defrecord DefaultComponentRegistry [])

(impl DefaultComponentRegistry ComponentRegistryOps fail-for {:message :string} :any
  [this input]
  (binding [*out* *err*]
    (println (:message input)))
  (System/exit 1))

(impl DefaultComponentRegistry ComponentRegistryOps parse-args-for ParseArgsInput :map
  [this input]
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
          (fail-for this {:message (str "Unknown argument: " arg)}))))))

(impl DefaultComponentRegistry ComponentRegistryOps load-index-for LoadIndexInput ComponentIndex
  [this input]
  (let [f (io/file (:indexPath input))]
    (when-not (.exists f)
      (fail-for this {:message (str "Component index file not found: " (:indexPath input))}))
    (edn/read-string (slurp f))))

(impl DefaultComponentRegistry ComponentRegistryOps resolve-index-for ResolveInput :map
  [this input]
  (let [components (:components (:index input))
        ids (map :id components)
        dup-ids (->> ids frequencies (filter (fn [[_ c]] (> c 1))) (map first) vec)]
    (when (seq dup-ids)
      (fail-for this {:message (str "Duplicate component ids in index: " (str/join ", " dup-ids))}))
    (let [resolved
          (reduce (fn [acc c]
                    (let [p (:path c)
                          f (io/file p)]
                      (when-not (.exists f)
                        (fail-for this {:message (str "Component path does not exist: " p)}))
                      (assoc acc (:id c)
                             {:id (:id c)
                              :path p
                              :interfaces (:interfaces c)
                              :status (:status c)})))
                  {}
                  components)]
      {:version (:version (:index input))
       :components resolved})))

(impl DefaultComponentRegistry ComponentRegistryOps lookup-component-for LookupInput :map
  [this input]
  (let [resolved (:resolved input)
        component (get-in resolved [:components (:componentId input)])]
    (when-not component
      (fail-for this {:message (str "Unknown component id: " (:componentId input))}))
    component))

(impl DefaultComponentRegistry ComponentRegistryOps emit-for {:value :any :format :string} :any
  [this input]
  (let [{:keys [value format]} input]
    (case format
      "json" (println (json/generate-string value {:pretty true}))
      (prn value))))

(impl DefaultComponentRegistry ComponentRegistryOps run-registry-for Input :any
  [this input]
  (let [{:keys [indexPath format componentId]} (parse-args-for this {:args (:args input)})
        index (load-index-for this {:indexPath indexPath})
        resolved (resolve-index-for this {:index index})]
    (if componentId
      (emit-for this {:value (lookup-component-for this {:resolved resolved :componentId componentId})
                      :format format})
      (emit-for this {:value resolved :format format}))))

(def default-component-registry (->DefaultComponentRegistry))

(main Input
  [input]
  (run-registry-for default-component-registry input))

(-main {:args (vec *command-line-args*)})
