#!/usr/bin/env bb

(require '[clojure.java.io :as io]
         '[clojure.string :as str])
(import 'java.security.MessageDigest)

;; Tool Stack Transparency:
;; Runtime: Babashka (Clojure)
;; Rationale: High-fidelity file hashing and dense encoding for programming versioning.

;; Jujutsu-inspired alphabet (no vowels, 32 chars)
(def JJ_ALPHABET "zkwpqrstnmvxlhgybjdf0123456789")

(defn sha256 [data]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.digest digest (.getBytes data "UTF-8"))))

(defn encode-jj [bytes length]
  (let [big-int (BigInteger. 1 bytes)
        base (BigInteger. (str (count JJ_ALPHABET)))]
    (loop [n big-int
           res ""]
      (if (or (zero? (.compareTo n BigInteger/ZERO)) (>= (count res) length))
        (str/reverse res)
        (let [mod (.remainder n base)
              char (.charAt JJ_ALPHABET (.intValue mod))]
          (recur (.divide n base) (str res char)))))))

(defn get-program-version []
  (let [core-dir (io/file "core")
        files (sort (.listFiles core-dir))
        combined-content (str/join "" (map slurp files))
        hash-bytes (sha256 combined-content)]
    (encode-jj hash-bytes 8)))

(if (= (first *command-line-args*) "get")
  (println (get-program-version))
  (println "Usage: program_version.clj get"))
