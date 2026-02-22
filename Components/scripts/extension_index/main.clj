#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.edn :as edn]
         '[clojure.string :as str])

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [impl main enable!]])

(enable!)

(def Input
  [:map
   [:args [:vector :string]]])

(def LocationConfig
  [:map
   [:version :int]
   [:locations [:vector :string]]])

(defprotocol ExtensionIndexOps
  (fail-for [this input])
  (load-location-config-for [this])
  (parse-args-for [this input])
  (find-existing-for [this input])
  (emit-for [this input])
  (run-for [this input]))

(defrecord DefaultExtensionIndex [])

(impl DefaultExtensionIndex ExtensionIndexOps fail-for {:message :string} :any
  [this input]
  (let [_ this]
    (binding [*out* *err*]
      (println (:message input)))
    (System/exit 1)))

(impl DefaultExtensionIndex ExtensionIndexOps load-location-config-for [:=> [:cat :any] LocationConfig]
  [this]
  (let [path "Core/EXTENSION_INDEX_LOCATIONS.edn"
        f (io/file path)]
    (when-not (.exists f)
      (fail-for this {:message (str "Missing extension index location contract: " path)}))
    (edn/read-string (slurp f))))

(impl DefaultExtensionIndex ExtensionIndexOps parse-args-for Input :map
  [this input]
  (let [_ this
        args (:args input)]
    {:format (if (some #{"--plain"} args) "plain" "edn")}))

(impl DefaultExtensionIndex ExtensionIndexOps find-existing-for {:locations [:vector :string]} [:vector :string]
  [this input]
  (let [_ this]
    (->> (:locations input)
         (filter #(.exists (io/file %)))
         vec)))

(impl DefaultExtensionIndex ExtensionIndexOps emit-for {:format :string :payload :map} :any
  [this input]
  (let [_ this
        {:keys [format payload]} input]
    (if (= format "plain")
      (doseq [path (:found payload)]
        (println path))
      (prn payload))))

(impl DefaultExtensionIndex ExtensionIndexOps run-for Input :any
  [this input]
  (let [{:keys [format]} (parse-args-for this input)
        {:keys [locations]} (load-location-config-for this)
        found (find-existing-for this {:locations locations})]
    (emit-for this {:format format
                    :payload {:locations locations
                              :found found}})))

(def default-extension-index (->DefaultExtensionIndex))

(main Input
  [input]
  (run-for default-extension-index input))

(-main {:args (vec *command-line-args*)})
