(ns leiningen.repack.manifest.source-test
  (:use midje.sweet)
  (:require [clojure.java.io :as io]
            [leiningen.repack.manifest.source :refer :all]))

^{:refer leiningen.repack.manifest.source/child-dirs :added "0.1.5"}
(fact "lists all the child directories for a particular folder"
  (child-dirs (io/file "example"))
  => ["repack.advance"])

^{:refer leiningen.repack.manifest.source/split-path :added "0.1.5"}
(fact "splits the file into its path components"
  (split-path "repack/example/hello.clj")
  =>  ["repack" "example" "hello"])

^{:refer leiningen.repack.manifest.source/build-manifest :added "0.1.5"}
(fact "builds manifest for clojure sources"

  (build-manifest "example/repack.advance"
                  {:levels 2
                   :path "src/clj"
                   :package #{"web"}})
  => (contains {"common"     anything ;; {src/clj/repack/common.clj}
                "core"       anything ;; {src/clj/repack/core.clj}
                "util.array" anything ;; {src/clj/repack/util/array.clj .. }
                "util.data"  anything ;; {src/clj/repack/util/data.clj}
                "web"        anything ;; {src/clj/repack/web.clj .. }
                })

  (build-manifest "example/repack.advance"
                  {:levels 2
                   :path "src/cljs"})
  => (contains {"web.client" anything ;; {src/cljs/repack/web/client.cljs},
                "web"        anything ;; {src/cljs/repack/web.cljs}
                })

  (build-manifest "example/repack.advance"
                  {:levels 2
                   :path "src/cljs"
                   :package #{"web"}})
  => (contains {"web"       anything ;; {src/cljs/repack/web.cljs .. }
                }))
