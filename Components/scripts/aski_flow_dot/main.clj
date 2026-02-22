#!/usr/bin/env bb

(ns mentci.aski-flow-dot
  (:require [babashka.deps :as deps]))

(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.string :as str]
         '[clojure.java.io :as io]
         '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/types.clj"))
(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

(enable!)

(def FlowInput
  [:map
   [:flow [:vector :any]]
   [:graphId {:optional true} :string]])

(def GraphvizJsonInput
  [:map
   [:graph [:map-of :string :any]]])

(def DotInput
  [:map
   [:dot :string]])

(def Input
  [:map
   [:args [:vector :string]]])

(defprotocol AskiFlowOps
  (to-id-for [this input])
  (to-attr-map-for [this input])
  (node-form-for [this input])
  (node-from-item-for [this input])
  (escape-dot-for [this input])
  (attrs->dot-fragment-for [this input])
  (aski-flow->graphviz-json-for [this input])
  (graphviz-json->dot-for [this input])
  (has-dot-for [this])
  (dot->graphviz-json-for [this input])
  (aski-flow->dot-for [this input])
  (run-flow-cli-for [this input]))

(defrecord DefaultAskiFlow [])

(impl DefaultAskiFlow AskiFlowOps to-id-for {:value [:or :symbol :keyword :string]} :string
  [this input]
  (let [_ this
        value (:value input)]
    (cond
      (symbol? value) (name value)
      (keyword? value) (name value)
      :else (str value))))

(impl DefaultAskiFlow AskiFlowOps to-attr-map-for {:attrs [:maybe :map]} [:map-of :string :string]
  [this input]
  (let [attrs (:attrs input)]
    (if (map? attrs)
      (into {} (map (fn [[k v]] [(to-id-for this {:value k}) (str v)])) attrs)
      {})))

(impl DefaultAskiFlow AskiFlowOps node-form-for {:item :any} :boolean
  [this input]
  (let [_ this
        item (:item input)]
    (or (symbol? item)
        (and (seq? item) (symbol? (first item))))))

(impl DefaultAskiFlow AskiFlowOps node-from-item-for {:item :any} [:maybe [:map [:id :string] [:attrs [:map-of :string :string]]]]
  [this input]
  (let [item (:item input)]
    (cond
      (symbol? item) {:id (to-id-for this {:value item}) :attrs {}}
      (and (seq? item) (symbol? (first item)))
      {:id (to-id-for this {:value (first item)})
       :attrs (to-attr-map-for this {:attrs (second item)})}
      :else nil)))

(impl DefaultAskiFlow AskiFlowOps escape-dot-for {:value :string} :string
  [this input]
  (str/escape (:value input) {\" "\\\"" \\ "\\\\"}))

(impl DefaultAskiFlow AskiFlowOps attrs->dot-fragment-for {:attrs [:map-of :string :string]} :string
  [this input]
  (let [attrs (:attrs input)]
    (if (empty? attrs)
      ""
      (str " ["
           (str/join ", "
                     (map (fn [[k v]]
                            (str k "=\"" (escape-dot-for this {:value v}) "\""))
                          attrs))
           "]"))))

(impl DefaultAskiFlow AskiFlowOps aski-flow->graphviz-json-for FlowInput [:map-of :string :any]
  [this input]
  (let [flow (:flow input)
        graph-id (or (:graphId input) "AskiFlow")
        elements (vec flow)
        node-order (->> elements (map #(node-from-item-for this {:item %})) (remove nil?) vec)
        node-ids (mapv :id node-order)
        id->idx (into {} (map-indexed (fn [idx id] [id idx]) node-ids))
        objects (mapv (fn [idx {:keys [id attrs]}]
                        (into {"_gvid" idx "name" id} attrs))
                      (range (count node-order))
                      node-order)
        edges
        (loop [i 0
               node-idx 0
               acc []]
          (if (>= i (count elements))
            (mapv (fn [gvid e] (assoc e "_gvid" gvid)) (range (count acc)) acc)
            (let [item (nth elements i)]
              (if-not (node-form-for this {:item item})
                (recur (inc i) node-idx acc)
                (let [current-id (nth node-ids node-idx)
                      current-gvid (id->idx current-id)
                      next-id (nth node-ids (inc node-idx) nil)
                      next-gvid (some-> next-id id->idx)
                      maybe-map (nth elements (inc i) nil)]
                  (if (map? maybe-map)
                    (let [{:keys [edges has-next]}
                          (reduce
                           (fn [state [condition target]]
                             (let [condition-name (to-id-for this {:value condition})
                                   target-id (if (= target :next) next-id (to-id-for this {:value target}))
                                   target-gvid (some-> target-id id->idx)]
                               (when (nil? target-gvid)
                                 (throw (ex-info "Aski-Flow route target is undefined in node set."
                                                 {:condition condition-name
                                                  :target target-id
                                                  :node current-id})))
                               {:edges (conj (:edges state)
                                             {"tail" current-gvid
                                              "head" target-gvid
                                              "label" condition-name
                                              "condition" condition-name})
                                :has-next (or (:has-next state) (= target :next))}))
                           {:edges acc :has-next false}
                           maybe-map)
                          acc* (if (and next-gvid (not has-next))
                                 (conj edges {"tail" current-gvid "head" next-gvid})
                                 edges)]
                      (recur (+ i 2) (inc node-idx) acc*))
                    (let [acc* (if next-gvid
                                 (conj acc {"tail" current-gvid "head" next-gvid})
                                 acc)]
                      (recur (inc i) (inc node-idx) acc*))))))))]
    {"name" graph-id
     "directed" true
     "strict" false
     "objects" objects
     "edges" edges}))

(impl DefaultAskiFlow AskiFlowOps graphviz-json->dot-for GraphvizJsonInput :string
  [this input]
  (let [graph (:graph input)
        graph-id (get graph "name" "AskiFlow")
        objects (vec (get graph "objects" []))
        edges (vec (get graph "edges" []))
        object-name-by-gvid (into {} (map (fn [o] [(get o "_gvid") (get o "name")]) objects))
        node-lines
        (map (fn [o]
               (let [node-id (get o "name")
                     attrs (-> o (dissoc "_gvid" "name")
                               (update-keys str)
                               (update-vals str))]
                 (str "  " node-id (attrs->dot-fragment-for this {:attrs attrs}) ";")))
             objects)
        edge-lines
        (map (fn [e]
               (let [tail-id (get object-name-by-gvid (get e "tail"))
                     head-id (get object-name-by-gvid (get e "head"))
                     attrs (-> e (dissoc "_gvid" "tail" "head")
                               (update-keys str)
                               (update-vals str))]
                 (when (or (nil? tail-id) (nil? head-id))
                   (throw (ex-info "Graphviz JSON edge references unknown node index."
                                   {:edge e})))
                 (str "  " tail-id " -> " head-id (attrs->dot-fragment-for this {:attrs attrs}) ";")))
             edges)]
    (str "digraph \"" graph-id "\" {\n"
         (str/join "\n" (concat node-lines edge-lines))
         "\n}\n")))

(impl DefaultAskiFlow AskiFlowOps has-dot-for [:=> [:cat :any] :boolean]
  [this]
  (zero? (:exit (sh "sh" "-lc" "command -v dot >/dev/null 2>&1"))))

(impl DefaultAskiFlow AskiFlowOps dot->graphviz-json-for DotInput [:map-of :string :any]
  [this input]
  (when-not (has-dot-for this)
    (throw (ex-info "Graphviz dot executable not found; cannot run dot -Tjson."
                    {:command "dot -Tjson"})))
  (let [tmp-dot (java.io.File/createTempFile "mentci-graph" ".dot")
        tmp-json (java.io.File/createTempFile "mentci-graph" ".json")
        _ (spit tmp-dot (:dot input))
        result (sh "dot" "-Tjson" (.getAbsolutePath tmp-dot) "-o" (.getAbsolutePath tmp-json))]
    (try
      (when-not (zero? (:exit result))
        (throw (ex-info "dot -Tjson failed." {:stderr (:err result) :stdout (:out result)})))
      (json/parse-string (slurp tmp-json))
      (finally
        (.delete tmp-dot)
        (.delete tmp-json)))))

(impl DefaultAskiFlow AskiFlowOps aski-flow->dot-for FlowInput :string
  [this input]
  (graphviz-json->dot-for this {:graph (aski-flow->graphviz-json-for this input)}))

(def default-aski-flow (->DefaultAskiFlow))

(defmacro aski-flow->dot-macro [flow]
  `(aski-flow->dot-for default-aski-flow {:flow ~flow :graphId "AskiFlow"}))

(impl DefaultAskiFlow AskiFlowOps run-flow-cli-for Input :any
  [this input]
  (let [args (:args input)
        flow-file (first args)
        graph-id (second args)]
    (if-not flow-file
      (binding [*out* *err*]
        (println "Usage: bb Components/scripts/aski_flow_dot/main.clj <flow-file> [graph-id]")
        (System/exit 1))
      (let [flow (read-string (slurp flow-file))
            dot (aski-flow->dot-for this {:flow flow :graphId graph-id})]
        (println dot)))))

(main Input
  [input]
  (run-flow-cli-for default-aski-flow input))

(when (= *file* (System/getProperty "babashka.file"))
  (-main {:args (vec *command-line-args*)}))
