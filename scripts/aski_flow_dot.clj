#!/usr/bin/env bb

(ns mentci.aski-flow-dot
  (:require [babashka.deps :as deps]))

(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (io/file *file*)) "/types.clj"))
(load-file (str (.getParent (io/file *file*)) "/malli.clj"))
(require '[mentci.malli :refer [defn* enable!]])

(enable!)

(def FlowToDotInput
  [:map
   [:flow [:vector :any]]
   [:graphId {:optional true} :string]])

(def DotAttrInput
  [:map
   [:attrs [:map-of :string :string]]])

(def EmitNodeInput
  [:map
   [:nodeId :string]
   [:attrs [:map-of :string :string]]])

(def EmitEdgeInput
  [:map
   [:from :string]
   [:to :string]
   [:attrs [:map-of :string :string]]])

(defn* to-id [:=> [:cat [:or :symbol :keyword :string]] :string] [value]
  (cond
    (symbol? value) (name value)
    (keyword? value) (name value)
    :else (str value)))

(defn* to-attr-map [:=> [:cat [:maybe :map]] [:map-of :string :string]] [attrs]
  (if (map? attrs)
    (into {}
          (map (fn [[k v]] [(to-id k) (str v)]))
          attrs)
    {}))

(defn* node-form? [:=> [:cat :any] :boolean] [item]
  (or (symbol? item)
      (and (seq? item) (symbol? (first item)))))

(defn* node-from-item [:=> [:cat :any] [:maybe [:map [:id :string] [:attrs [:map-of :string :string]]]]] [item]
  (cond
    (symbol? item)
    {:id (to-id item) :attrs {}}

    (and (seq? item) (symbol? (first item)))
    {:id (to-id (first item))
     :attrs (to-attr-map (second item))}

    :else nil))

(defn* attrs->dot [:=> [:cat DotAttrInput] :string] [input]
  (let [attrs (:attrs input)]
    (if (empty? attrs)
      ""
      (str " ["
           (str/join ", "
                     (map (fn [[k v]]
                            (str k "=\"" (str/escape v {\" "\\\""}) "\""))
                          attrs))
           "]"))))

(defn* emit-node-line [:=> [:cat EmitNodeInput] :string] [input]
  (str "  " (:nodeId input) (attrs->dot {:attrs (:attrs input)}) ";"))

(defn* emit-edge-line [:=> [:cat EmitEdgeInput] :string] [input]
  (str "  " (:from input) " -> " (:to input) (attrs->dot {:attrs (:attrs input)}) ";"))

(defn* flow->graph [:=> [:cat FlowToDotInput] :map] [input]
  (let [flow (:flow input)
        graph-id (or (:graphId input) "AskiFlow")
        elements (vec flow)
        node-order (->> elements (map node-from-item) (remove nil?) vec)
        node-ids (mapv :id node-order)
        nodes (reduce (fn [acc node] (assoc acc (:id node) (:attrs node))) {} node-order)]
    (loop [i 0
           node-idx 0
           edges []]
      (if (>= i (count elements))
        {:graphId graph-id :nodes nodes :edges edges}
        (let [item (nth elements i)]
          (if-not (node-form? item)
            (recur (inc i) node-idx edges)
            (let [current-id (nth node-ids node-idx)
                  next-id (nth node-ids (inc node-idx) nil)
                  maybe-map (nth elements (inc i) nil)]
              (if (map? maybe-map)
                (let [{:keys [edges has-next]}
                      (reduce
                       (fn [acc [condition target]]
                         (let [condition-name (to-id condition)
                               target-id (cond
                                           (= target :next) next-id
                                           (or (symbol? target) (keyword? target) (string? target)) (to-id target)
                                           :else (throw (ex-info "Unsupported Aski-Flow map target for DOT conversion."
                                                                 {:target target :condition condition})))
                               edge-attrs {"label" condition-name
                                           "condition" condition-name}]
                           (when (nil? target-id)
                             (throw (ex-info "Route target :next requires a next node."
                                             {:condition condition-name :node current-id})))
                           {:edges (conj (:edges acc) {:from current-id :to target-id :attrs edge-attrs})
                            :has-next (or (:has-next acc) (= target :next))}))
                       {:edges edges :has-next false}
                       maybe-map)
                      edges* (if (and next-id (not has-next))
                               (conj edges {:from current-id :to next-id :attrs {}})
                               edges)]
                  (recur (+ i 2) (inc node-idx) edges*))
                (let [edges* (if next-id
                               (conj edges {:from current-id :to next-id :attrs {}})
                               edges)]
                  (recur (inc i) (inc node-idx) edges*))))))))))

(defn* flow->dot [:=> [:cat FlowToDotInput] :string] [input]
  (let [{:keys [graphId nodes edges]} (flow->graph input)
        node-lines (->> nodes
                        (map (fn [[node-id attrs]] (emit-node-line {:nodeId node-id :attrs attrs})))
                        sort)
        edge-lines (->> edges
                        (map (fn [{:keys [from to attrs]}] (emit-edge-line {:from from :to to :attrs attrs}))))
        body (concat node-lines edge-lines)]
    (str "digraph \"" graphId "\" {\n"
         (str/join "\n" body)
         "\n}\n")))

(defmacro aski-flow->dot [flow]
  (flow->dot {:flow flow :graphId "AskiFlow"}))

(defn* -main [:=> [:cat [:map [:args [:vector :string]]]] :any] [input]
  (let [args (:args input)
        flow-file (first args)
        graph-id (second args)]
    (if-not flow-file
      (binding [*out* *err*]
        (println "Usage: bb scripts/aski_flow_dot.clj <flow-file> [graph-id]")
        (System/exit 1))
      (let [flow (read-string (slurp flow-file))]
        (println (flow->dot {:flow flow :graphId graph-id})))))) 

(when (= *file* (System/getProperty "babashka.file"))
  (-main {:args (vec *command-line-args*)}))
