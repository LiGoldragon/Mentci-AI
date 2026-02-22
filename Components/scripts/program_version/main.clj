#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.java.io :as io]
         '[clojure.string :as str])
(import 'java.security.MessageDigest
        'java.math.BigInteger)

(load-file (str (.getParent (.getParentFile (io/file *file*))) "/lib/malli.clj"))
(require '[mentci.malli :refer [defn* impl main enable!]])

(enable!)

;; Jujutsu-inspired alphabet (no vowels, 32 chars)
(def JJ_ALPHABET "zkwpqrstnmvxlhgybjdf0123456789")

(def Input
  [:map
   [:args [:vector :string]]])

(def HashInput
  [:map
   [:data :string]])

(def EncodeInput
  [:map
   [:bytes :any]
   [:length :int]])

(defprotocol ProgramVersionOps
  (sha256-for [this input])
  (encode-jj-for [this input])
  (core-dir-path-for [this input])
  (program-version-for [this input]))

(defrecord DefaultProgramVersion [])

(impl DefaultProgramVersion ProgramVersionOps sha256-for
  [:=> [:cat :any HashInput] :any]
  [this input]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.digest digest (.getBytes (:data input) "UTF-8"))))

(impl DefaultProgramVersion ProgramVersionOps encode-jj-for
  [:=> [:cat :any EncodeInput] :string]
  [this input]
  (let [big-int (BigInteger. 1 (:bytes input))
        base (BigInteger/valueOf (long (count JJ_ALPHABET)))]
    (loop [n big-int
           res ""]
      (if (or (zero? (.compareTo n BigInteger/ZERO))
              (>= (count res) (:length input)))
        (str/reverse res)
        (let [mod (.remainder n base)
              ch (.charAt JJ_ALPHABET (.intValue mod))]
          (recur (.divide n base) (str res ch)))))))

(impl DefaultProgramVersion ProgramVersionOps core-dir-path-for
  [:=> [:cat :any :map] :string]
  [this input]
  (if (.exists (io/file "Core")) "Core" "core"))

(impl DefaultProgramVersion ProgramVersionOps program-version-for
  [:=> [:cat :any :map] :string]
  [this input]
  (let [core-dir (io/file (core-dir-path-for this {}))
        files (->> (file-seq core-dir)
                   (filter #(.isFile %))
                   (sort-by #(.getPath %)))
        combined-content (str/join "" (map slurp files))
        hash-bytes (sha256-for this {:data combined-content})]
    (encode-jj-for this {:bytes hash-bytes :length 8})))

(def default-program-version (->DefaultProgramVersion))

(main Input
  [input]
  (if (= (first (:args input)) "get")
    (println (program-version-for default-program-version {}))
    (println "Usage: program_version.clj get")))

(-main {:args (vec *command-line-args*)})
