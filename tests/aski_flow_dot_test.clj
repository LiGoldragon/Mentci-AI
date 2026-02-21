#!/usr/bin/env bb

(require '[clojure.test :refer [deftest is run-tests]]
         '[clojure.string :as str])

(load-file "scripts/aski_flow_dot/main.clj")

(def example-flow
  (read-string (slurp "workflows/test.aski-flow")))

(deftest converts-example-aski-flow-to-graphviz-json-medium
  (let [medium (mentci.aski-flow-dot/aski-flow->graphviz-json
                {:flow example-flow :graphId "ExampleFlow"})
        objects (vec (get medium "objects"))
        edges (vec (get medium "edges"))]
    (is (= "ExampleFlow" (get medium "name")))
    (is (= true (get medium "directed")))
    (is (= false (get medium "strict")))
    (is (= ["ImplementationPlan" "Review" "DeliveredArtifact"]
           (mapv #(get % "name") objects)))
    ;; Edge: ImplementationPlan (0) -> Review (1) [success]
    (is (some #(and (= 0 (get % "tail")) (= 1 (get % "head")) (= "success" (get % "label"))) edges))
    ;; Edge: Review (1) -> DeliveredArtifact (2) [pass]
    (is (some #(and (= 1 (get % "tail")) (= 2 (get % "head")) (= "pass" (get % "label"))) edges))
    ;; Edge: Review (1) -> ImplementationPlan (0) [fail]
    (is (some #(and (= 1 (get % "tail")) (= 0 (get % "head")) (= "fail" (get % "label"))) edges))))

(deftest emits-dot-only-via-graphviz-json-medium
  (let [medium (mentci.aski-flow-dot/aski-flow->graphviz-json
                {:flow example-flow :graphId "ExampleFlow"})
        dot (mentci.aski-flow-dot/graphviz-json->dot {:graph medium})]
    (is (str/includes? dot "digraph \"ExampleFlow\" {"))
    (is (str/includes? dot "ImplementationPlan -> Review [label=\"success\", condition=\"success\"];"))
    (is (str/includes? dot "Review -> DeliveredArtifact [label=\"pass\", condition=\"pass\"];"))
    (is (str/includes? dot "Review -> ImplementationPlan [label=\"fail\", condition=\"fail\"];"))))

(deftest macro-emits-dot-via-medium
  (let [dot (mentci.aski-flow-dot/aski-flow->dot-macro
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
