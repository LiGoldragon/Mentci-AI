#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}
                        cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def QueryInput
  [:map
   [:query :string]])

(def DiscoverInput
  [:map
   [:subject :string]])

(defprotocol ToolDiscovererOps
  (search-nix-for [this input])
  (search-cargo-for [this input])
  (discover-tools-for [this input]))

(defrecord DefaultToolDiscoverer [])

(impl DefaultToolDiscoverer ToolDiscovererOps search-nix-for
  [:=> [:cat :any QueryInput] [:vector :string]]
  [this input]
  (let [query (:query input)]
    (println (str "Searching nixpkgs for: " query))
    (let [{:keys [exit out err]} (sh "nix" "search" "nixpkgs" query "--json")]
      (if (zero? exit)
        (try
          (let [results (json/parse-string out)]
            (vec (take 5 (keys results))))
          (catch Exception _ []))
        (do
          (println "Nix search failed:" err)
          [])))))

(impl DefaultToolDiscoverer ToolDiscovererOps search-cargo-for
  [:=> [:cat :any QueryInput] [:vector :string]]
  [this input]
  (let [query (:query input)]
    (println (str "Searching crates.io for: " query))
    (let [{:keys [exit out err]} (sh "cargo" "search" query "--limit" "5")]
      (if (zero? exit)
        (->> (str/split-lines out)
             (map #(first (str/split % #" = ")))
             (filter seq)
             vec)
        (do
          (println "Cargo search failed:" err)
          [])))))

(impl DefaultToolDiscoverer ToolDiscovererOps discover-tools-for
  [:=> [:cat :any DiscoverInput] :map]
  [this input]
  (let [subject (:subject input)
        nix-results (search-nix-for this {:query subject})
        cargo-results (search-cargo-for this {:query subject})]
    {:subject subject
     :timestamp (str (java.time.Instant/now))
     :recommendations {:nix nix-results
                       :cargo cargo-results}}))

(def default-tool-discoverer (->DefaultToolDiscoverer))

(main Input
  [input]
  (let [subject (first (:args input))]
    (if (str/blank? subject)
      (println "Usage: tool_discoverer.clj <Subject>")
      (println (json/generate-string
                (discover-tools-for default-tool-discoverer {:subject subject})
                {:pretty true})))))

(-main {:args (vec *command-line-args*)})
