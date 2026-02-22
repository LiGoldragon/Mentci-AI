#!/usr/bin/env bb

(ns mentci.malli
  (:require [malli.core :as m]
            [malli.instrument :as mi]
            [malli.experimental.lite :as ml]))

(defmacro defn* [name schema args & body]
  `(do
     (m/=> ~name ~schema)
     (defn ~name ~args ~@body)))

(defmacro main
  "Concise entrypoint macro.
   Arity 1 (common): (main InputSchemaOrLite [args] ...)
   Arity 2 (advanced): (main [:=> ...] [args] ...)"
  [schema-or-input args & body]
  (let [is-function-schema? (and (vector? schema-or-input)
                                 (= :=> (first schema-or-input)))
        input-schema (if (symbol? schema-or-input)
                       schema-or-input
                       `(ml/schema ~schema-or-input))
        schema (if is-function-schema?
                 schema-or-input
                 `[:=> [:cat ~input-schema] :any])]
    `(defn* -main ~schema ~args ~@body)))

(defn enable! []
  (mi/instrument!))
