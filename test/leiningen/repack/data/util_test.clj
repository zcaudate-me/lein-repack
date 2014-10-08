(ns leiningen.repack.data.util-test
  (:use midje.sweet)
  (:require [leiningen.repack.data.util :refer :all]
            [clojure.java.io :as io]))

^{:refer leiningen.repack.data.util/interpret-dots :added "0.1.5"}
(fact "interprets dots within a path vector"

  (interpret-dots ["a" "b" "." "c"]) => ["a" "b" "c"]

  (interpret-dots ["a" "b" ".." "c"]) => ["a" "c"])

^{:refer leiningen.repack.data.util/drop-while-matching :added "0.1.5"}
(fact "drops elements from the head of the two sequences if they contain
   the same element"

  (drop-while-matching ["a" "b" "c"] ["a" "b" "d" "e"])
  => [["c"] ["d" "e"]])


^{:refer leiningen.repack.data.util/path-vector :added "0.1.5"}
(fact "creates a vector from a path"

  (path-vector "/usr/local/bin")
  => ["" "usr" "local" "bin"]

  (path-vector (io/file "/usr/local/bin"))
  => ["" "usr" "local" "bin"])

^{:refer leiningen.repack.data.util/relative-path :added "0.1.5"}
(fact "finds the relative path as a string of a particular file
  location given a base directory"

  (relative-path "/usr/local/bin" "/usr/local/bin/example/one.sh")
  => "example/one.sh"

  (relative-path "/usr/local/bin" "/usr/local/lib/clojure/all.clj")
  => "../lib/clojure/all.clj")

^{:refer leiningen.repack.data.util/best-match :added "0.1.5"}
(fact "groups a bunch of files using a map containing how they should
  be distributed based upon "

  (best-match "b/c/file.txt" {"a" #{"a" "b"}
                              "c" #{"b/c"}})
  => ["c" 2])

^{:refer leiningen.repack.data.util/group-by-distribution :added "0.1.5"}
(fact "groups a bunch of files using a map containing how they should
  be distributed based upon "

  (group-by-distribution {"a" #{"a" "b"} "c" #{"b/c"}}
                         ["a/file.txt" "b/c/file.txt" "b/file.txt" "none.txt"])
  => {nil #{"none.txt"}
      "a" #{"a/file.txt" "b/file.txt"}
      "c" #{"b/c/file.txt"}})
