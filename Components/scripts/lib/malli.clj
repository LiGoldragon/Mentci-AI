#!/usr/bin/env bb

(ns mentci.malli
  (:require [malli.core :as m]
            [malli.instrument :as mi]))

(defmacro defn* [name schema args & body]
  `(do
     (m/=> ~name ~schema)
     (defn ~name ~args ~@body)))

(defmacro main
  "Concise entrypoint macro.
   Arity 1 (common): (main InputSchema [args] ...)
   Arity 2 (advanced): (main [:=> ...] [args] ...)"
  [schema-or-input args & body]
  (let [is-function-schema? (and (vector? schema-or-input)
                                 (= :=> (first schema-or-input)))
        schema (if is-function-schema?
                 schema-or-input
                 [:=> [:cat schema-or-input] :any])]
    `(defn* -main ~schema ~args ~@body)))

(defn enable! []
  (mi/instrument!))
