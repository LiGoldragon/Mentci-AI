(require '[clojure.test :refer [deftest is run-tests]]
         '[clojure.java.io :as io])

(load-file "scripts/tool_discoverer/main.clj")

(deftest test-search-cargo-parsing
  (with-redefs [clojure.java.shell/sh (fn [& _] {:exit 0 :out "dot-parser = \"0.6.1\"\ngraphviz-rs = \"0.1.0\""})]
    (let [results (search-cargo "dot")]
      (is (= 2 (count results)))
      (is (= "dot-parser" (first results))))))

(let [summary (run-tests)]
  (if (pos? (+ (:fail summary) (:error summary)))
    (System/exit 1)
    (System/exit 0)))
