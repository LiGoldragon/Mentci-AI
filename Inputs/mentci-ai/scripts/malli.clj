#!/usr/bin/env bb

(ns mentci.malli
  (:require [malli.core :as m]
            [malli.instrument :as mi]))

(defmacro defn* [name schema args & body]
  `(do
     (m/=> ~name ~schema)
     (defn ~name ~args ~@body)))

(defn enable! []
  (mi/instrument!))
