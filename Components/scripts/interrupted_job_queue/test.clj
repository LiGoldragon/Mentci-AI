#!/usr/bin/env bb

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {metosin/malli {:mvn/version "0.17.0"}}})

(require '[clojure.test :refer [deftest is run-tests]])

(load-file "Components/scripts/lib/malli.clj")
(require '[mentci.malli :refer [defn* enable!]])
(enable!)

(load-file "Components/scripts/interrupted_job_queue/main.clj")

(defn* test-marker [:=> [:cat] :boolean] []
  true)

(def classifier (->DefaultClassifier))

(deftest classifier-methods-test
  (is (test-marker))
  (is (= "Commit-Protocol-Merge-Fanin"
         (subject-for classifier {:prompt "make sure release tag and push commit are correct"})))
  (is (= "strategy-only"
         (class-for classifier {:prompt "strategize this work"})))
  (is (= 1
         (priority-for classifier {:prompt "fix commit protocol and session push"})))
  (is (= "requires-confirmation"
         (class-for classifier {:prompt "what should we do next?"}))))

(deftest queue-build-and-dedup-test
  (let [jobs (build-jobs {:prompts ["fix commit protocol"
                                    "fix commit protocol"
                                    "report on strategy queue"]})
        deduped (dedup-jobs {:jobs jobs})]
    (is (= 2 (count deduped)))
    (is (= "IJ-001" (:jobId (first deduped))))
    (is (= "IJ-002" (:jobId (second deduped))))
    (is (= "strategy-only" (:executionClass (second deduped))))))

(let [{:keys [fail error]} (run-tests)]
  (when (pos? (+ fail error))
    (System/exit 1)))
