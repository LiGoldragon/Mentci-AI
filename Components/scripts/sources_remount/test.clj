(require '[clojure.test :refer [deftest is run-tests]]
         '[clojure.java.io :as io])

(load-file "Components/scripts/sources_remount/main.clj")

(defn random-temp-path [prefix]
  (str (System/getProperty "java.io.tmpdir") "/" prefix "-" (java.util.UUID/randomUUID)))

(defn delete-tree! [path]
  (let [file (io/file path)]
    (when (.exists file)
      (if (.isDirectory file)
        (doseq [child (reverse (file-seq file))]
          (.delete child))
        (.delete file)))))

(deftest remount-input-replaces-stale-target-with-symlink
  (let [root (random-temp-path "Sources-remount-test")
        src-path (str root "/src/foo")
        target-path (str root "/Sources/foo")]
    (try
      (.mkdirs (io/file src-path))
      (.mkdirs (io/file target-path))
      (spit (str target-path "/stale.txt") "stale")
      (spit (str src-path "/fresh.txt") "fresh")

      (remount-input-for default-inputs-remount {:name "foo"
                                                 :sourcePath src-path
                                                 :targetPath target-path})

      (let [target (.toPath (io/file target-path))]
        (is (java.nio.file.Files/isSymbolicLink target))
        (is (= (.toPath (io/file src-path))
               (java.nio.file.Files/readSymbolicLink target))))
      (finally
        (delete-tree! root)))))

(deftest strip-write-permissions-removes-write-bits-recursively
  (let [root (random-temp-path "Sources-remount-perms")
        src-path (str root "/src/foo")
        nested-path (str src-path "/sub")
        file-path (str nested-path "/file.txt")]
    (try
      (.mkdirs (io/file nested-path))
      (spit file-path "x")
      (.setWritable (io/file src-path) true false)
      (.setWritable (io/file nested-path) true false)
      (.setWritable (io/file file-path) true false)

      (let [report (strip-write-permissions-for default-inputs-remount {:path src-path})]
        (is (> (:visited report) 0))
        (is (> (:changed report) 0))
        (is (false? (.canWrite (io/file src-path))))
        (is (false? (.canWrite (io/file nested-path))))
        (is (false? (.canWrite (io/file file-path)))))
      (finally
        ;; Restore writability so cleanup can proceed.
        (.setWritable (io/file file-path) true false)
        (.setWritable (io/file nested-path) true false)
        (.setWritable (io/file src-path) true false)
        (delete-tree! root)))))

(let [summary (run-tests)]
  (if (pos? (+ (:fail summary) (:error summary)))
    (System/exit 1)
    (System/exit 0)))
