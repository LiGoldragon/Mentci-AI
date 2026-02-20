#!/usr/bin/env bb

(require '[clojure.test :refer [deftest is run-tests]]
         '[clojure.string :as str])

(load-file "scripts/aski_flow_dot.clj")

(def example-flow
  (read-string (slurp "workflows/test.aski-flow")))

(deftest converts-example-aski-flow-to-dot
  (let [dot (mentci.aski-flow-dot/flow->dot {:flow example-flow
                                              :graphId "ExampleFlow"})]
    (is (str/includes? dot "digraph \"ExampleFlow\" {"))
    (is (str/includes? dot "Start -> ImplementationPlan;"))
    (is (str/includes? dot "ImplementationPlan -> Review [label=\"success\", condition=\"success\"];"))
    (is (str/includes? dot "Review -> Exit [label=\"pass\", condition=\"pass\"];"))
    (is (str/includes? dot "Review -> ImplementationPlan [label=\"fail\", condition=\"fail\"];"))))

(deftest macro-emits-dot-from-sugar-form
  (let [dot (mentci.aski-flow-dot/aski-flow->dot
             [ (Start {:type "start"})
               (Work {:type "codergen" :prompt "Do work"})
               {:ok Exit}
               (Exit {:type "exit"}) ])]
    (is (str/includes? dot "digraph \"AskiFlow\" {"))
    (is (str/includes? dot "Start -> Work;"))
    (is (str/includes? dot "Work -> Exit [label=\"ok\", condition=\"ok\"];"))))

(let [{:keys [fail error]} (run-tests)]
  (when (pos? (+ fail error))
    (System/exit 1)))
