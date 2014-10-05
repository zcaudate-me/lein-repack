(ns leiningen.repack.graph-test
  (:use midje.sweet)
  (:require [clojure.set :as set]  ;;[leiningen.repack.graph :refer :all]
            [leiningen.repack.manifest.common :as common]
            [leiningen.repack.manifest.source :as source]
            [leiningen.repack.analyser.java]
            [leiningen.repack.analyser.clj]
            [leiningen.repack.analyser.cljs]))

(def a (merge-with set/union
                 (common/build-manifest "example/repack.advance"
                                        {:subpackage "jvm"
                                         :path "java/im/chit/repack"
                                         :distribute {"common" #{"common"}
                                                      "web"    #{"web"}}})

                 (source/build-manifest "example/repack.advance"
                                        {:levels 2
                                         :path "src/clj"
                                         :package #{"web"}})))

(let [all-info  (get a "web")
      imports (apply set/union (map :imports all-info))
      exports (apply set/union (map :exports all-info))
      selfref (set/intersection imports exports)]
  {:imports (set/difference imports selfref)
   :exports (set/difference exports selfref)})

(defn )

(defn package-referencs
  (map :imports )
  (apply ))

(defn package-exports)
