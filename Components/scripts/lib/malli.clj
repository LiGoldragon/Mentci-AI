#!/usr/bin/env bb

(ns mentci.malli
  (:require [malli.core :as m]
            [malli.instrument :as mi]
            [malli.experimental.lite :as ml]
            [clojure.string :as str]))

(defmacro defn* [name schema args & body]
  `(do
     (m/=> ~name ~schema)
     (defn ~name ~args ~@body)))

(defmacro main
  "Concise entrypoint macro.
   Explicit args form:
     (main InputSchemaOrLite [args] ...)
     (main [:=> ...] [args] ...)

   Auto-arg form:
     (main InputSchemaOrLite ...body...)
   In auto-arg form, a symbol schema derives one arg name by lowercasing the first
   letter (e.g. MainInput -> [mainInput], Input -> [input])."
  [schema-or-input args-or-body & maybe-body]
  (let [explicit-args? (vector? args-or-body)
        args (if explicit-args?
               args-or-body
               (if (symbol? schema-or-input)
                 (let [n (name schema-or-input)
                       lowered (str (str/lower-case (subs n 0 1)) (subs n 1))]
                   [(symbol lowered)])
                 ['input]))
        body (if explicit-args?
               maybe-body
               (cons args-or-body maybe-body))
        is-function-schema? (and (vector? schema-or-input)
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
