#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {cheshire/cheshire {:mvn/version "5.13.0"}}})

(require '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json]
         '[clojure.string :as str])

;; Tool Stack Transparency:
;; Runtime: Babashka (Clojure)
;; Rationale: Fast orchestration of nix-search and cargo-search for tool discovery.

(defn search-nix [query]
  (println (str "Searching nixpkgs for: " query))
  (let [{:keys [exit out err]} (sh "nix" "search" "nixpkgs" query "--json")]
    (if (zero? exit)
      (try 
        (let [results (json/parse-string out)]
          (take 5 (keys results)))
        (catch Exception _ []))
      (do (println "Nix search failed:" err) []))))

(defn search-cargo [query]
  (println (str "Searching crates.io for: " query))
  (let [{:keys [exit out err]} (sh "cargo" "search" query "--limit" "5")]
    (if (zero? exit)
      (->> (str/split-lines out)
           (map #(first (str/split % #" = ")))
           (filter seq))
      (do (println "Cargo search failed:" err) []))))

(defn discover-tools [subject]
  (let [nix-results (search-nix subject)
        cargo-results (search-cargo subject)]
    {:subject subject
     :timestamp (java.time.Instant/now)
     :recommendations {:nix nix-results
                       :cargo cargo-results}}))

(let [subject (first *command-line-args*)]
  (if (str/blank? subject)
    (println "Usage: tool_discoverer.clj <Subject>")
    (println (json/generate-string (discover-tools subject) {:pretty true}))))
